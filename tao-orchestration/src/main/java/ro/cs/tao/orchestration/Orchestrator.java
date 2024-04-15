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
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.ProductHelperFactory;
import ro.cs.tao.docker.DockerVolumeMap;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.drmaa.Environment;
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
import ro.cs.tao.execution.JobCompletedListener;
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
import ro.cs.tao.orchestration.queue.JobQueue;
import ro.cs.tao.orchestration.status.TaskStatusHandler;
import ro.cs.tao.persistence.*;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.services.bridge.spring.SpringContextBridgedServices;
import ro.cs.tao.services.utils.WorkflowUtilities;
import ro.cs.tao.spi.OutputDataHandlerManager;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.TopologyManager;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.utils.*;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.io.IOException;
import java.net.Inet4Address;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final Set<ExecutionStatus> notFurtherRunnableStatuses = EnumSet.of(ExecutionStatus.DONE,
                                                                                      ExecutionStatus.CANCELLED,
                                                                                      ExecutionStatus.FAILED);
    private static final Set<ExecutionStatus> completedOkStatuses = EnumSet.of(ExecutionStatus.DONE,
                                                                               ExecutionStatus.PENDING_FINALISATION);
    private static final Orchestrator instance;
    private final Map<Long, InternalStateHandler<?>> groupStateHandlers;
    private final BlockingQueue<AbstractMap.SimpleEntry<Message, SessionContext>> messageQueue;

    static {
        instance = new Orchestrator();
    }

    public static Orchestrator getInstance() {
        return instance;
    }

    private final StringListAdapter listAdapter;
    private final Logger logger = Logger.getLogger(Orchestrator.class.getName());
    private final JobQueue jobQueue;
    private final JobQueueWorker jobQueueWorker;
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
    private final ExecutorService messageWorker = Executors.newFixedThreadPool(1);
    private final Set<Long> runningTasks;
    private final AtomicInteger activeJobs;
    private final List<JobCompletedListener> jobListeners;

    private String uuid;
    private final Object lock;

    private Orchestrator() {
        this.lock = new Object();
        this.groupStateHandlers = new HashMap<>();
        this.messageQueue = new LinkedBlockingDeque<>();
        this.runningTasks = Collections.synchronizedSet(new HashSet<>());
        this.listAdapter = new StringListAdapter();
        final SpringContextBridgedServices bridgedServices = SpringContextBridge.services();
        this.jobProvider = bridgedServices.getService(ExecutionJobProvider.class);
        this.taskProvider = bridgedServices.getService(ExecutionTaskProvider.class);
        this.workflowProvider = bridgedServices.getService(WorkflowProvider.class);
        this.workflowNodeProvider = bridgedServices.getService(WorkflowNodeProvider.class);
        this.processingComponentProvider = bridgedServices.getService(ProcessingComponentProvider.class);
        this.groupComponentProvider = bridgedServices.getService(GroupComponentProvider.class);
        this.namingRuleProvider = bridgedServices.getService(NamingRuleProvider.class);
        this.userProvider = bridgedServices.getService(UserProvider.class);
        this.jobQueue = new DefaultJobQueue(this.jobProvider);
        this.jobQueueWorker = new JobQueueWorker(this.jobQueue);
        this.jobQueueWorker.setOrchestrator(this);
        this.activeJobs = new AtomicInteger(0);
        this.jobListeners = new ArrayList<>();
        addJobListener(new JobListener(this));
        try {
            final NodeDescription masterNode = bridgedServices.getService(NodeDBProvider.class).get(Inet4Address.getLocalHost().getHostName());
            this.uuid = masterNode.getAppId();
        } catch (Exception e) {
            logger.warning("The master node appId could not be found. A temporary appId will be created for the lifetime of this instance");
            this.uuid = UUID.randomUUID().toString();
        }
        subscribe(Topic.EXECUTION.value(), Topic.SYSTEM.value());
    }

    public String getId() {
        return uuid;
    }

    public void start() {
        final Function<Long, WorkflowNodeDescriptor> workflowProvider = this.workflowNodeProvider::get;
        initializeJobSelector(workflowProvider, this.taskProvider::getByJobAndNode);
        initializeGroupSelector(workflowProvider, this.taskProvider::getByGroupAndNode);
        TaskStatusHandler.registerHandlers(jobProvider, taskProvider);
        this.jobFactory = new DefaultJobFactory();
        this.metadataServices = ServiceRegistryManager.getInstance().getServiceRegistry(MetadataInspector.class).getServices();
        OrchestratorWorker monitor = new OrchestratorWorker();
        monitor.setName("orchestrator");
        monitor.start();
        this.jobQueue.initialize();
        this.jobQueueWorker.start();
        logger.fine("Orchestration service initialized");
    }

    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param context     The session context
     * @param request   The workflow execution request
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public ExecutionJob startWorkflow(SessionContext context, ExecutionRequest request) throws ExecutionException {
        try {
            final String user = context.getPrincipal().getName();
            final List<ExecutionJob> jobs = new ArrayList<>();
            
        	final ExecutionJob job = tryCreateJob(user, context, request);
            if (job == null) {
                throw new ExecutionException("Another job is running or queued for user '" + user + "'. New job will be queued");
            }
            jobs.add(job);
            
            // check if the data sources for the job contains product sets or queries
            final List<DataSourceExecutionTask> dataTasks = job.getTasks().stream().filter(t -> t instanceof DataSourceExecutionTask)
                    .map(t -> (DataSourceExecutionTask) t).collect(Collectors.toList());

            final long productSetTasks = dataTasks.stream().filter(dt -> dt.getComponent().getId().startsWith("product-set-")).count();
            if (productSetTasks > 0) {
            	// try to split the job
            	jobs.addAll(jobFactory.splitJobBeforeExecution(job)); 
            }
            
            // add jobs for processing
            jobs.stream().forEach(j -> this.jobQueue.put(j));
            
            return job;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e instanceof ExecutionException ? (ExecutionException) e : new ExecutionException(e.getMessage());
        }
    }
    /**
     * Creates a job for a WPS service invocation.
     *
     * @param context   The session context
     * @param wpsId     The WPS identifier
     * @param inputs    The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public ExecutionJob invokeWPS(SessionContext context, String wpsId,
                                  Map<String, Map<String, String>> inputs) throws ExecutionException {
        try {
            final String user = context.getPrincipal().getName();
            final ExecutionJob executionJob = tryCreateWPSJob(user, wpsId, inputs);
            if (executionJob == null) {
                throw new ExecutionException("Another WPS job is running or queued for user '" + user + "'. New job will be queued");
            } else {
                this.jobQueue.put(executionJob);
            }
            return executionJob;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e instanceof ExecutionException ? (ExecutionException) e : new ExecutionException(e.getMessage());
        }
    }

    /**
     * Creates a job for a WPS service invocation.
     *
     * @param context   The session context
     * @param wmsId     The WMS identifier
     * @param inputs    The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public ExecutionJob invokeWMS(SessionContext context, String wmsId,
                                  Map<String, String> inputs) throws ExecutionException {
        try {
            final String user = context.getPrincipal().getName();
            final ExecutionJob executionJob = tryCreateWMSJob(user, wmsId, inputs);
            if (executionJob == null) {
                throw new ExecutionException("Another WMS job is running or queued for user '" + user + "'. New job will be queued");
            } else {
                this.jobQueue.put(executionJob);
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
     * @param context      The session context
     * @param workflowId The workflow identifier
     * @param inputs     The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public ExecutionJob startExternalWorkflow(SessionContext context, long workflowId, String description,
                                              Map<String, Map<String, String>> inputs,
                                              EndpointDescriptor callback) throws ExecutionException {
        ExecutionJob executionJob;
        try {
            final ExecutionRequest request = new ExecutionRequest();
            request.setWorkflowId(workflowId);
            request.setName(description);
            request.setParameters(inputs);
            executionJob = startWorkflow(context, request);
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
     * @param context      The session context
     * @param workflowId The workflow identifier
     * @param inputs     The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong
     */
    public ExecutionJob createJob(SessionContext context, long workflowId, String description,
                                  Map<String, Map<String, String>> inputs, JobType jobType)  throws ExecutionException {
        try {
            final WorkflowDescriptor descriptor = workflowProvider.get(workflowId);
            if (descriptor == null) {
                throw new ExecutionException(String.format("Non-existent workflow [%s]", workflowId));
            }
            final ExecutionJob job = new ScriptJobFactory().createJob(context, this.uuid, description, descriptor,
                                                                      inputs, jobType, Environment.DEFAULT);
            this.jobQueue.put(job);
            return job;
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Creates a job from a workflow, intended for execution.
     *
     * @param context      The session context
     * @param request    The execution request
     * @param optimize  If <code>true</code>, the workflow will be optimized if possible
     * @throws ExecutionException In case anything goes wrong
     */
    public ExecutionJob createJob(SessionContext context, ExecutionRequest request, boolean optimize)  throws ExecutionException {
        try {
            final WorkflowDescriptor descriptor = workflowProvider.get(request.getWorkflowId());
            if (descriptor == null) {
                throw new ExecutionException(String.format("Non-existent workflow [%s]", request.getWorkflowId()));
            }
            final Principal principal = context.getPrincipal();
            // check if the user's disk quota is not reached
            if (!checkProcessingQuota(principal)) {
                // do not create any job
                throw new ExecutionException("Quota exceeded for user " + principal.getName());
            }
            WorkflowDescriptor optimized = descriptor;
            if (optimize) {
                optimized = WorkflowOptimizer.getOptimizedWorkflow(descriptor, request.getParameters());
                if (optimized == null) {
                    optimized = descriptor;
                    optimized.setActive(descriptor.isActive());
                }
            }
            final Environment env = request.getEnvironment() != null
                                    ? Environment.valueOf(request.getEnvironment())
                                    : Environment.DEFAULT;
            return this.jobFactory.createJob(context, this.uuid, request.getName(), optimized,
                                             request.getParameters(), 
                                             request.getJobType() == null ? JobType.EXECUTION : JobType.valueOf(request.getJobType()),
                                             env);
        } catch (QuotaException | PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Creates a list of jobs from a workflow, intended for execution.
     *
     * @param context      The session context
     * @param request    The execution request
     * @param optimize  If <code>true</code>, the workflow will be optimized if possible
     * @throws ExecutionException In case anything goes wrong
     */
    public List<ExecutionJob> createJobs(SessionContext context, ExecutionRequest request, boolean optimize)  throws ExecutionException {
        try {
        	// create the job as before. 
        	final ExecutionJob mainJob = createJob(context, request, optimize);
        	if (mainJob == null) {
        		return null;
        	}
        	// split the job into sub-jobs
        	final List<ExecutionJob> jobsList = this.jobFactory.splitJobBeforeExecution(mainJob);

        	if (jobsList == null) {
        		return null;
        	}
        	// add the main job to the list
        	jobsList.add(0, mainJob);
        	
        	// return list
        	return jobsList;
        } catch (CloneNotSupportedException | PersistenceException e) {
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
    public ExecutionJob createWPSJob(String wpsId, Map<String, Map<String, String>> inputs)  throws ExecutionException {
        try {
            return this.jobFactory.createWPSJob(SessionStore.currentContext(), this.uuid, wpsId, inputs);
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Creates a job for a WMS, intended for execution.
     *
     * @param wmsId     The WMS identifier
     * @param inputs     The overridden parameter values,
     * @throws ExecutionException In case anything goes wrong
     */
    public ExecutionJob createWMSJob(String wmsId, Map<String, String> inputs)  throws ExecutionException {
        try {
            return this.jobFactory.createWMSJob(SessionStore.currentContext(), this.uuid, wmsId, inputs);
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
        final ExecutionStatus status = job.getExecutionStatus();
        try {
            this.jobQueue.removeJob(jobId);
            JobCommand.STOP.applyTo(job);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        } finally {
            if (status == ExecutionStatus.QUEUED_ACTIVE ||
                status == ExecutionStatus.RUNNING ||
                status == ExecutionStatus.PENDING_FINALISATION ||
                status == ExecutionStatus.SUSPENDED) {
                notifyJobListeners(job);
            }
        }
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

    int getActiveJobsCount() {
        return jobProvider.count(ExecutionStatus.RUNNING);
    }

    int getActiveJobsCount(String userId) {
        return jobProvider.count(userId, ExecutionStatus.RUNNING);
    }

    int getMaximumAllowedJobs() {
        final boolean shouldPoolNodes = !ConfigurationManager.getInstance().getBooleanValue("topology.dedicated.user.nodes");
        int nodeUserLimit = Integer.parseInt(ConfigurationManager.getInstance().getValue("topology.node.user.limit", "1"));
        return shouldPoolNodes ? NodeManager.getInstance().getActiveNodes() * 2 : (int) userProvider.count();
    }

    public int purgeJobs(String user) {
        final List<Long> jobIds = this.jobQueue.removeUserJobs(user);
        int purged = 0;
        if (jobIds != null) {
            jobIds.forEach(this::stopJob);
            purged = jobIds.size();
        }
        List<ExecutionJob> jobs = jobProvider.list(user,EnumSet.of(ExecutionStatus.UNDETERMINED,
                                                                   ExecutionStatus.QUEUED_ACTIVE,
                                                                   ExecutionStatus.RUNNING));
        if (jobs != null) {
            jobs.forEach(j -> stopJob(j.getId()));
            purged += jobs.size();
        }
        return purged;
    }

    @Override
    protected void onMessageReceived(Message message) {
        if (message instanceof StartupCompletedMessage) {
            final boolean resumeJobs = ConfigurationManager.getInstance().getBooleanValue("resume.jobs.at.startup");
            if (resumeJobs) {
                final List<ExecutionTask> tasks = prepareStalledTasks();
                if (tasks != null) {
                    for (ExecutionTask task : tasks) {
                        logger.fine(String.format("Resuming task [id=%s] from job [id=%d]",
                                                  task.getId(), task.getJob().getId()));
                        try {
                            TaskCommand.START.applyTo(task);
                        } catch (ExecutionException e) {
                            logger.severe(e.getMessage());
                        }
                    }
                }
            }
        } else {
            try {
                final SessionContext context = new MessageContext(message);
                messageQueue.put(new AbstractMap.SimpleEntry<>(message, context));
            } catch (InterruptedException e) {
                logger.severe(e.getMessage());
            }
        }
    }

    private List<ExecutionTask> prepareStalledTasks() {
        List<ExecutionTask> runningTasks = null;
        List<ExecutionJob> jobs = jobProvider.list(ExecutionStatus.RUNNING);
        if (jobs != null) {
            for (ExecutionJob job : jobs) {
                runningTasks = job.getTasks().stream()
                                                      .filter(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING)
                                                      .collect(Collectors.toList());
                for (ExecutionTask t : runningTasks) {
                    try {
                        t = taskProvider.updateStatus(t, ExecutionStatus.QUEUED_ACTIVE, "Stalled job");
                    } catch (PersistenceException e) {
                        logger.severe(String.format("Cannot reset status for task %s [job %s]",
                                                    t.getId(), job.getId()));
                    }
                }
            }
        }
        return runningTasks;
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
        final String taskId = message.getItem(Message.SOURCE_KEY);
        String payload = message.getPayload();
        if (StringUtilities.isNullOrEmpty(payload)) {
            return;
        }
        final ExecutionStatus status = EnumUtils.getEnumConstantByName(ExecutionStatus.class, payload);
        final ExecutionTask task = taskProvider.get(Long.parseLong(taskId));
        final String user = task.getJob().getUserId();
        try {
            currentContext.setPrincipal(new UserPrincipal(user));
            task.setContext(currentContext);
            if (!(status == ExecutionStatus.QUEUED_ACTIVE && task.getExecutionStatus() == ExecutionStatus.RUNNING)) {
                // it may happen that the notification of Queue_Active to arrive after the task was actually put
                // in Running state, hence we don't need to update the status
                changeStatus(task, status, message.getMessage());
            }
            // A processing task can be successively in PENDING_FINALIZATION and DONE states, while a datasource task
            // can only transit to DONE (from RUNNING).
            // Hence, for a processing task, we don't want to process twice the DONE state, as it will recurse.
            if (completedOkStatuses.contains(status) && notFurtherRunnableStatuses.contains(status)
                    && task instanceof ProcessingExecutionTask && !(task instanceof ScriptTask)) {
                return;
            }
            if (notFurtherRunnableStatuses.contains(status)) {
                this.runningTasks.remove(task.getId());
                if (task instanceof ProcessingExecutionTask) {
                    ExecutionsManager.getInstance().doPostExecuteAction(task);
                }
                logger.finest(String.format("Active or pending tasks: %d", this.runningTasks.size()));
            }
            if (completedOkStatuses.contains(status)) {
                // Current task has completed somehow
                final WorkflowNodeDescriptor taskNode = workflowNodeProvider.get(task.getWorkflowNodeId());
                // For DataSourceExecutionTask, it is the Query executor that sets the outputs.
                // Hence, we need to "confirm" here the outputs of a processing task.
                if (task instanceof ProcessingExecutionTask && !(task instanceof ScriptTask)) {
                    handleProcessingTask((ProcessingExecutionTask) task, taskNode == null || taskNode.getPreserveOutput());
                }
                // Identify next tasks to be executed
                final List<ExecutionTask> nextTasks = findNextTasks(task);
                if (nextTasks != null && !nextTasks.isEmpty()) {
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
                                        for (ExecutionJob job : jobs) {
                                            this.jobQueue.put(job);
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
                        if (JobType.EXECUTION.equals(job.getJobType()) &&
                            taskNode instanceof WorkflowNodeGroupDescriptor && taskNode.getPreserveOutput()) {
                            persistOutputProducts(task);
                        }
                        WorkflowDescriptor workflow = workflowProvider.get(job.getWorkflowId());
                        Duration time = null;
                        if (job.getStartTime() != null && job.getEndTime() != null) {
                            time = Duration.between(job.getStartTime(), job.getEndTime());
                        }
                        notifyJobListeners(job);
                        tryRestoreSymlinks(job);
                        tryRemoveTemporaryWorkflow(job);
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
                if (status == ExecutionStatus.FAILED) {
                    if (TaskUtilities.getTransitionBehavior(task) == TransitionBehavior.FAIL_ON_ERROR) {
                        cancelJob(job);
                    } else {
                        // Only cancel child tasks
                        final List<ExecutionTask> nextTasks = findNextTasks(task);
                        if (nextTasks != null) {
                            for (ExecutionTask nextTask : nextTasks) {
                                changeStatus(nextTask, ExecutionStatus.CANCELLED, "Parent task " + task.getId() + " failed");
                            }
                        }
                    }
                }
                if (TaskUtilities.haveAllTasksCompleted(job)) {
                    tryRemoveTemporaryWorkflow(job);
                }
            }
        } catch (Throwable e) {
            logger.severe(String.format("Abnormal termination: %s", ExceptionUtils.getStackTrace(logger, e)));
            ExecutionJob job = task.getJob();
            job.setExecutionStatus(ExecutionStatus.FAILED);
            try {
                cancelJob(job);
            } catch (PersistenceException e1) {
                logger.severe(String.format("Abnormal termination: %s", ExceptionUtils.getStackTrace(logger, e1)));
            }
        }
    }

    public void cancelJob(ExecutionJob job) throws PersistenceException {
        try {
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
            tryRestoreSymlinks(job);
            cleanupTransientComponents(job);
        } finally {
            notifyJobListeners(job);
        }
    }

    public void queueJob(ExecutionJob job) {
        if (this.jobQueue.getAllJobs().stream().noneMatch(t -> t.getKeyOne().equals(job.getId()))) {
            this.jobQueue.put(job);
        } else {
            throw new ExecutionException("Job already in queue");
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
        final JobType jobType = pcTask.getJob().getJobType();
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
                    final String variableName = variable.getKey();
                    instanceTargetOuptut = handleUnmanagedPath(instanceTargetOuptut,
                                                               pcTask.getComponent()
                                                                      .getTargets().stream()
                                                                                   .filter(tr -> tr.getName().equalsIgnoreCase(variableName))
                                                                       .findFirst().get().getDataDescriptor().getFormatType() != DataFormat.FOLDER);
                }
                pcTask.setOutputParameterValue(t.getName(), instanceTargetOuptut);
                logger.finest(String.format("Output [%s] of task [id=%d] was set to '%s'",
                                            t.getName(), pcTask.getId(), instanceTargetOuptut));
                if (keepOutput && JobType.EXECUTION.equals(jobType)) {
                    persistOutputProducts(pcTask);
                } else if (JobType.EXECUTION != jobType) {
                    // cleanup empty folders created by simulated execution
                    Path jobPath = Paths.get(pcTask.getJob().getJobOutputPath());
                    try {
                        List<Path> folders = FileUtilities.listFolders(jobPath);
                        for (Path folder : folders) {
                            FileUtilities.deleteTree(folder);
                        }
                    } catch (IOException e) {
                        logger.warning(e.getMessage());
                    }
                }
                if (lastFromGroup) {
                    //groupTask.setOutputParameterValue(t.getName(), targetOutput);
                    pcTask.setOutputParameterValue(t.getName(), instanceTargetOuptut);
                }
            }
        });
        pcTask.setExecutionStatus(ExecutionStatus.DONE);
        taskProvider.update(pcTask);
        if (JobType.EXECUTION.equals(jobType)) {
            taskProvider.updateComponentTime(pcTask.getComponent().getId(),
                                             (int) Duration.between(pcTask.getStartTime(), pcTask.getEndTime()).getSeconds());
        }
        ExecutionsManager.getInstance().doPostExecuteAction(pcTask);
        if (lastFromGroup) {
            taskProvider.update(groupTask);
        }
        TaskStatusHandler.handle(pcTask, null);
    }

    /**
     * Maps the output of a task (as computed by the framework) to the actual output of the task.
     * Since we don't know what a task produces, we try to map the largest known raster (if a file) or otherwise return
     * what was the initial value.
     * @param source    What is currently assigned as task output
     */
    private String handleUnmanagedPath(String source, boolean isFile) {
        final Path sourcePath = Paths.get(source);
        String result;
        final Path inspectionPath = sourcePath.getParent();
        try(Stream<Path> stream = Files.list(inspectionPath)) {
            Path file = isFile
                        ? stream.filter(FileUtilities::isRaster)
                                .max((o1, o2) -> {
                                      try {
                                          return Long.compare(Files.size(o2), Files.size(o1));
                                      } catch (IOException e) {
                                          logger.warning(ExceptionUtils.getStackTrace(logger, e));
                                          return 0;
                                      }
                                }).orElse(null)
                        : stream.filter(p -> Files.isDirectory(p)).findFirst().orElse(null);
            result = file != null ? file.toString() : source;
        } catch (IOException e) {
            logger.warning(ExceptionUtils.getStackTrace(logger, e));
            result = source;
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
            if (values != null && !values.isEmpty()) {
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
                if (expectedCardinality < -1) { // allow also -1, it represents no input required for the task
                    String message = String.format("Cannot determine input cardinality for task %s",
                                                   TaskUtilities.getTaskName(nextTask));
                    changeStatus(nextTask, ExecutionStatus.CANCELLED, message);
                }
                if (cardinality > 1 && expectedCardinality == 1) {
                    // Since the next component can handle only 1 input, we split the current job in as
                    // many jobs as the number of inputs. The current job remains the same, with only 1st input
                    try {
                        final List<ExecutionJob> jobs = this.jobFactory.splitJob(task.getJob());
                        logger.fine(String.format("Job %s will fork additional %d jobs", task.getJob().getName(), jobs.size()));
                        for (ExecutionJob job : jobs) {
                            this.jobQueue.put(job);
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
                if (tasks != null && !tasks.isEmpty()) {
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
            if (task.getExecutionStatus() != newStatus || task instanceof DataSourceExecutionTask) {
                task = taskProvider.updateStatus(task, newStatus, reason);
                ExecutionGroup groupTask = task.getGroupTask();
                if (groupTask != null) {
                    notifyTaskGroup(groupTask, task, reason);
                } else {
                    notifyJob(task, reason);
                }
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
                if (!rules.isEmpty()) {
                    NameExpressionParser parser = new NameExpressionParser(rules.get(0));
                    final List<Variable> oValues = originatingTask.getOutputParameterValues();
                    if (oValues != null && !oValues.isEmpty()) {
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
                } else {
                    succeeded = true;
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
                    if (!rules.isEmpty()) {
                        NameExpressionParser parser = new NameExpressionParser(rules.get(0));
                        final List<Variable> oValues = originatingTask.getOutputParameterValues();
                        if (oValues != null && !oValues.isEmpty()) {
                            final List<String> oValuesList;
                            oValuesList = this.listAdapter.marshal(oValues.get(0).getValue());
                            final int size = oValuesList.size();
                            for (int i = 0; i < size; i++) {
                                try {
                                    final String val = oValuesList.get(i);
                                    Path path;
                                    if (FileUtilities.isURI(val)) {
                                        path = Paths.get(new URI(val));
                                    } else {
                                        path = Paths.get(val);
                                    }
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
                if (Files.exists(path)) {
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
                        logger.warning(String.format("No suitable metadata inspector found for output %s", outValue));
                    }
                } else {
                    logger.warning("No such file: " + path);
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
     * @param user the user for which the interrogation is made
     * 
     * @return true if the user stil has disk processing quota available, false otherwise. 
     * @throws QuotaException if the operation fails. 
     */
    private boolean checkProcessingQuota(Principal user) throws QuotaException {
        // update the quota before checking
        UserQuotaManager.getInstance().updateUserProcessingQuota(user);
        
        return UserQuotaManager.getInstance().checkUserProcessingQuota(user);
    }

    private ExecutionJob tryCreateJob(String user, SessionContext context, ExecutionRequest request) {
        return createJob(context, request, ConfigurationManager.getInstance().getBooleanValue("optimize.workflows"));
    }
    
    private ExecutionJob tryCreateWPSJob(String user, String wpsId, Map<String, Map<String, String>> inputs) {
        return createWPSJob(wpsId, inputs);
    }

    private ExecutionJob tryCreateWMSJob(String user, String wmsId, Map<String, String> inputs) {
        return createWMSJob(wmsId, inputs);
    }

    private String translateOutputToMasterPath(ProcessingExecutionTask task) {
        String path = task.getInstanceTargetOutput();
        final String masterRoot = SystemVariable.ROOT.value().replace("\\", "/");
        if (path.startsWith(masterRoot)) {
            return path;
        }
        final DockerVolumeMap workerVolumeMap = ExecutionConfiguration.getWorkerContainerVolumeMap();
        String outPath = path.replace(workerVolumeMap.getContainerWorkspaceFolder(), masterRoot);
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

    private void tryRemoveTemporaryWorkflow(ExecutionJob job) {
        final WorkflowDescriptor workflow = workflowProvider.get(job.getWorkflowId());
        if (workflow != null && workflow.isTemporary()) {
            synchronized (lock) {
                final List<ExecutionJob> jobs = jobProvider.listByWorkflow(workflow.getId());
                if (jobs.stream().allMatch(j -> j.getExecutionStatus() == ExecutionStatus.FAILED ||
                        j.getExecutionStatus() == ExecutionStatus.DONE ||
                        j.getExecutionStatus() == ExecutionStatus.CANCELLED)) {
                    final List<WorkflowNodeDescriptor> nodes = workflow.getNodes();
                    final Map<Long, Long> idMap = new HashMap<>();
                    long originalWorkflowId = workflow.getCreatedFromWorkflowId();
                    for (WorkflowNodeDescriptor node : nodes) {
                        idMap.put(node.getId(), node.getCreatedFromNodeId());
                    }
                    try {
                        for (ExecutionJob jb : jobs) {
                            final List<ExecutionTask> tasks = jb.getTasks();
                            for (ExecutionTask task : tasks) {
                                final Long originalId = idMap.get(task.getWorkflowNodeId());
                                if (originalId != null) {
                                    task.setWorkflowNodeId(originalId);
                                    taskProvider.update(task);
                                }
                            }
                            jb.setWorkflowId(originalWorkflowId);
                            jobProvider.update(jb);
                        }
                        workflowProvider.delete(workflow);
                    } catch (PersistenceException ex) {
                        logger.severe(ex.getMessage());
                    }
                }
            }
        }
    }

    private void tryRestoreSymlinks(ExecutionJob job) {
        if (job.getWorkflowId() != null) {
            final WorkflowDescriptor workflow = workflowProvider.get(job.getWorkflowId());
            if (workflow != null) {
                final List<WorkflowNodeDescriptor> firstLevel = WorkflowUtilities.findFirstLevel(workflow.getOrderedNodes());
                final List<DataSourceComponent> components = new ArrayList<>();
                for (WorkflowNodeDescriptor node : firstLevel) {
                    final TaoComponent c = TaskUtilities.getComponentFor(node);
                    if (c instanceof DataSourceComponent) {
                        components.add((DataSourceComponent) c);
                    }
                }
                for (DataSourceComponent component : components) {
                    synchronized (lock) {
                        final SourceDescriptor srcDescriptor = component.getSources().stream()
                                                                        .filter(t -> t.getName().equals(DataSourceComponent.QUERY_PARAMETER))
                                                                        .findFirst().get();
                        final TargetDescriptor trgDescriptor = component.getTargets().stream()
                                                                        .filter(t -> t.getName().equals(DataSourceComponent.RESULTS_PARAMETER))
                                                                        .findFirst().get();
                        final String initialList = srcDescriptor.getDataDescriptor().getLocation();
                        final String modifiedList = trgDescriptor.getDataDescriptor().getLocation();
                        if (!StringUtilities.isNullOrEmpty(modifiedList)) {
                            final String[] initial = initialList.split(",");
                            final String[] modified = modifiedList.split(",");
                            if (initial.length == modified.length) {
                                for (int i = 0; i < initial.length; i++) {
                                    if (!initial[i].equals(modified[i])) {
                                        try {
                                            FileUtilities.replaceWithLink(FileUtilities.toPath(initial[i]),
                                                                          FileUtilities.toPath(modified[i]));
                                            logger.finest("Restored symlink " + initial[i]);
                                        } catch (IOException e) {
                                            ExceptionUtils.getStackTrace(logger, e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void addJobListener(JobCompletedListener listener) {
        this.jobListeners.add(listener);
    }

    private void notifyJobListeners(ExecutionJob job) {
        this.jobListeners.forEach(l -> {
            try {
                l.onCompleted(job);
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        });
    }

    /**
     * Moves a job one position closer to the head of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the head.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param userId  The user
     * @param jobId The job identifier
     */
    public void moveJobToHead(String userId, long jobId) {
        jobQueue.moveJobToHead(userId, jobId);
    }
    /**
     * Moves a job one position closer to the head of the queue, regardless the user.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    public void moveJobToHead(long jobId) {
        jobQueue.moveJobToHead(jobId);
    }
    /**
     * Moves a job one position closer to the tail of the queue, regardless the user.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    public void moveJobToTail(long jobId) {
        jobQueue.moveJobToTail(jobId);
    }
    /**
     * Moves a job one position closer to the tail of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the tail.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param user  The user
     * @param jobId The job identifier
     */
    public void moveJobToTail(String user, long jobId) {
        jobQueue.moveJobToTail(user, jobId);
    }
    /**
     * Lists the contents of the job queue.
     */
    public Queue<Tuple<Long, String>> getAllJobs() {
        return jobQueue.getAllJobs();
    }
    /**
     * Lists all the queued jobs of a user
     * @param user  The name of the user
     */
    public List<Long> getUserQueuedJobs(String user) {
        return jobQueue.getUserJobs(user);
    }
    /**
     * Lists all the jobs for all the users, grouped by user
     */
    public Map<String, List<Long>> getQueuedJobs() {
        return jobQueue.getUserQueues();
    }
    /**
     * Removes a job from the queue, regardless its position in the queue.
     *
     * @param user  The user
     * @param jobId The job identifier
     */
    public void removeJob(String user, long jobId) {
        jobQueue.removeJob(user, jobId);
        try {
            jobProvider.delete(jobId);
        } catch (PersistenceException e) {
            logger.warning(e.getMessage());
        }
    }

    private class MessageContext extends SessionContext {
        private final Principal impersonatingUser;

        private MessageContext(Message message) {
            this.impersonatingUser = new UserPrincipal(message.getUserId());
        }

        private MessageContext(String userId) {
            this.impersonatingUser = new UserPrincipal(userId);
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
                    AbstractMap.SimpleEntry<Message, SessionContext> entry = messageQueue.take();
                    logger.finest("Dequeued message " + entry.getKey());
                    Orchestrator.this.messageWorker.submit(() -> processMessage(entry.getKey(), entry.getValue()));
                } catch (InterruptedException e) {
                    logger.severe(e.getMessage());
                }
            }
        }
    }

    private class JobListener implements JobCompletedListener {
        private final Orchestrator instance;

        public JobListener(Orchestrator instance) {
            this.instance = instance;
        }

        @Override
        public void onCompleted(ExecutionJob job) {
            try {
                notify(job);
            } catch (PersistenceException e) {
                logger.warning(e.getMessage());
            }
            if (instance.jobQueue.hasMoreJobs(job.getUserId())) {
                instance.jobQueueWorker.setLastUserId(job.getUserId());
            } else{
                // finally, remove any volatile node if the mode is configured
                final int keepAlive = Integer.parseInt(ConfigurationManager.getInstance()
                                                                           .getValue("topology.remove.node.after.execution.delay", "1800"));
                if (keepAlive == 0) {
                    final List<String> hosts = job.getTasks().stream().map(ExecutionTask::getExecutionNodeHostName).collect(Collectors.toList());
                    for (String host : hosts) {
                        if (!StringUtilities.isNullOrEmpty(host)) {
                            try {
                                final TopologyManager topologyManager = TopologyManager.getInstance();
                                final NodeDescription node = topologyManager.getNode(host);
                                if (Boolean.TRUE.equals(node.getVolatile())) {
                                    topologyManager.removeNode(host);
                                    logger.fine(String.format("Node %s destroyed", host));
                                }
                            } catch (Throwable e) {
                                logger.severe(() -> String.format("Cannot destroy host %s. Reason: %s", host, e.getMessage()));
                            }
                        }
                    }
                }
            }
            
            // remove any volatile compnents created for this job
            try {
				jobFactory.removeVolatileComponents(job);
			} catch (PersistenceException e) {
				logger.severe(() -> String.format("Cannot remove volatile components. Reason: %s", e.getMessage()));
			}
        }

        private void notify(ExecutionJob job) throws PersistenceException {
            Duration time = null;
            if (job.getStartTime() != null && job.getEndTime() != null) {
                time = Duration.between(job.getStartTime(), job.getEndTime());
            } else if (job.getStartTime() != null) {
                job.setEndTime(LocalDateTime.now());
                time = Duration.between(job.getStartTime(), job.getEndTime());
                jobProvider.update(job);
            }
            String msg = String.format("Job [%s] for user [%s] %s after %ss",
                                       job.getName(),
                                       job.getUserId(),
                                       job.getExecutionStatus().friendlyName(),
                                       time != null ? time.getSeconds() : "<n/a>");
            switch (job.getExecutionStatus()) {
                case DONE:
                    logger.info(msg);
                    break;
                default:
                    logger.warning(msg);
                    break;
            }
            Messaging.send(new UserPrincipal(job.getUserId()), Topic.EXECUTION.value(), this, msg);
        }
    }
}
