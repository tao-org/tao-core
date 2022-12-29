/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.orchestration;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.*;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.ProductHelperFactory;
import ro.cs.tao.docker.DockerVolumeMap;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.eodata.metadata.DecodeStatus;
import ro.cs.tao.eodata.metadata.MetadataInspector;
import ro.cs.tao.eodata.naming.NameExpressionParser;
import ro.cs.tao.eodata.naming.NamingRule;
import ro.cs.tao.eodata.naming.ParseException;
import ro.cs.tao.eodata.util.ProductHelper;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.ExecutionsManager;
import ro.cs.tao.execution.callback.EndpointDescriptor;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.messaging.system.StartupCompletedMessage;
import ro.cs.tao.optimization.WorkflowOptimizer;
import ro.cs.tao.orchestration.commands.JobCommand;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.orchestration.status.TaskStatusHandler;
import ro.cs.tao.persistence.*;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.services.utils.WorkflowUtilities;
import ro.cs.tao.spi.OutputDataHandlerManager;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.FileMap;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.TriFunction;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The singleton orchestrator of task executions.
 *
 * @author Cosmin Cara
 */
public class Orchestrator extends Notifiable {

    private static final String GROUP_TASK_SELECTOR_KEY = "group.task.selector";
    private static final String JOB_TASK_SELECTOR_KEY = "job.task.selector";
    private static final String USER_JOB_LIMIT_KEY = "tao.user.jobs.limit";
    private static final Set<ExecutionStatus> notFurtherRunnableStatuses = new HashSet<>() {{
        add(ExecutionStatus.DONE);
        add(ExecutionStatus.CANCELLED);
        add(ExecutionStatus.FAILED);
    }};
    private static final Set<ExecutionStatus> completedOkStatuses = new HashSet<>() {{
        add(ExecutionStatus.DONE);
        add(ExecutionStatus.PENDING_FINALISATION);
    }};

    private static final Orchestrator instance;
    private final Map<Long, InternalStateHandler<?>> groupStateHandlers;
    private final BlockingQueue<AbstractMap.SimpleEntry<Message, SessionContext>> queue;
    private final Map<String, List<Long>> waitingJobs;

    static {
        instance = new Orchestrator();
    }

    public static Orchestrator getInstance() {
        return instance;
    }

    private final StringListAdapter listAdapter;
    private final Logger logger = Logger.getLogger(Orchestrator.class.getName());

    private final WorkflowProvider workflowProvider;
    private final WorkflowNodeProvider workflowNodeProvider;
    private final ExecutionJobProvider jobProvider;
    private final ExecutionTaskProvider taskProvider;
    private final ProcessingComponentProvider processingComponentProvider;
    private final GroupComponentProvider groupComponentProvider;
    private final NamingRuleProvider namingRuleProvider;
    private final UserProvider userProvider;

    private TaskSelector<ExecutionGroup> groupTaskSelector;
    private TaskSelector<ExecutionJob> jobTaskSelector;
    private DefaultJobFactory jobFactory;
    private Set<MetadataInspector> metadataServices;
    private final Map<String, ExecutorService> executors;
    private final Set<Long> runningTasks;

    private Orchestrator() {
        this.groupStateHandlers = new HashMap<>();
        this.queue = new LinkedBlockingDeque<>();
        this.executors = Collections.synchronizedMap(new HashMap<>());
        this.executors.put(SystemPrincipal.instance().getName(), Executors.newFixedThreadPool(1));
        this.runningTasks = Collections.synchronizedSet(new HashSet<>());
        this.waitingJobs = Collections.synchronizedMap(new FileMap<>(Paths.get(SystemVariable.CACHE.value())));
        this.listAdapter = new StringListAdapter();
        this.jobProvider = SpringContextBridge.services().getService(ExecutionJobProvider.class);
        this.taskProvider = SpringContextBridge.services().getService(ExecutionTaskProvider.class);
        this.workflowProvider = SpringContextBridge.services().getService(WorkflowProvider.class);
        this.workflowNodeProvider = SpringContextBridge.services().getService(WorkflowNodeProvider.class);
        this.processingComponentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        this.groupComponentProvider = SpringContextBridge.services().getService(GroupComponentProvider.class);
        this.namingRuleProvider = SpringContextBridge.services().getService(NamingRuleProvider.class);
        this.userProvider = SpringContextBridge.services().getService(UserProvider.class);
        subscribe(Topic.EXECUTION.value(), Topic.SYSTEM.value());
    }

    public void start() {
        //JobCommand.setPersistenceManager(persistenceManager);
        //TaskUtilities.setPersistenceManager(persistenceManager);
        final Function<Long, WorkflowNodeDescriptor> workflowProvider = this.workflowNodeProvider::get;
        initializeJobSelector(workflowProvider, this.taskProvider::getByJobAndNode);
        initializeGroupSelector(workflowProvider, this.taskProvider::getByGroupAndNode);
        TaskStatusHandler.registerHandlers(jobProvider, taskProvider);
        this.jobFactory = new DefaultJobFactory();
        this.metadataServices = ServiceRegistryManager.getInstance().getServiceRegistry(MetadataInspector.class).getServices();
        OrchestratorWorker monitor = new OrchestratorWorker();
        monitor.setName("orchestrator");
        monitor.start();
        logger.fine("Orchestration service initialized");
    }

    public Map<String, ExecutorService> getUserExecutors() {
        return this.executors;
    }

    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param context 	 The session context 
     * @param workflowId The workflow identifier
     * @param inputs     The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public ExecutionJob startWorkflow(SessionContext context, long workflowId, String description,
                              Map<String, Map<String, String>> inputs) throws ExecutionException {
        try {
            final String user = context.getPrincipal().getName();
            final ExecutionJob job = tryCreateJob(user, context, workflowId, description, inputs);
            if (job == null) {
                throw new ExecutionException("Another job is running or queued for user '" + user + "'. New job will be queued");
            } else {
                this.executors.get(user).submit(() -> {
                    try {
                        JobCommand.START.applyTo(job);
                    } catch (ExecutionException e) {
                        try {
                            cancelJob(job);
                        } catch (PersistenceException ex) {
                            logger.severe(ex.getMessage());
                        }
                    }
                });
            }
            return job;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e instanceof ExecutionException ? (ExecutionException) e : new ExecutionException(e.getMessage());
        }
    }
    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param context 	 The session context
     * @param inputs     The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public ExecutionJob startExecution(SessionContext context, String wpsId,
                                       Map<String, Map<String, String>> inputs, ExecutorService executorService) throws ExecutionException {
        try {
            final String user = context.getPrincipal().getName();
            final ExecutionJob executionJob = tryCreateJob(user, wpsId, inputs);
            if (executionJob == null) {
                throw new ExecutionException("Another WPS job is running or queued for user '" + user + "'. New job will be queued");
            } else {
                this.executors.putIfAbsent(user, executorService);
                executorService.submit(() -> {
                    try {
                        JobCommand.START.applyTo(executionJob);
                    } catch (ExecutionException e) {
                        try {
                            cancelJob(executionJob);
                        } catch (PersistenceException ex) {
                            logger.severe(ex.getMessage());
                        }
                    }
                });
            }
            return executionJob;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e instanceof ExecutionException ? (ExecutionException) e : new ExecutionException(e.getMessage());
        }
    }


    /**
     * Creates a job from an externally-defined workflow and starts its execution.
     *
     * @param context 	 The session context
     * @param workflowId The workflow identifier
     * @param inputs     The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public ExecutionJob startExternalWorkflow(SessionContext context, long workflowId, String description,
                                              Map<String, Map<String, String>> inputs, ExecutorService executorService,
                                              EndpointDescriptor callback) throws ExecutionException {
        ExecutionJob executionJob;
        try {
            executionJob = startWorkflow(context, workflowId, description, inputs);
            if (executionJob != null) {
                executionJob.setExternal(true);
                executionJob.setCallbackDescriptor(callback);
                jobProvider.update(executionJob);
            }
            return executionJob;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e instanceof ExecutionException ? (ExecutionException) e : new ExecutionException(e.getMessage());
        }
    }

    /**
     * Creates a job from a workflow without starting its execution.
     * The method is intended for operations that convert/export a TAO workflow
     * into another format (such as JSON, CWL)
     *
     * @param context 	 The session context
     * @param workflowId The workflow identifier
     * @param inputs     The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong
     */
    public ExecutionJob createJob(SessionContext context, long workflowId, String description,
                                  Map<String, Map<String, String>> inputs)  throws ExecutionException {
        try {
            final WorkflowDescriptor descriptor = workflowProvider.get(workflowId);
            if (descriptor == null) {
                throw new ExecutionException(String.format("Non-existent workflow [%s]", workflowId));
            }
            final ExecutionJob job = new ScriptJobFactory().createJob(context, description, descriptor, inputs);
            this.executors.get(context.getPrincipal().getName()).submit(() -> {
                try {
                    JobCommand.START.applyTo(job);
                } catch (ExecutionException e) {
                    try {
                        cancelJob(job);
                    } catch (PersistenceException ex) {
                        logger.severe(ex.getMessage());
                    }
                }
            });
            return job;
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Creates a job from a workflow, intended for execution.
     *
     * @param context 	 The session context
     * @param workflowId The workflow identifier
     * @param description A description of the execution (workflow instance)
     * @param inputs     The overridden parameter values for workflow nodes,
     * @param optimize  If <code>true</code>, the workflow will be optimized if possible
     * @throws ExecutionException In case anything goes wrong
     */
    public ExecutionJob createJob(SessionContext context, long workflowId, String description,
                                  Map<String, Map<String, String>> inputs, boolean optimize)  throws ExecutionException {
        try {
            final WorkflowDescriptor descriptor = workflowProvider.get(workflowId);
            if (descriptor == null) {
                throw new ExecutionException(String.format("Non-existent workflow [%s]", workflowId));
            }
            final Principal principal = context.getPrincipal();
            // check if the user's disk quota is not reached
            if (!checkProcessingQuota(principal)) {
                // do not create any job
                throw new ExecutionException("Quota exceeded for user " + principal.getName());
            }
            WorkflowDescriptor optimized = descriptor;
            if (optimize) {
                optimized = WorkflowOptimizer.getOptimizedWorkflow(descriptor);
                if (optimized == null) {
                    optimized = descriptor;
                }
            }
            return this.jobFactory.createJob(context, description, optimized, inputs);
        } catch (QuotaException | PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Creates a job for a WPS, intended for execution.
     *
     * @param wpsId     The WPS identifier
     * @param inputs     The overridden parameter values,
     * @throws ExecutionException In case anything goes wrong
     */
    public ExecutionJob createJob(String wpsId, Map<String, Map<String, String>> inputs)  throws ExecutionException {
        try {
            return this.jobFactory.createJob(SessionStore.currentContext(), wpsId, inputs);
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Stops the execution of the given job corresponding
     *
     * @param jobId The job identifier
     * @throws ExecutionException In case anything goes wrong or there was no job for this workflow
     */
    public void stopJob(long jobId) throws ExecutionException {
        ExecutionJob job = checkJob(jobId);
        JobCommand.STOP.applyTo(job);
    }

    /**
     * Pauses (suspends) the execution of the given job corresponding.
     *
     * @param jobId The job identifier
     * @throws ExecutionException In case anything goes wrong or there was no job for this workflow
     */
    public void pauseJob(long jobId) throws ExecutionException {
        ExecutionJob job = checkJob(jobId);
        JobCommand.SUSPEND.applyTo(job);
    }

    /**
     * Resumes the execution of the job corresponding to this workflow.
     *
     * @param jobId The job identifier
     * @throws ExecutionException In case anything goes wrong or there was no job with this id
     */
    public void resumeJob(long jobId) throws ExecutionException {
        ExecutionJob job = checkJob(jobId);
        JobCommand.RESUME.applyTo(job);
    }

    public int purgeJobs(String user) {
        final List<Long> jobIds = this.waitingJobs.remove(user);
        int purged = 0;
        if (jobIds != null) {
            jobIds.forEach(this::stopJob);
            purged = jobIds.size();
        }
        List<ExecutionJob> jobs = jobProvider.list(user,
                                                   new HashSet<>() {{
                                                       add(ExecutionStatus.UNDETERMINED);
                                                       add(ExecutionStatus.QUEUED_ACTIVE);
                                                       add(ExecutionStatus.RUNNING);
                                                   }});
        if (jobs != null) {
            jobs.forEach(j -> stopJob(j.getId()));
            purged += jobs.size();
        }
        return purged;
    }

    @Override
    protected void onMessageReceived(Message message) {
        logger.finest("Received message " + message);
        if (message instanceof StartupCompletedMessage) {
            /*final List<ExecutionJob> jobs = prepareStalledJobs();
            if (jobs != null) {
                for (ExecutionJob job : jobs) {
                    this.executors.put(job.getUserName(),
                                       Executors.newFixedThreadPool(2));
                    logger.fine(String.format("Resuming job [id=%s]", job.getId()));
                    JobCommand.RESUME.applyTo(job);
                }
            }*/
        } else {
            try {
                final SessionContext context = new MessageContext(message);
                queue.put(new AbstractMap.SimpleEntry<>(message, context));
            } catch (InterruptedException e) {
                logger.severe(e.getMessage());
            }
        }
    }

    private List<ExecutionJob> prepareStalledJobs() {
        List<ExecutionJob> jobs = jobProvider.list(ExecutionStatus.RUNNING);
        if (jobs != null) {
            for (ExecutionJob job : jobs) {
                job.getTasks().stream()
                              .filter(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING)
                              .forEach(t -> {
                                  try {
                                      t = taskProvider.updateStatus(t, ExecutionStatus.SUSPENDED, "Stalled job");
                                  } catch (PersistenceException e) {
                                      logger.severe(String.format("Cannot reset status for task %s [job %s]",
                                                                  t.getId(), job.getId()));
                                  }
                              });
                job.setExecutionStatus(ExecutionStatus.SUSPENDED);
                try {
                    jobProvider.update(job);
                } catch (PersistenceException e) {
                    logger.severe(String.format("Cannot reset status for job %s", job.getId()));
                }
            }
        }
        return jobs;
    }

    private void initializeGroupSelector(Function<Long, WorkflowNodeDescriptor> workflowProvider,
                                         TriFunction<Long, Long, Integer, ExecutionTask> nodeProvider) {
        String groupTaskSelectorClass = ConfigurationManager.getInstance().getValue(GROUP_TASK_SELECTOR_KEY,
                                                                                        DefaultGroupTaskSelector.class.getName());
        final Set<TaskSelector> selectors = ServiceRegistryManager.getInstance()
                .getServiceRegistry(TaskSelector.class).getServices()
                .stream().filter(s -> ExecutionGroup.class.equals(s.getTaskContainerClass()))
                .collect(Collectors.toSet());
        //noinspection unchecked
        this.groupTaskSelector = selectors.stream()
                .filter(s -> s.getClass().getName().equals(groupTaskSelectorClass))
                .map(s -> (TaskSelector<ExecutionGroup>) s)
                .findFirst().orElse(null);
        if (this.groupTaskSelector == null) {
            throw new RuntimeException(String.format("Cannot instantiate the selector class defined by %s in configuration",
                                                     GROUP_TASK_SELECTOR_KEY));
        }
        this.groupTaskSelector.setWorkflowProvider(workflowProvider);
        this.groupTaskSelector.setTaskByNodeProvider(nodeProvider);
    }

    private void initializeJobSelector(Function<Long, WorkflowNodeDescriptor> workflowProvider,
                                       TriFunction<Long, Long, Integer, ExecutionTask> nodeProvider) {
        String jobTaskSelectorClass = ConfigurationManager.getInstance().getValue(JOB_TASK_SELECTOR_KEY,
                                                                                      DefaultJobTaskSelector.class.getName());
        final Set<TaskSelector> selectors = ServiceRegistryManager.getInstance()
                .getServiceRegistry(TaskSelector.class).getServices()
                .stream().filter(s -> ExecutionJob.class.equals(s.getTaskContainerClass()))
                .collect(Collectors.toSet());
        //noinspection unchecked
        this.jobTaskSelector = selectors.stream()
                .filter(s -> s.getClass().getName().equals(jobTaskSelectorClass))
                .map(s -> (TaskSelector<ExecutionJob>) s)
                .findFirst().orElse(null);
        if (this.jobTaskSelector == null) {
            throw new RuntimeException(String.format("Cannot instantiate the selector class defined by %s in configuration",
                                                     JOB_TASK_SELECTOR_KEY));
        }
        this.jobTaskSelector.setWorkflowProvider(workflowProvider);
        this.jobTaskSelector.setTaskByNodeProvider(nodeProvider);
    }

    private synchronized void processMessage(Message message, SessionContext currentContext) {
        ExecutionTask task = null;
        String user = null;
        try {
            final String taskId = message.getItem(Message.SOURCE_KEY);
            final ExecutionStatus status = EnumUtils.getEnumConstantByName(ExecutionStatus.class,
                                                                           message.getPayload());
            task = taskProvider.get(Long.parseLong(taskId));
            user = task.getJob().getUserName();
            currentContext.setPrincipal(new UserPrincipal(user));
            task.setContext(currentContext);
            if (!(status == ExecutionStatus.QUEUED_ACTIVE && task.getExecutionStatus() == ExecutionStatus.RUNNING)) {
                // it may happen that the notification of Queue_Active to arrive after the task was actually put
                // in Running state, hence we don't need to update the status
                changeStatus(task, status, message.getMessage());
            }
            if (notFurtherRunnableStatuses.contains(status)) {
                this.runningTasks.remove(task.getId());
                if (task instanceof ProcessingExecutionTask) {
                    ExecutionsManager.getInstance().doPostExecuteAction(task);
                }
                logger.finest(String.format("Active or pending tasks: %d", this.runningTasks.size()));
            }
            user = task.getJob().getUserName();
            //user = currentContext.getPrincipal().getName();
            if (completedOkStatuses.contains(status)) {
                // Current task has completed somehow
                final WorkflowNodeDescriptor taskNode = workflowNodeProvider.get(task.getWorkflowNodeId());
                // For DataSourceExecutionTask, it is the Query executor that sets the outputs.
                // Hence, we need to "confirm" here the outputs of a processing task.
                if (task instanceof ProcessingExecutionTask) {
                    handleProcessingTask((ProcessingExecutionTask) task, taskNode == null || taskNode.getPreserveOutput());
                }
                // Identify next tasks to be executed
                final List<ExecutionTask> nextTasks = findNextTasks(task);
                if (nextTasks != null && nextTasks.size() > 0) {
                    logger.finest(String.format("Has %s next tasks", nextTasks.size()));
                    for (ExecutionTask nextTask : nextTasks) {
                        final WorkflowNodeDescriptor nextNode = workflowNodeProvider.get(nextTask.getWorkflowNodeId());
                        final List<ParameterValue> additionalInfo = taskNode != null ? taskNode.getAdditionalInfo() : null;
                        final Set<ComponentLink> links = nextNode.getIncomingLinks();
                        final boolean manyParents = links.size() > 1;
                        if (!(additionalInfo != null && additionalInfo.stream().anyMatch(p -> "externalTaskId".equals(p.getParameterName())) &&
                            nextNode.getAdditionalInfo() == null)) {
                            if (!manyParents) {
                                flowOutputs(task, nextTask);
                            }
                        }
                        logger.finest(String.format("Task %s about to start.", nextTask.getId()));
                        nextTask.getInputParameterValues().forEach(
                                v -> logger.finest(String.format("Input: %s=%s", v.getKey(), v.getValue())));
                       final ExecutionGroup parentGroupTask = nextTask.getGroupTask();
                        if (parentGroupTask != null) {
                            String state = parentGroupTask.getInternalState();
                            if (state != null) {
                                InternalStateHandler<?> handler = this.groupStateHandlers.get(parentGroupTask.getId());
                                parentGroupTask.setStateHandler(handler);
                                nextTask.setInternalState(String.valueOf(((LoopState) handler.currentState()).getCurrent()));
                            }
                        }
                        final List<Variable> nextInputParameters = nextTask.getInputParameterValues();
                        final Set<String> outParams = nextTask.getOutputParameterValues().stream().map(Variable::getKey).collect(Collectors.toSet());
                        nextInputParameters.stream().filter(v -> !outParams.contains(v.getKey())).forEach(v -> resolveVariable(nextTask, v));
                        Variable overriddenOutParam = null;
                        for (Variable var : nextInputParameters) {
                            if (outParams.contains(var.getKey())) {
                                overriddenOutParam = var;
                                break;
                            }
                        }
                        // If there is an overridden output parameter, it may be that we have a name expression
                        if (overriddenOutParam != null) {
                            resolveTargetName(nextTask, overriddenOutParam);
                        }
                        if (manyParents) {
                            if (TaskUtilities.haveParentsCompleted(nextTask)) {
                                final List<String> parentIds = task.getJob().getTaskDependencies().get(String.valueOf(nextTask.getId()));
                                final List<ExecutionTask> parentTasks = parentIds.stream().map(i -> taskProvider.get(Long.parseLong(i))).collect(Collectors.toList());
                                boolean allDS = true;
                                int parentCardinality = 0;
                                for (ExecutionTask pTask : parentTasks) {
                                    allDS &= pTask instanceof DataSourceExecutionTask;
                                    parentCardinality = Math.max(parentCardinality, TaskUtilities.getTargetCardinality(pTask));
                                }
                                final int expectedCardinality = TaskUtilities.getSourceCardinality(nextTask);
                                if (allDS && parentCardinality > 1 && expectedCardinality == 1) {
                                    // There are more than 1 parent data source tasks that returned a list of results,
                                    // we need to split the job and
                                    try {
                                        List<ExecutionJob> jobs = this.jobFactory.splitJob(task.getJob());
                                        logger.fine(String.format("Job %s will fork additional %d jobs", task.getJob().getName(), jobs.size()));
                                        //final String user = task.getContext().getPrincipal().getName();
                                        final ExecutorService executorService = this.executors.get(user);
                                        for (ExecutionJob job : jobs) {
                                            if (canSubmitJob(user, job)) {
                                                executorService.submit(() -> JobCommand.START.applyTo(job));
                                            }
                                        }
                                    } catch (Exception e) {
                                        logger.warning(ro.cs.tao.utils.ExceptionUtils.getStackTrace(logger, e));
                                    }
                                }
                                TaskUtilities.transferParentOutputs(nextTask);
                                nextTask.setContext(currentContext);
                                if (!this.runningTasks.contains(nextTask.getId())) {
                                    this.runningTasks.add(nextTask.getId());
                                    TaskCommand.START.applyTo(nextTask);
                                }
                            } else {
                                logger.fine("Task " + nextTask.getId() + " has still predecessors that did not complete");
                            }
                        } else {
                            nextTask.setContext(currentContext);
                            if (!this.runningTasks.contains(nextTask.getId())) {
                                this.runningTasks.add(nextTask.getId());
                                TaskCommand.START.applyTo(nextTask);
                            }
                        }

                    }
                } else {
                    final ExecutionJob job = jobProvider.get(task.getJob().getId());
                    if (TaskUtilities.haveAllTasksCompleted(job)) {
                        //logger.fine(String.format("Job %s: no more child tasks to execute after the current task", job.getId()));
                        if (job.orderedTasks().stream()
                                .anyMatch(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING ||
                                               t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE)) {
                            return;
                        }
                        job.setExecutionStatus(ExecutionStatus.DONE);
                        if (job.getEndTime() == null) {
                            job.setEndTime(LocalDateTime.now());
                        }
                        jobProvider.update(job);
                        if (taskNode instanceof WorkflowNodeGroupDescriptor && taskNode.getPreserveOutput()) {
                            persistOutputProducts(task);
                        }
                        WorkflowDescriptor workflow = workflowProvider.get(job.getWorkflowId());
                        Duration time = null;
                        if (job.getStartTime() != null && job.getEndTime() != null) {
                            time = Duration.between(job.getStartTime(), job.getEndTime());
                        }
                        String msg = String.format("Job [%s] for workflow [%s]" +
                                                           (job.getExecutionStatus() == ExecutionStatus.DONE ?
                                                                   " completed in %ss" :
                                                                   " failed after %ss"),
                                                   job.getId(), workflow.getName(), time != null ? time.getSeconds() : "<unknown>");
                        logger.info(msg);
                        Messaging.send(currentContext.getPrincipal(), Topic.EXECUTION.value(), this, msg);
                        // If there are waiting jobs for this user, let's trigger the execution of the first one
                        List<Long> nextJobs;
                        if ((nextJobs = this.waitingJobs.get(user)) != null && nextJobs.size() > 0) {
                            Long nextJobId = nextJobs.remove(0);
                            if (nextJobs.size() == 0) {
                                this.waitingJobs.remove(user);
                            }
                            final ExecutorService executorService = this.executors.get(user);
                            if (executorService == null) {
                                throw new ExecutionException("Executor service for user " + user + " was reset");
                            }
                            final ExecutionJob nextJob = jobProvider.get(nextJobId);
                            executorService.submit(() -> JobCommand.START.applyTo(nextJob));
                        } else {
                            shutDownExecutor(user);
                        }
                        cleanupTransientComponents(job);
                    } else {
                        logger.fine("Job has still tasks to complete");
                    }
                }
            } else {
                final ExecutionJob job = task.getJob();
                final ExecutionStatus jobStatus = job.getExecutionStatus();
                if (jobStatus == ExecutionStatus.CANCELLED || jobStatus == ExecutionStatus.FAILED) {
                    cancelJob(job);
                }
            }
        } catch (Throwable e) {
            logger.severe(String.format("Abnormal termination: %s", e.getMessage()));
            ExecutionJob job = task.getJob();
            job.setExecutionStatus(ExecutionStatus.FAILED);
            try {
                jobProvider.update(job);
            } catch (PersistenceException e1) {
                logger.severe(String.format("Abnormal termination: %s", e1.getMessage()));
            }
            shutDownExecutor(user != null ? user : currentContext.getPrincipal().getName());
        }
    }

    private void cancelJob(ExecutionJob job) throws PersistenceException {
        if (job.getExecutionStatus() != ExecutionStatus.CANCELLED) {
            job.setExecutionStatus(ExecutionStatus.FAILED);
        }

        if (job.getEndTime() == null) {
            job.setEndTime(LocalDateTime.now());
        }
        WorkflowDescriptor workflow = workflowProvider.get(job.getWorkflowId());
        Duration time = null;
        if (job.getStartTime() != null && job.getEndTime() != null) {
            time = Duration.between(job.getStartTime(), job.getEndTime());
        }
        String msg = String.format("Job [%s] for workflow [%s] %s after %ss",
                                   job.getId(),
                                   workflow != null ? workflow.getName() : "n/a",
                                   (job.getExecutionStatus() == ExecutionStatus.CANCELLED ? " cancelled " : " failed "),
                                   time != null ? time.getSeconds() : "<unknown>");
        logger.warning(msg);
        jobProvider.update(job);
        job.getTasks().forEach(t -> {
            if (t.getExecutionStatus() != ExecutionStatus.DONE && t.getExecutionStatus() != ExecutionStatus.FAILED) {
                t.setExecutionStatus(ExecutionStatus.CANCELLED);
                try {
                    taskProvider.update(t);
                } catch (PersistenceException e) {
                    logger.warning(ro.cs.tao.utils.ExceptionUtils.getStackTrace(logger, e));
                }
            }
        });
        Messaging.send(new UserPrincipal(job.getUserName()), Topic.EXECUTION.value(), this, msg);
        shutDownExecutor(job.getUserName());
        cleanupTransientComponents(job);
    }

    private void shutDownExecutor(final String principalName) {
        if (!SystemPrincipal.instance().getName().equals(principalName)) {
            final List<Long> pendingJobIds = this.waitingJobs.get(principalName);
            if (pendingJobIds != null && pendingJobIds.size() > 0) {
                final ExecutorService service = executors.get(principalName);
                if (service != null) {
                    service.shutdown();
                    executors.remove(principalName);
                }
                // update user processing quota
                try {
                    UserQuotaManager.getInstance().updateUserProcessingQuota(new UserPrincipal(principalName));
                } catch (QuotaException e) {
                    logger.severe(String.format("Error updating quota. Error message: %s", e.getMessage()));
                }
            }
        }
    }

    private void cleanupTransientComponents(ExecutionJob job) {
        if (job.isExternal()) {
            try {
                WorkflowUtilities.deleteWorkflowNodes(job.getWorkflowId());
            } catch (Exception e) {
                logger.severe(String.format("Cannot remove nodes for workflow %d. Reason: %s", job.getWorkflowId(), e.getMessage()));
            }
            final List<ExecutionTask> tasks = job.getTasks();
            if (tasks != null) {
                for (ExecutionTask task : tasks) {
                    final TaoComponent component = TaskUtilities.getComponentFor(task);
                    if (component instanceof ProcessingComponent && ((ProcessingComponent) component).isTransient()) {
                        try {
                            processingComponentProvider.delete(component.getId());
                        } catch (Exception e) {
                            logger.severe(String.format("Cannot remove transient node %s or component %s. Reason: %s",
                                                        task.getWorkflowNodeId(), component.getId(), e.getMessage()));
                        }
                    }
                }
            }
        }
    }

    private void handleProcessingTask(ProcessingExecutionTask pcTask, boolean keepOutput) throws PersistenceException {
        List<TargetDescriptor> targets = pcTask.getComponent().getTargets();
        ExecutionGroup groupTask = pcTask.getGroupTask();
        boolean lastFromGroup = groupTask != null &&
                groupTask.getOutputParameterValues().stream()
                        .allMatch(o -> targets.stream().anyMatch(t -> t.getName().equals(o.getKey())));
        final Map<String, Variable> parameterValues = pcTask.getInputParameterValues().stream()
                                                          .collect(Collectors.toMap(Variable::getKey, Function.identity()));
        targets.forEach(t -> {
            Variable variable = parameterValues.get(t.getName());
            if (variable == null) {
                variable = pcTask.getOutputParameterValues().stream().filter(o -> o.getKey().equals(t.getName())).findFirst().orElse(null);
            }
            if (variable != null) {
                String targetOutput = variable.getValue();
                // if the output came from a remote note, we need to translate the node volumes to master volumes
                String instanceTargetOuptut = translateOutputToMasterPath(pcTask);
                if (targetOutput == null || instanceTargetOuptut == null) {
                    logger.severe(String.format("NULL TARGET [task %s, parameter %s]", pcTask.getId(), t.getName()));
                }
                if (!pcTask.getComponent().isOutputManaged()) {
                    // Since the component ignores what is given as target and creates its own output, we need to
                    // get what was produced and map it to the target output
                    instanceTargetOuptut = handleUnmanagedPath(instanceTargetOuptut);
                }
                pcTask.setOutputParameterValue(t.getName(), instanceTargetOuptut);
                logger.finest(String.format("Output [%s] of task [id=%d] was set to '%s'",
                                            t.getName(), pcTask.getId(), instanceTargetOuptut));
                if (keepOutput) {
                    persistOutputProducts(pcTask);
                }
                if (lastFromGroup) {
                    //groupTask.setOutputParameterValue(t.getName(), targetOutput);
                    pcTask.setOutputParameterValue(t.getName(), instanceTargetOuptut);
                }
            }
        });
        pcTask.setExecutionStatus(ExecutionStatus.DONE);
        taskProvider.update(pcTask);
        taskProvider.updateComponentTime(pcTask.getComponent().getId(),
                                         (int) Duration.between(pcTask.getStartTime(), pcTask.getEndTime()).getSeconds());
        ExecutionsManager.getInstance().doPostExecuteAction(pcTask);
        if (lastFromGroup) {
            taskProvider.update(groupTask);
        }
        logger.fine(String.format("Status change for task %s [job %s]: %s",
                                  pcTask.getComponent().getId(), pcTask.getJob().getName(), ExecutionStatus.DONE.name()));
    }

    /**
     * Maps the output of a task (as computed by the framework) to the actual output of the task.
     * Since we don't know what a task produces, we try to map the largest known raster (if a file) or otherwise return
     * what was the initial value.
     * @param source    What is currently assigned as task output
     */
    private String handleUnmanagedPath(String source) {
        final Path sourcePath = Paths.get(source);
        String result;
        if (Files.isRegularFile(sourcePath)) {
            result = sourcePath.toString();
        } else {
            try(Stream<Path> stream = Files.list(sourcePath)) {
                Path file = stream.filter(FileUtilities::isRaster)
                             .max((o1, o2) -> {
                                 try {
                                     return Long.compare(Files.size(o2), Files.size(o1));
                                 } catch (IOException e) {
                                     logger.warning(ExceptionUtils.getStackTrace(logger, e));
                                     return 0;
                                 }
                             }).orElse(null);
                result = file != null ? file.toString() : source;
            } catch (IOException e) {
                logger.warning(ExceptionUtils.getStackTrace(logger, e));
                result = source;
            }
        }
        return result;
    }

    /**
     * Propagates output descriptors of the task to the next one to be executed
     * @param task      The task that just finished
     * @param nextTask  The task that follows in execution
     */
    private void flowOutputs(ExecutionTask task, ExecutionTask nextTask) throws PersistenceException {
        final List<Variable> values = task.getOutputParameterValues();
        if (task instanceof DataSourceExecutionTask) {
            final int cardinality;
            // A DataSourceExecutionTask outputs the list of results as a JSON-ified string array
            Variable theOnlyValue = null;
            final List<String> valuesList;
            if (values != null && values.size() > 0) {
                theOnlyValue = values.get(0);
                valuesList = this.listAdapter.marshal(theOnlyValue.getValue());
            } else {
                valuesList = new ArrayList<>();
            }
            cardinality = valuesList.size();
            if (cardinality == 0) { // no point to continue since we have nothing to do
                changeStatus(nextTask, ExecutionStatus.CANCELLED, String.format("Task %s produced no results", TaskUtilities.getTaskName(task)));
            }
            for (int i = 0; i < cardinality; i++) {
                String file = valuesList.get(i);
                ProductHelper helper = null;
                try {
                    helper = ProductHelperFactory.getHelper(FileUtilities.toPath(file).getFileName().toString());
                } catch (Exception ignored) { }
                if (helper != null) {
                    valuesList.set(i, valuesList.get(i) + (valuesList.get(i).endsWith("/") ? "" : "/") + helper.getMetadataFileName());
                }
                valuesList.set(i, TaskUtilities.relativizePathForExecution(valuesList.get(i), nextTask));

            }
            if (nextTask instanceof ExecutionGroup) {
                ExecutionGroup groupTask = (ExecutionGroup) nextTask;
                groupTask.setStateHandler(new LoopStateHandler(new LoopState(cardinality, 1)));
                // we need to keep this mapping because ExecutionTask.stateHandler is transient
                this.groupStateHandlers.put(groupTask.getId(), groupTask.getStateHandler());
                if (theOnlyValue != null) {
                    groupTask.setInputParameterValue(groupTask.getInputParameterValues().get(0).getKey(),
                                                     theOnlyValue.getValue());
                }
                taskProvider.update(groupTask);
            } else if (!(nextTask instanceof DataSourceExecutionTask)){
                // if next to a DataSourceExecutionTask is a simple ExecutionTask, we feed it with as many
                // values as sources
                int expectedCardinality = TaskUtilities.getSourceCardinality(nextTask);
                if (expectedCardinality == -1) {
                    String message = String.format("Cannot determine input cardinality for task %s",
                                                   TaskUtilities.getTaskName(nextTask));
                    changeStatus(nextTask, ExecutionStatus.CANCELLED, message);
                }
                if (cardinality > 1 && expectedCardinality == 1) {
                    // Since the next component can handle only 1 input, we split the current job in as
                    // many jobs as the number of inputs. The current job remains the same, with only 1st input
                    try {
                        List<ExecutionJob> jobs = this.jobFactory.splitJob(task.getJob());
                        logger.fine(String.format("Job %s will fork additional %d jobs", task.getJob().getName(), jobs.size()));
                        //final String user = task.getContext().getPrincipal().getName();
                        final String user = task.getJob().getUserName();
                        final ExecutorService executorService = this.executors.get(user);
                        for (ExecutionJob job : jobs) {
                            if (canSubmitJob(user, job)) {
                                executorService.submit(() -> JobCommand.START.applyTo(job));
                            }
                        }
                    } catch (Exception e) {
                        logger.warning(ro.cs.tao.utils.ExceptionUtils.getStackTrace(logger, e));
                    }
                }
                final List<Variable> nextInputParameters = nextTask.getInputParameterValues();
                final Set<String> outParams = nextTask.getOutputParameterValues().stream().map(Variable::getKey).collect(Collectors.toSet());
                nextInputParameters.stream().filter(v -> !outParams.contains(v.getKey())).forEach(v -> resolveVariable(nextTask, v));
                if (expectedCardinality != 0) {
                    if (cardinality < expectedCardinality) {
                        String message = String.format("Insufficient inputs for task %s [expected %s, received %s]",
                                                       TaskUtilities.getTaskName(nextTask),
                                                       expectedCardinality, cardinality);
                        changeStatus(nextTask, ExecutionStatus.CANCELLED, message);
                    }
                    int idx = 0;
                    final Map<String, String> connectedInputs = TaskUtilities.getConnectedInputs(task, nextTask);
                    for (Variable var : nextInputParameters) {
                        if (connectedInputs.containsKey(var.getKey())) {
                            nextTask.setInputParameterValue(var.getKey(), valuesList.get(idx++));
                        }
                    }
                } else { // nextTask accepts a list
                    Map<String, String> connectedInputs = TaskUtilities.getConnectedInputs(task, nextTask);
                    if (theOnlyValue != null) {
                        for (Variable var : nextInputParameters) {
                            if (theOnlyValue.getKey().equals(connectedInputs.get(var.getKey()))) {
                                for (String value : valuesList) {
                                    nextTask.setInputParameterValue(var.getKey(), TaskUtilities.relativizePathForExecution(value, nextTask));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            Map<String, String> connectedInputs = TaskUtilities.getConnectedInputs(task, nextTask);
            final List<Variable> nextInputParameters = nextTask.getInputParameterValues();
            // Connected inputs are target->source mappings. Since, at this point, targets should have been resolved
            // to master paths, we need to "relativize" them for the next container execution
            for (Map.Entry<String, String> entry : connectedInputs.entrySet()) {
                nextTask.setInputParameterValue(entry.getKey(),
                                                TaskUtilities.relativizePathForExecution(values.stream().filter(v -> v.getKey().equals(entry.getValue()))
                                                                                               .findFirst().get().getValue(), nextTask));
            }
            final Set<String> outParams = nextTask.getOutputParameterValues().stream().map(Variable::getKey).collect(Collectors.toSet());
            nextInputParameters.stream().filter(v -> !outParams.contains(v.getKey())).forEach(v -> resolveVariable(nextTask, v));
        }
    }

    /**
     * An execution job is considered duplicate if: 1) it is created for the same workflow and 2) it has the same inputs
     * and 3) the previously created job is in one of the following states: UNDETERMINED, QUEUED_ACTIVE, RUNNING
     *
     * @param job    An existing job to check
     * @param inputs The inputs to be checked against
     * @return <code>true</code> if the given job doesn't match the inputs, <code>false</code> otherwise
     */
    private boolean checkExistingJob(ExecutionJob job, Map<String, String> inputs) {
        boolean existing = false;
        switch (job.getExecutionStatus()) {
            case UNDETERMINED:
            case QUEUED_ACTIVE:
            case RUNNING:
                List<ExecutionTask> tasks = job.orderedTasks();
                if (tasks != null && tasks.size() > 0) {
                    ExecutionTask first = tasks.get(0);
                    existing = first.getInputParameterValues().stream()
                            .allMatch(i -> inputs.containsKey(i.getKey()) && inputs.get(i.getKey()).equals(i.getValue()));
                }
                break;
            default:
                break;
        }
        return existing;
    }

    private void changeStatus(ExecutionTask task, ExecutionStatus newStatus, String reason) {
        try {
            if (task.getExecutionStatus() != newStatus) {
                task = taskProvider.updateStatus(task, newStatus, reason);
            }
            ExecutionGroup groupTask = task.getGroupTask();
            if (groupTask != null) {
                notifyTaskGroup(groupTask, task, reason);
            } else {
                notifyJob(task, reason);
            }
        } catch (Exception e) {
            logger.severe(ro.cs.tao.utils.ExceptionUtils.getStackTrace(logger, e));
        }
    }

    private void notifyTaskGroup(ExecutionGroup groupTask, ExecutionTask task, String reason) {
        final ExecutionStatus previousStatus = groupTask.getExecutionStatus();
        final List<ExecutionTask> tasks = groupTask.getTasks();
        final int taskCount = tasks.size();
        final ExecutionStatus taskStatus = task.getExecutionStatus();
        try {
            switch (taskStatus) {
                case QUEUED_ACTIVE:
                case RUNNING:
                    if (groupTask.getStartTime() == null) {
                        groupTask.setStartTime(task.getStartTime() != null ? task.getStartTime() : LocalDateTime.now());
                    }
                    groupTask.setExecutionStatus(taskStatus);
                    break;
                case SUSPENDED:
                case CANCELLED:
                case FAILED:
                    int startIndex = IntStream.range(0, taskCount)
                            .filter(i -> task.getId().equals(tasks.get(i).getId()))
                            .findFirst().orElse(0);
                    bulkSetStatus(tasks.subList(startIndex, taskCount), taskStatus);
                    if (TransitionBehavior.FAIL_ON_ERROR == workflowNodeProvider.get(task.getWorkflowNodeId()).getBehavior()) {
                        groupTask.setExecutionStatus(taskStatus);
                    } else {
                        if (groupTask.getStateHandler() == null) {
                            groupTask.setStateHandler(groupStateHandlers.get(groupTask.getId()));
                        }
                        LoopState nextState = (LoopState) groupTask.nextInternalState();
                        if (nextState != null) {
                            taskProvider.updateStatus(task, ExecutionStatus.UNDETERMINED, "Advance in loop");
                            bulkSetStatus(tasks, ExecutionStatus.UNDETERMINED);
                            groupTask.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                            try {
                                InternalStateHandler handler = groupTask.getStateHandler();
                                if (handler != null) {
                                    groupTask.setInternalState(handler.serializeState());
                                }
                            } catch (SerializationException e) {
                                e.printStackTrace();
                            }
                        } else {
                            groupTask.setExecutionStatus(ExecutionStatus.DONE);
                            groupTask.setEndTime(LocalDateTime.now());
                        }
                    }
                    break;
                case DONE:
                    // If the task is the last one executing of this group
                    if (tasks.stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
                        if (groupTask.getStateHandler() == null) {
                            groupTask.setStateHandler(groupStateHandlers.get(groupTask.getId()));
                        }
                        LoopState nextState = (LoopState) groupTask.nextInternalState();
                        if (nextState != null) {
                            taskProvider.updateStatus(task, ExecutionStatus.UNDETERMINED, "Advance in loop iteration");
                            bulkSetStatus(tasks, ExecutionStatus.UNDETERMINED);
                            groupTask.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                            try {
                                InternalStateHandler handler = groupTask.getStateHandler();
                                if (handler != null) {
                                    groupTask.setInternalState(handler.serializeState());
                                }
                            } catch (SerializationException e) {
                                e.printStackTrace();
                            }
                        } else {
                            groupTask.setExecutionStatus(ExecutionStatus.DONE);
                            groupTask.setEndTime(LocalDateTime.now());
                        }
                    }
                    break;
                default:
                    // do nothing for other states
                    break;
            }
            ExecutionStatus currentStatus = groupTask.getExecutionStatus();
            if (previousStatus != null && previousStatus != currentStatus) {
                taskProvider.update(groupTask);
                notifyJob(groupTask, reason);
            }
        } catch (PersistenceException pex) {
            logger.severe(pex.getMessage());
        }
    }

    private void notifyJob(ExecutionTask changedTask, String reason) {
        try {
            TaskStatusHandler.handle(changedTask, reason);
        } catch (PersistenceException pex) {
            logger.severe(pex.getMessage());
        }
    }

    private void stopTasks(List<ExecutionTask> tasks) {
        for (ExecutionTask task : tasks) {
            try {
                TaskCommand.STOP.applyTo(task);
            } catch (ExecutionException ex) {
                logger.severe(ExceptionUtils.getStackTrace(logger, ex));
            }
        }
    }

    private ExecutionJob checkJob(long jobId) {
        ExecutionJob job = jobProvider.get(jobId);
        if (job == null) {
            throw new ExecutionException(String.format("Job [id=%d] does not exist", jobId));
        }
        return job;
    }

    private void bulkSetStatus(Collection<ExecutionTask> tasks, ExecutionStatus status) {
        if (tasks == null) {
            return;
        }
        tasks.forEach(t -> {
            t.setExecutionStatus(status);
            try {
                taskProvider.updateStatus(t, status, "Bulk set");
            } catch (PersistenceException e) {
                logger.severe(e.getMessage());
            }
        });
    }

    private List<ExecutionTask> findNextTasks(ExecutionTask task) {
        ExecutionGroup groupTask = task instanceof ExecutionGroup ? (ExecutionGroup) task : task.getGroupTask();
        ExecutionJob job = jobProvider.get(task.getJob().getId()); // refresh object from DB
        if (groupTask != null) {
            if (groupTask.getExecutionStatus() != ExecutionStatus.DONE) {
                return this.groupTaskSelector.chooseNext(groupTask, task);
            }
            List<ExecutionTask> next = this.jobTaskSelector.chooseNext(job, groupTask);
            next.removeIf(n -> groupTask.getTasks().contains(n));
            return next;
        }
        return this.jobTaskSelector.chooseNext(job, task);
    }

    private List<DataSourceExecutionTask> findOriginatingTasks(ExecutionTask current) {
        /*ExecutionGroup groupTask = current.getGroupTask();
        if (groupTask != null) {
            return this.groupTaskSelector.findDataSourceTasks(groupTask, current);
        } else {
            return this.jobTaskSelector.findDataSourceTasks(current.getJob(), current);
        }*/
        return TaskUtilities.findDataSourceTasks(current.getJob());
    }

    private void resolveTargetName(ExecutionTask nextTask, Variable overriddenOutParam) {
        List<DataSourceExecutionTask> originatingTasks = findOriginatingTasks(nextTask);
        if (originatingTasks != null && !originatingTasks.isEmpty()) {
            boolean succeeded = false;
            String error = "";
            for (DataSourceExecutionTask originatingTask : originatingTasks) {
                String targetOutput = overriddenOutParam.getValue();
                String sensor = originatingTask.getComponent().getSensorName().replace("-", "");
                List<NamingRule> rules = namingRuleProvider.listBySensor(sensor);
                if (rules.size() > 0) {
                    NameExpressionParser parser = new NameExpressionParser(rules.get(0));
                    final List<Variable> oValues = originatingTask.getOutputParameterValues();
                    if (oValues != null && oValues.size() > 0) {
                        final List<String> oValuesList;
                        oValuesList = this.listAdapter.marshal(oValues.get(0).getValue());
                        final int size = oValuesList.size();
                        for (int i = 0; i < size; i++) {
                            try {
                                Path path = FileUtilities.isURI(oValuesList.get(i))
                                            ? Paths.get(new URI(oValuesList.get(i)))
                                            : Paths.get(oValuesList.get(i));
                                oValuesList.set(i, path.getName(path.getNameCount() - 1).toString());
                            } catch (URISyntaxException e) {
                                logger.warning(e.getMessage());
                            }
                        }
                        final String key = overriddenOutParam.getKey();
                        try {
                            nextTask.setOutputParameterValue(key, parser.resolve(targetOutput, oValuesList.toArray(new String[0])));
                            nextTask.getInputParameterValues().removeIf(v -> v.getKey().equals(key));
                            succeeded = true;
                        } catch (ParseException ex) {
                            error += ex.getMessage() + ";";
                        }
                    }
                }
            }
            if (!succeeded) {
                changeStatus(nextTask, ExecutionStatus.FAILED, error);
            }
        }
    }

    private void resolveVariable(ExecutionTask nextTask, Variable variable) {
        final String value = variable.getValue();
        if (value != null && value.contains("${")) {
            List<DataSourceExecutionTask> originatingTasks = findOriginatingTasks(nextTask);
            if (originatingTasks != null && !originatingTasks.isEmpty()) {
                boolean succeeded = false;
                String error = "";
                for (DataSourceExecutionTask originatingTask : originatingTasks) {
                    String sensor = originatingTask.getComponent().getSensorName().replace("-", "");
                    List<NamingRule> rules = namingRuleProvider.listBySensor(sensor);
                    if (rules.size() > 0) {
                        NameExpressionParser parser = new NameExpressionParser(rules.get(0));
                        final List<Variable> oValues = originatingTask.getOutputParameterValues();
                        if (oValues != null && oValues.size() > 0) {
                            final List<String> oValuesList;
                            oValuesList = this.listAdapter.marshal(oValues.get(0).getValue());
                            final int size = oValuesList.size();
                            for (int i = 0; i < size; i++) {
                                try {
                                    Path path = Paths.get(new URI(oValuesList.get(i)));
                                    oValuesList.set(i, path.getName(path.getNameCount() - 1).toString());
                                } catch (URISyntaxException e) {
                                    logger.warning(e.getMessage());
                                }
                            }
                            try {
                                variable.setValue(parser.resolve(value, oValuesList.toArray(new String[0])));
                                succeeded = true;
                            } catch (ParseException ex) {
                                error += ex.getMessage() + ";";
                            }
                        }
                    }
                }
                if (!succeeded) {
                    changeStatus(nextTask, ExecutionStatus.FAILED, error);
                }
            }
        }
    }

    private void persistOutputProducts(ExecutionTask task) {
        try {
            if (ExecutionConfiguration.developmentModeEnabled()) {
                return;
            }
            List<Variable> values = task.getOutputParameterValues();
            WorkflowNodeDescriptor node = workflowNodeProvider.get(task.getWorkflowNodeId());
            TaoComponent component;
            if (node instanceof WorkflowNodeGroupDescriptor) {
                component = groupComponentProvider.get(node.getComponentId());
            } else {
                component = processingComponentProvider.get(node.getComponentId());
            }
            List<EOProduct> products = new ArrayList<>();
            for (Variable value : values) {
                products.addAll(createProducts(component, value, task.getContext()));
            }
            OutputDataHandlerManager.getInstance().applyHandlers(products);
        } catch (Exception ex) {
            logger.severe(String.format("Error persisting products: %s", ex.getMessage()));
        }

    }

    private List<EOProduct> createProducts(TaoComponent component, Variable outParam, SessionContext context) {
        List<EOProduct> products = new ArrayList<>();
        List<TargetDescriptor> targets = component.getTargets();
        String value = outParam.getValue();
        TargetDescriptor descriptor = targets.stream()
                .filter(t -> t.getName().equals(outParam.getKey()))
                .findFirst()
                .orElse(null);
        if (value != null && descriptor != null) {
            try {
                // first try to see if it's a list
                List<String> list = this.listAdapter.marshal(value);
                for (String v : list) {
                    products.add(createProduct(descriptor, v, context));
                }
            } catch (Exception e) {
                products.add(createProduct(descriptor, value, context));
            }
        }
        return products;
    }

    private EOProduct createProduct(TargetDescriptor descriptor, String outValue, SessionContext context) {
        EOProduct product = null;
        try {
            if (descriptor.getDataDescriptor().getFormatType() != DataFormat.VECTOR) {
                //String netPath = FileUtilities.asUnixPath(context.getNetSpace(), true);
                DockerVolumeMap masterMap = ExecutionConfiguration.getMasterContainerVolumeMap();
                DockerVolumeMap workerMap = ExecutionConfiguration.getWorkerContainerVolumeMap();
                Path path;
                String strPath;
                if (outValue.startsWith(SystemVariable.ROOT.value().replace("\\", "/"))) {
                    strPath = outValue;
                } else {
                    if (outValue.startsWith(masterMap.getContainerWorkspaceFolder())) {
                        strPath = outValue.replace(masterMap.getContainerWorkspaceFolder(), masterMap.getHostWorkspaceFolder());
                    } else {
                        strPath = outValue.replace(workerMap.getContainerWorkspaceFolder(), masterMap.getHostWorkspaceFolder());
                    }
                    if (strPath.endsWith("/")) {
                        strPath = strPath.substring(0, strPath.length() - 2);
                    }
                    if (strPath.charAt(2) == '/') {
                        // This is a windows path (eg. /D/something)
                        strPath = strPath.substring(2);
                    }
                }
                path = Paths.get(strPath).toAbsolutePath();
                MetadataInspector metadataInspector = this.metadataServices.stream()
                        .filter(s -> s.decodeQualification(path) == DecodeStatus.INTENDED)
                        .findFirst().orElse(null);
                if (metadataInspector == null) {
                    metadataInspector = this.metadataServices.stream()
                            .filter(s -> s.decodeQualification(path) == DecodeStatus.SUITABLE)
                            .findFirst().orElse(null);
                }
                if (metadataInspector != null) {
                    logger.fine("Creating metadata for " + path);
                    MetadataInspector.Metadata metadata = metadataInspector.getMetadata(path);
                    if (metadata != null) {
                        product = metadata.toProductDescriptor(path);
                        product.addReference(context.getPrincipal().getName());
                        product.setVisibility(Visibility.PRIVATE);
                        product.setAcquisitionDate(LocalDateTime.now());
                    } else {
                        throw new ExecutionException(String.format("Metadata for output %s could not be extracted", outValue));
                    }
                } else {
                    throw new ExecutionException(String.format("No suitable metadata inspector found for output %s", outValue));
                }
            }
        } catch (Exception e2) {
            logger.severe(ExceptionUtils.getStackTrace(logger, e2));
            logger.severe("If this was caused by 'gdalinfo', please check that the Docker daemon is not blocked by a firewall");
        }
        return product;
    }
    
    /**
     * Check if the user still has disk processing quota available
     * 
     * @param user the user for which the interogation is made
     * 
     * @return true if the user stil has disk processing quota available, false otherwise. 
     * @throws QuotaException if the operation fails. 
     */
    private boolean checkProcessingQuota(Principal user) throws QuotaException {
    	// update the quota before checking
        UserQuotaManager.getInstance().updateUserProcessingQuota(user);
        
        return UserQuotaManager.getInstance().checkUserProcessingQuota(user);
    }

    private boolean canSubmitJob(String user, ExecutionJob job) {
        final List<ExecutionJob> runnableJobs = this.jobProvider.list(user, new HashSet<>() {{
            add(ExecutionStatus.QUEUED_ACTIVE);
            add(ExecutionStatus.RUNNING);
        }});
        final int limit = Math.min(Integer.parseInt(ConfigurationManager.getInstance().getValue(USER_JOB_LIMIT_KEY, "1")),
                                   NodeManager.getInstance().getActiveNodes());
        if (runnableJobs != null && (limit != 0 && runnableJobs.size() > limit - 1)) {
            if (!this.waitingJobs.containsKey(user)) {
                this.waitingJobs.put(user, new ArrayList<>());
            }
            this.waitingJobs.get(user).add(job.getId());
            logger.warning("Job " + job.getId() + " was put in the waiting list");
            return false;
        }
        return true;
    }

    private ExecutionJob tryCreateJob(String user, SessionContext context, long workflowId, String description,
                                      Map<String, Map<String, String>> inputs) {
        final ExecutionJob executionJob = createJob(context, workflowId, description, inputs,
                                                    ConfigurationManager.getInstance().getBooleanValue("optimize.workflows"));
        return canSubmitJob(user, executionJob) ? executionJob : null;
    }

    private ExecutionJob tryCreateJob(String user, String wpsId, Map<String, Map<String, String>> inputs) {
        final ExecutionJob executionJob = createJob(wpsId, inputs);
        return canSubmitJob(user, executionJob) ? executionJob : null;
    }

    private String translateOutputToMasterPath(ProcessingExecutionTask task) {
        String path = task.getInstanceTargetOutput();
        final DockerVolumeMap workerVolumeMap = ExecutionConfiguration.getWorkerContainerVolumeMap();
        String outPath = path.replace(workerVolumeMap.getContainerWorkspaceFolder(),
                                      SystemVariable.ROOT.value().replace("\\", "/"));
        if (!task.getComponent().isOutputManaged()) {
            DataFormat type = task.getComponent().getTargets().get(0).getDataDescriptor().getFormatType();
            switch (type) {
                case RASTER:
                case VECTOR:
                case FOLDER:
                    Path taskFolder = Paths.get(outPath).getParent();
                    try (Stream<Path> list = Files.list(taskFolder)) {
                        Path producedPath = list.findFirst().orElse(null);
                        if (producedPath != null) {
                            outPath = outPath.substring(0, outPath.lastIndexOf('/') + 1) + producedPath.getFileName();
                        } else {
                            outPath = null;
                        }
                    } catch (IOException e) {
                        logger.severe(ExceptionUtils.getStackTrace(logger, e));
                    }
                    break;
                default:
                    outPath = null;
                    break;
            }
        }
        return outPath;
    }

    private class MessageContext extends SessionContext {
        private final Principal impersonatingUser;

        private MessageContext(Message message) {
            this.impersonatingUser = new UserPrincipal(message.getUser());
        }

        private MessageContext(String userName) {
            this.impersonatingUser = new UserPrincipal(userName);
        }

        @Override
        public Principal getPrincipal() { return this.impersonatingUser; }

        @Override
        public Principal setPrincipal(Principal principal) {
            return this.impersonatingUser;
        }

        @Override
        protected List<UserPreference> setPreferences() {
            Principal principal = getPrincipal();
            return userProvider != null && principal != null ? userProvider.listPreferences(getPrincipal().getName()) : null;
        }

        @Override
        public int hashCode() {
            return getPrincipal() != null ? getPrincipal().getName().hashCode() : super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SessionContext)) {
                return false;
            }
            SessionContext other = (SessionContext) obj;
            return (this.getPrincipal() == null && other.getPrincipal() == null) ||
                    (this.getPrincipal().getName().equals(other.getPrincipal().getName()));
        }
    }

    private class OrchestratorWorker extends Thread {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    AbstractMap.SimpleEntry<Message, SessionContext> entry = queue.take();
                    logger.finest("Dequeued message " + entry.getKey());
                    String user = entry.getValue().getPrincipal().getName();
                    ExecutorService service = Orchestrator.this.executors.get(user);
                    if (service == null) {
                        service = Executors.newFixedThreadPool(1);
                        Orchestrator.this.executors.put(user, service);
                    }
                    if (service.isShutdown() || service.isTerminated()) {
                        logger.warning("Executor for " + entry.getKey() + " was shutdown. Further tasks cannot be processed");
                    } else {
                        service.submit(() -> processMessage(entry.getKey(), entry.getValue()));
                    }
                } catch (InterruptedException e) {
                    logger.severe(e.getMessage());
                }
            }
        }
    }
}
