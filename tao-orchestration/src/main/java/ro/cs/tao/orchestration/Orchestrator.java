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

import org.apache.commons.lang3.exception.ExceptionUtils;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.Variable;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.eodata.DataHandlingException;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.eodata.metadata.DecodeStatus;
import ro.cs.tao.eodata.metadata.MetadataInspector;
import ro.cs.tao.eodata.naming.NameExpressionParser;
import ro.cs.tao.eodata.naming.NamingRule;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.OutputDataHandlerManager;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.orchestration.commands.JobCommand;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.orchestration.status.TaskStatusHandler;
import ro.cs.tao.orchestration.util.TaskUtilities;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.utils.TriFunction;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The singleton orchestrator of task executions.
 *
 * @author Cosmin Cara
 */
public class Orchestrator extends Notifiable {

    private static final String GROUP_TASK_SELECTOR_KEY = "group.task.selector";
    private static final String JOB_TASK_SELECTOR_KEY = "job.task.selector";

    private static final Orchestrator instance;
    private final Map<Long, InternalStateHandler> groupStateHandlers;
    private final BlockingQueue<AbstractMap.SimpleEntry<Message, SessionContext>> queue;

    static {
        instance = new Orchestrator();
    }

    public static Orchestrator getInstance() {
        return instance;
    }

    private final StringListAdapter listAdapter;
    private final Logger logger = Logger.getLogger(Orchestrator.class.getName());
    private final PersistenceManager persistenceManager;
    private TaskSelector<ExecutionGroup> groupTaskSelector;
    private TaskSelector<ExecutionJob> jobTaskSelector;
    private JobFactory jobFactory;
    private Set<MetadataInspector> metadataServices;
    private final Map<SessionContext, ExecutorService> executors;
    private final Set<Long> runningTasks;

    private Orchestrator() {
        this.groupStateHandlers = new HashMap<>();
        this.queue = new LinkedBlockingDeque<>();
        this.executors = Collections.synchronizedMap(new HashMap<>());
        this.runningTasks = Collections.synchronizedSet(new HashSet<>());
        this.listAdapter = new StringListAdapter();
        this.persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
        subscribe(Topics.EXECUTION);
    }

    public void start() {
        //JobCommand.setPersistenceManager(persistenceManager);
        //TaskUtilities.setPersistenceManager(persistenceManager);
        final Function<Long, WorkflowNodeDescriptor> workflowProvider = this.persistenceManager::getWorkflowNodeById;
        initializeJobSelector(workflowProvider, this.persistenceManager::getTaskByJobAndNode);
        initializeGroupSelector(workflowProvider, this.persistenceManager::getTaskByGroupAndNode);
        TaskStatusHandler.registerHandlers(persistenceManager);
        this.jobFactory = new JobFactory(this.persistenceManager);
        this.metadataServices = ServiceRegistryManager.getInstance().getServiceRegistry(MetadataInspector.class).getServices();
        QueueMonitor monitor = new QueueMonitor();
        monitor.setName("orchestrator");
        monitor.start();
        logger.fine("Orchestration service initialized");
    }

    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param workflowId The workflow identifier
     * @param inputs     The overridden parameter values for workflow nodes
     * @throws ExecutionException In case anything goes wrong or a job for this workflow was already created
     */
    public long startWorkflow(long workflowId, String description,
                              Map<String, Map<String, String>> inputs, ExecutorService executorService) throws ExecutionException {
        try {
            WorkflowDescriptor descriptor = persistenceManager.getWorkflowDescriptor(workflowId);
            if (descriptor == null) {
                throw new ExecutionException(String.format("Non-existent workflow [%s]", workflowId));
            }
            
            // check if the user's disk quota is not reached
            if (!checkProcessingQuota()) {
            	// do not create any job
            	return -1;
            }
            
            final ExecutionJob executionJob = this.jobFactory.createJob(description, descriptor, inputs);
            this.executors.put(SessionStore.currentContext(), executorService);
            executorService.submit(() -> JobCommand.START.applyTo(executionJob));
            return executionJob.getId();
        } catch (QuotaException | PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Stops the execution of the job corresponding to this workflow.
     *
     * @param workflowId The workflow identifier
     * @throws ExecutionException In case anything goes wrong or there was no job for this workflow
     */
    public void stopWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.STOP.applyTo(job);
    }

    /**
     * Pauses (suspends) the execution of the job corresponding to this workflow.
     *
     * @param workflowId The workflow identifier
     * @throws ExecutionException In case anything goes wrong or there was no job for this workflow
     */
    public void pauseWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.SUSPEND.applyTo(job);
    }

    /**
     * Resumes the execution of the job corresponding to this workflow.
     *
     * @param workflowId The workflow identifier
     * @throws ExecutionException In case anything goes wrong or there was no job for this workflow
     */
    public void resumeWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.RESUME.applyTo(job);
    }

    @Override
    protected void onMessageReceived(Message message) {
        try {
            SessionContext context = new SessionContext() {
                @Override
                protected Principal setPrincipal() { return message::getUser; }

                @Override
                protected List<UserPreference> setPreferences() {
                    try {
                        return persistenceManager != null ?
                                persistenceManager.getUserPreferences(getPrincipal().getName()) : null;
                    } catch (PersistenceException e) {
                        logger.severe(e.getMessage());
                    }
                    return null;
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
            };
            queue.put(new AbstractMap.SimpleEntry<>(message, context));
        } catch (InterruptedException e) {
            logger.severe(e.getMessage());
        }
    }

    private void initializeGroupSelector(Function<Long, WorkflowNodeDescriptor> workflowProvider,
                                         TriFunction<Long, Long, Integer, ExecutionTask> nodeProvider) {
        String groupTaskSelectorClass = ConfigurationManager.getInstance().getValue(GROUP_TASK_SELECTOR_KEY,
                                                                                    DefaultGroupTaskSelector.class.getName());
        Set<TaskSelector> selectors = ServiceRegistryManager.getInstance()
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
        Set<TaskSelector> selectors = ServiceRegistryManager.getInstance()
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

    private void processMessage(Message message, SessionContext currentContext) {
        ExecutionTask task = null;
        try {
            final String taskId = message.getItem(Message.SOURCE_KEY);
            final ExecutionStatus status = EnumUtils.getEnumConstantByName(ExecutionStatus.class, message.getItem(Message.PAYLOAD_KEY));
            task = persistenceManager.getTaskById(Long.parseLong(taskId));
            task.setContext(currentContext);
            logger.fine(String.format("Status change for task %s [node %s]: %s", taskId, task.getWorkflowNodeId(), status.name()));
            statusChanged(task, message.getMessage());
            if (status == ExecutionStatus.DONE || status == ExecutionStatus.CANCELLED || status == ExecutionStatus.FAILED) {
                this.runningTasks.remove(task.getId());
                logger.fine(String.format("Active or pending tasks: %d", this.runningTasks.size()));
            }
            if (status == ExecutionStatus.DONE) {
                WorkflowNodeDescriptor taskNode = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
                // For DataSourceExecutionTask, it is the executor that sets the outputs,
                // hence we need to "confirm" here the outputs of a processing task.
                if (task instanceof ProcessingExecutionTask) {
                    task = handleProcessingTask((ProcessingExecutionTask) task, taskNode.getPreserveOutput());
                }
                List<ExecutionTask> nextTasks = findNextTasks(task);
                if (nextTasks != null && nextTasks.size() > 0) {
                    logger.finest(String.format("Has %s next tasks", nextTasks.size()));
                    for (ExecutionTask nextTask : nextTasks) {
                        flowOutputs(task, nextTask);
                        logger.finest(String.format("Task %s about to start.", nextTask.getId()));
                        nextTask.getInputParameterValues().forEach(
                                v -> logger.finest(String.format("Input: %s=%s", v.getKey(), v.getValue())));
                        ExecutionGroup parentGroupTask = nextTask.getGroupTask();
                        if (parentGroupTask != null) {
                            String state = parentGroupTask.getInternalState();
                            if (state != null) {
                                InternalStateHandler handler = this.groupStateHandlers.get(parentGroupTask.getId());
                                parentGroupTask.setStateHandler(handler);
                                nextTask.setInternalState(String.valueOf(((LoopState) handler.currentState()).getCurrent()));
                            }
                        }
                        nextTask.setContext(currentContext);
                        if (!this.runningTasks.contains(nextTask.getId())) {
                            this.runningTasks.add(nextTask.getId());
                            TaskCommand.START.applyTo(nextTask);
                        }
                    }
                } else {
                    ExecutionJob job = task.getJob();
                    if (TaskUtilities.haveAllTasksCompleted(job)) {
                        logger.fine("No more child tasks to execute after the current task");

                        if (job.orderedTasks().stream()
                                .anyMatch(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING ||
                                               t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE)) {
                            return;
                        }
                        job.setExecutionStatus(ExecutionStatus.DONE);
                        if (job.getEndTime() == null) {
                            job.setEndTime(LocalDateTime.now());
                        }
                        persistenceManager.updateExecutionJob(job);
                        if (taskNode instanceof WorkflowNodeGroupDescriptor && taskNode.getPreserveOutput()) {
                            persistOutputProducts(task);
                        }
                        WorkflowDescriptor workflow = persistenceManager.getWorkflowDescriptor(job.getWorkflowId());
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
                        Messaging.send(currentContext.getPrincipal(), Topics.EXECUTION, this, msg);
                        shutDownExecutor(currentContext);
                    } else {
                        logger.fine("Job has still tasks to complete");
                    }
                }
            } else {
                ExecutionJob job = task.getJob();
                ExecutionStatus jobStatus = job.getExecutionStatus();
                if (jobStatus == ExecutionStatus.CANCELLED || jobStatus == ExecutionStatus.FAILED) {

                    if (job.getEndTime() == null) {
                        job.setEndTime(LocalDateTime.now());
                    }
                    WorkflowDescriptor workflow = persistenceManager.getWorkflowDescriptor(job.getWorkflowId());
                    Duration time = null;
                    if (job.getStartTime() != null && job.getEndTime() != null) {
                        time = Duration.between(job.getStartTime(), job.getEndTime());
                    }
                    String msg = String.format("Job [%s] for workflow [%s]" +
                                                       (jobStatus == ExecutionStatus.CANCELLED ?
                                                               " cancelled after %ss" :
                                                               " failed after %ss"),
                                               job.getId(), workflow.getName(), time != null ? time.getSeconds() : "<unknown>");
                    logger.warning(msg);
                    persistenceManager.updateExecutionJob(job);
                    Messaging.send(currentContext.getPrincipal(), Topics.EXECUTION, this, msg);
                    shutDownExecutor(currentContext);
                }
            }
        } catch (PersistenceException e) {
            logger.severe(String.format("Abnormal termination: %s", e.getMessage()));
            if (task != null) {
                ExecutionJob job = task.getJob();
                job.setExecutionStatus(ExecutionStatus.FAILED);
                try {
                    persistenceManager.updateExecutionJob(job);
                } catch (PersistenceException e1) {
                    logger.severe(String.format("Abnormal termination: %s", e1.getMessage()));
                }
            }
            shutDownExecutor(currentContext);
        }
    }

    private void shutDownExecutor(final SessionContext context) {
        final ExecutorService service = executors.get(context);
        if (service != null) {
            service.shutdown();
            executors.remove(context);
        }
        // update user processing quota
        try {
			UserQuotaManager.getInstance().updateUserProcessingQuota(SessionStore.currentContext().getPrincipal());
		} catch (QuotaException e) {
			logger.severe(String.format("Error updating quota. Error message: %s", e.getMessage()));
		}
    }

    private ExecutionTask handleProcessingTask(ProcessingExecutionTask pcTask, boolean keepOutput) throws PersistenceException {
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
                pcTask.setOutputParameterValue(t.getName(), targetOutput);
                logger.finest(String.format("Output [%s] of task [id=%d] was set to '%s'",
                                            t.getName(), pcTask.getId(), targetOutput));
                if (targetOutput == null) {
                    logger.severe(String.format("NULL TARGET [task %s, parameter %s]", pcTask.getId(), t.getName()));
                }
                if (keepOutput) {
                    persistOutputProducts(pcTask);
                }
                if (lastFromGroup) {
                    groupTask.setOutputParameterValue(t.getName(), targetOutput);
                }
            }
        });
        ExecutionTask task;
        if (lastFromGroup) {
            persistenceManager.updateExecutionTask(groupTask);
            task = pcTask;
        } else {
            task = persistenceManager.updateExecutionTask(pcTask);
        }
        return task;
    }

    private void flowOutputs(ExecutionTask task, ExecutionTask nextTask) throws PersistenceException {
        final List<Variable> values = task.getOutputParameterValues();
        if (task instanceof DataSourceExecutionTask) {
            final int cardinality;
            // A DataSourceExecutionTask outputs the list of results as a JSON
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
                logger.severe(String.format("Task %s produced no results", task.getId()));
                nextTask.setExecutionStatus(ExecutionStatus.CANCELLED);
                statusChanged(nextTask, null);
            }
            for (int i = 0; i < cardinality; i++) {
                valuesList.set(i, TaskUtilities.relativizePathForExecution(valuesList.get(i)));
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
                persistenceManager.updateExecutionTask(groupTask);
            } else {
                // if next to a DataSourceExecutionTask is a simple ExecutionTask, we feed it with as many
                // values as sources
                int expectedCardinality = TaskUtilities.getSourceCardinality(nextTask);
                if (expectedCardinality == -1) {
                    String message = String.format("Cannot determine input cardinality for task %s",
                                                   nextTask.getId());
                    logger.severe(message);
                    nextTask.setExecutionStatus(ExecutionStatus.CANCELLED);
                    statusChanged(nextTask, message);
                }
                final List<Variable> nextInputParameters = nextTask.getInputParameterValues();
                if (expectedCardinality != 0) {
                    if (cardinality < expectedCardinality) {
                        String message = String.format("Insufficient inputs for task %s [expected %s, received %s]",
                                                       nextTask.getId(),
                                                       expectedCardinality, cardinality);
                        logger.severe(message);
                        nextTask.setExecutionStatus(ExecutionStatus.CANCELLED);
                        statusChanged(nextTask, message);
                    }
                    int idx = 0;
                    final Set<String> outParams = nextTask.getOutputParameterValues().stream().map(Variable::getKey).collect(Collectors.toSet());
                    Variable overriddenOutParam = null;
                    for (Variable var : nextInputParameters) {
                        if (!outParams.contains(var.getKey())) {
                            nextTask.setInputParameterValue(var.getKey(), valuesList.get(idx++));
                        } else {
                            overriddenOutParam = var;
                        }
                    }
                    // If there is an overridden output parameter, it may be that we have a name expression
                    if (overriddenOutParam != null) {
                        DataSourceExecutionTask originatingTask = findOriginatingTask(nextTask);
                        if (originatingTask != null) {
                            String targetOutput = overriddenOutParam.getValue();
                            String sensor = originatingTask.getComponent().getSensorName().replace("-", "");
                            List<NamingRule> rules = persistenceManager.getRules(sensor);
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
                                        } catch (Exception e) {
                                            logger.warning(e.getMessage());
                                        }
                                    }
                                    final String key = overriddenOutParam.getKey();
                                    nextTask.setOutputParameterValue(key, parser.resolve(targetOutput, oValuesList.toArray(new String[0])));
                                    nextTask.getInputParameterValues().removeIf(v -> v.getKey().equals(key));
                                }
                            }
                        }
                    }
                } else { // nextTask accepts a list
                    Map<String, String> connectedInputs = TaskUtilities.getConnectedInputs(task, nextTask);
                    if (theOnlyValue != null && connectedInputs != null) {
                        for (Variable var : nextInputParameters) {
                            if (theOnlyValue.getKey().equals(connectedInputs.get(var.getKey()))) {
                                nextTask.setInputParameterValue(var.getKey(), theOnlyValue.getValue());
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            Map<String, String> connectedInputs = TaskUtilities.getConnectedInputs(task, nextTask);
            final List<Variable> nextInputParameters = nextTask.getInputParameterValues();
            if (connectedInputs != null) {
                for (Map.Entry<String, String> entry : connectedInputs.entrySet()) {
                    nextTask.setInputParameterValue(entry.getKey(),
                                                    values.stream().filter(v -> v.getKey().equals(entry.getValue())).findFirst().get().getValue());
                }
            }
            final Set<String> outParams = nextTask.getOutputParameterValues().stream().map(Variable::getKey).collect(Collectors.toSet());
            Variable overriddenOutParam = null;
            for (Variable var : nextInputParameters) {
                if (outParams.contains(var.getKey())) {
                    overriddenOutParam = var;
                    break;
                }
            }
            // If there is an overridden output parameter, it may be that we have a name expression
            if (overriddenOutParam != null) {
                DataSourceExecutionTask originatingTask = findOriginatingTask(nextTask);
                if (originatingTask != null) {
                    String targetOutput = overriddenOutParam.getValue();
                    String sensor = originatingTask.getComponent().getSensorName().replace("-", "");
                    List<NamingRule> rules = persistenceManager.getRules(sensor);
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
                            final String key = overriddenOutParam.getKey();
                            nextTask.setOutputParameterValue(key, parser.resolve(targetOutput, oValuesList.toArray(new String[0])));
                            nextTask.getInputParameterValues().removeIf(v -> v.getKey().equals(key));
                        }
                    }
                }
            }
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

    private void statusChanged(ExecutionTask task, String reason) {
        ExecutionGroup groupTask = task.getGroupTask();
        if (groupTask != null) {
            notifyTaskGroup(groupTask, task, reason);
        } else {
            notifyJob(task, reason);
        }
    }

    private void notifyTaskGroup(ExecutionGroup groupTask, ExecutionTask task, String reason) {
        ExecutionStatus previousStatus = groupTask.getExecutionStatus();
        List<ExecutionTask> tasks = groupTask.getTasks();
        ExecutionStatus taskStatus = task.getExecutionStatus();
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
                    int startIndex = IntStream.range(0, tasks.size())
                            .filter(i -> task.getId().equals(tasks.get(i).getId()))
                            .findFirst().orElse(0);
                    bulkSetStatus(tasks.subList(startIndex, tasks.size()), taskStatus);
                    if (TransitionBehavior.FAIL_ON_ERROR == persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId()).getBehavior()) {
                        groupTask.setExecutionStatus(taskStatus);
                    } else {
                        if (groupTask.getStateHandler() == null) {
                            groupTask.setStateHandler(groupStateHandlers.get(groupTask.getId()));
                        }
                        LoopState nextState = (LoopState) groupTask.nextInternalState();
                        if (nextState != null) {
                            task.setExecutionStatus(ExecutionStatus.UNDETERMINED);
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
                            task.setExecutionStatus(ExecutionStatus.UNDETERMINED);
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
                persistenceManager.updateExecutionTask(groupTask);
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
                logger.severe(ExceptionUtils.getStackTrace(ex));
            }
        }
    }

    private ExecutionJob checkWorkflow(long workflowId) {
        List<ExecutionJob> jobs = persistenceManager.getJobs(workflowId);
        if (jobs == null || jobs.isEmpty()) {
            throw new ExecutionException(String.format("No job exists for workflow %s", workflowId));
        }
        return jobs.stream().filter(j -> j.getExecutionStatus() != ExecutionStatus.DONE
                && j.getExecutionStatus() != ExecutionStatus.CANCELLED
                && j.getExecutionStatus() != ExecutionStatus.FAILED).findFirst().orElse(null);
    }

    private void bulkSetStatus(Collection<ExecutionTask> tasks, ExecutionStatus status) {
        if (tasks == null) {
            return;
        }
        tasks.forEach(t -> {
            t.setExecutionStatus(status);
            try {
                persistenceManager.updateTaskStatus(t, status);
                logger.fine(String.format("Task %s was put into status %s", t.getId(), status));
            } catch (PersistenceException e) {
                logger.severe(e.getMessage());
            }
        });
    }

    private List<ExecutionTask> findNextTasks(ExecutionTask task) {
        ExecutionGroup groupTask = task instanceof ExecutionGroup ? (ExecutionGroup) task : task.getGroupTask();
        ExecutionJob job = task.getJob();
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

    private DataSourceExecutionTask findOriginatingTask(ExecutionTask current) {
        ExecutionGroup groupTask = current.getGroupTask();
        if (groupTask != null) {
            return this.groupTaskSelector.findDataSourceTask(groupTask, current);
        } else {
            return this.jobTaskSelector.findDataSourceTask(current.getJob(), current);
        }
    }

    private void persistOutputProducts(ExecutionTask task) {
        List<Variable> values = task.getOutputParameterValues();
        WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        TaoComponent component;
        if (node instanceof WorkflowNodeGroupDescriptor) {
            component = persistenceManager.getGroupComponentById(node.getComponentId());
        } else {
            component = persistenceManager.getProcessingComponentById(node.getComponentId());
        }
        List<EOProduct> products = new ArrayList<>();
        for (Variable value : values) {
            products.addAll(createProducts(component, value, task.getContext()));
        }
        try {
            OutputDataHandlerManager.getInstance().applyHandlers(products);
        } catch (DataHandlingException ex) {
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
                .get();
        if (value != null) {
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
            if (descriptor.getDataDescriptor().getFormatType() == DataFormat.RASTER) {
                String netPath = context.getNetSpace().toString().replace('\\', '/');
                Path path;
                if (outValue.startsWith(netPath)) {
                    path = Paths.get(outValue.replace(netPath, context.getWorkspace().toString()));
                } else {
                    path = Paths.get(outValue);
                }
                MetadataInspector metadataInspector = this.metadataServices.stream()
                        .filter(s -> s.decodeQualification(path) == DecodeStatus.SUITABLE)
                        .findFirst().orElse(null);
                if (metadataInspector != null) {
                    MetadataInspector.Metadata metadata = metadataInspector.getMetadata(path);
                    if (metadata != null) {
                        product = metadata.toProductDescriptor(path);
                        product.addReference(context.getPrincipal().getName());
                        product.setVisibility(Visibility.PRIVATE);
                    }
                }
            }
        } catch (Exception e2) {
            logger.severe(ExceptionUtils.getStackTrace(e2));
            logger.severe("If this was caused by 'gdalinfo', please check that the Docker daemon is not blocked by a firewall");
        }
        return product;
    }
    
    /**
     * Check if the user still has disk processing quota available
     * 
     * @return true if the user stil has disk processing quota available, false otherwise. 
     * @throws QuotaException if the operation fails. 
     */
    private boolean checkProcessingQuota() throws QuotaException {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
    	// update the quota before checking
        UserQuotaManager.getInstance().updateUserProcessingQuota(principal);
        
        return UserQuotaManager.getInstance().checkUserProcessingQuota(principal);
    }
    
    private class QueueMonitor extends Thread {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    AbstractMap.SimpleEntry<Message, SessionContext> entry = queue.take();
                    ExecutorService service = Orchestrator.this.executors.get(entry.getValue());
                    if (service != null) {
                        service.submit(() -> processMessage(entry.getKey(), entry.getValue()));
                    }/* else {
                        logger.warning("No executor found for " + entry.getKey());
                    }*/
                } catch (InterruptedException e) {
                    logger.severe(e.getMessage());
                }
            }
        }
    }
}
