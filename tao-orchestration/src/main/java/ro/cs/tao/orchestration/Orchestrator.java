/*
 * Copyright (C) 2017 CS ROMANIA
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

import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.orchestration.commands.JobCommand;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.serialization.MapAdapter;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class Orchestrator extends Notifiable {

    private static final Orchestrator instance;
    private final ExecutorService backgroundWorker;
    private final Map<Long, InternalStateHandler> groupStateHandlers;

    static {
        instance = new Orchestrator();
    }

    public static Orchestrator getInstance() { return instance; }

    private final Logger logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    private PersistenceManager persistenceManager;
    private TaskSelector groupTaskSelector;
    private TaskSelector jobTaskSelector;
    private JobFactory jobFactory;

    private Orchestrator() {
        this.backgroundWorker = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.groupStateHandlers = new HashMap<>();
        subscribe(Topics.TASK_STATUS_CHANGED);
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        JobCommand.setPersistenceManager(persistenceManager);
        TaskCommand.setPersistenceManager(persistenceManager);
        Set<TaskSelector> selectors = ServiceRegistryManager.getInstance().getServiceRegistry(TaskSelector.class).getServices();
        final Function<Long, WorkflowNodeDescriptor> workflowProvider = this.persistenceManager::getWorkflowNodeById;
        final BiFunction<Long, Long, ExecutionTask> taskByGroupNodeProvider = this.persistenceManager::getTaskByGroupAndNode;
        final BiFunction<Long, Long, ExecutionTask> taskByJobNodeProvider = this.persistenceManager::getTaskByJobAndNode;
        final BiFunction<Long, String, List<WorkflowNodeDescriptor>> nodesByComponentProvider = this.persistenceManager::getWorkflowNodesByComponentId;
        this.groupTaskSelector = selectors.stream()
                .filter(s -> ExecutionGroup.class.equals(s.getTaskContainerClass())).findFirst()
                .orElse(new DefaultGroupTaskSelector());
        this.groupTaskSelector.setWorkflowProvider(workflowProvider);
        this.groupTaskSelector.setTaskByNodeProvider(taskByGroupNodeProvider);
        this.groupTaskSelector.setNodesByComponentProvider(nodesByComponentProvider);
        this.jobTaskSelector = selectors.stream()
                .filter(s -> ExecutionJob.class.equals(s.getTaskContainerClass())).findFirst()
                .orElse(new DefaultJobTaskSelector());
        this.jobTaskSelector.setWorkflowProvider(workflowProvider);
        this.jobTaskSelector.setTaskByNodeProvider(taskByJobNodeProvider);
        this.jobTaskSelector.setNodesByComponentProvider(nodesByComponentProvider);
        this.jobFactory = new JobFactory(this.persistenceManager);
    }

    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param workflowId    The workflow identifier
     *
     * @throws ExecutionException   In case anything goes wrong or a job for this workflow was already created
     */
    public long startWorkflow(long workflowId, Map<String, String> inputs) throws ExecutionException {
        try {
            List<ExecutionJob> jobs = persistenceManager.getJobs(workflowId);
            if (jobs == null || jobs.size() == 0 || jobs.stream().noneMatch(j -> checkExistingJob(j, inputs))) {
                WorkflowDescriptor descriptor = persistenceManager.getWorkflowDescriptor(workflowId);
                if (descriptor == null) {
                    throw new ExecutionException(String.format("Non-existent workflow [%s]", workflowId));
                }
                final ExecutionJob executionJob = this.jobFactory.createJob(descriptor, inputs);
                backgroundWorker.submit(() -> JobCommand.START.applyTo(executionJob));
                return executionJob.getId();
            } else {
                throw new ExecutionException(
                        String.format("A job for the workflow [%s] with the same input values is already running",
                                      workflowId));
            }
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Stops the execution of the job corresponding to this workflow.
     *
     * @param workflowId    The workflow identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job for this workflow
     */
    public void stopWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.STOP.applyTo(job);
    }

    /**
     * Pauses (suspends) the execution of the job corresponding to this workflow.
     *
     * @param workflowId    The workflow identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job for this workflow
     */
    public void pauseWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.SUSPEND.applyTo(job);
    }

    /**
     * Resumes the execution of the job corresponding to this workflow.
     *
     * @param workflowId    The workflow identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job for this workflow
     */
    public void resumeWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.RESUME.applyTo(job);
    }

    @Override
    protected void onMessageReceived(Message message) {
        try {
            String taskId = message.getItem(Message.SOURCE_KEY);
            ExecutionStatus status = ExecutionStatus.getEnumConstantByValue(Integer.parseInt(message.getItem(Message.PAYLOAD_KEY)));
            if (status == null) {
                throw new PersistenceException(String.format("Invalid status received: %s",
                                                             message.getItem(Message.PAYLOAD_KEY)));
            }
            ExecutionTask task = persistenceManager.getTaskById(Long.parseLong(taskId));
            logger.fine(String.format("Status change for task %s [node %s]: %s",
                                             taskId,
                                             task.getWorkflowNodeId(),
                                             status.name()));
            statusChanged(task);
            if (status == ExecutionStatus.DONE) {
                // For DataSourceExecutionTask, it is the executor that sets the outputs,
                // hence we need to "confirm" here the outputs of a processing task.
                if (task instanceof ProcessingExecutionTask) {
                    ProcessingExecutionTask pcTask = (ProcessingExecutionTask) task;
                    pcTask.getComponent().getTargets().forEach(t -> {
                        pcTask.setOutputParameterValue(t.getName(), pcTask.getInstanceTargetOuptut(t));
                        logger.fine(String.format("Task %s output: %s=%s",
                                                  task.getId(), t.getName(),
                                                  t.getDataDescriptor().getLocation()));
                    });
                    persistenceManager.updateExecutionTask(task);
                }
                List<ExecutionTask> nextTasks = findNextTasks(task);
                if (nextTasks != null && nextTasks.size() > 0) {
                    logger.fine(String.format("Has %s next tasks", nextTasks.size()));
                    for (ExecutionTask nextTask : nextTasks) {
                        if (nextTask != null) {
                            if (task instanceof DataSourceExecutionTask && nextTask instanceof ExecutionGroup) {
                                ExecutionGroup groupTask = (ExecutionGroup) nextTask;
                                // A DataSourceExecutionTask outputs the list of results as a JSON
                                List<Variable> values = task.getOutputParameterValues();
                                int cardinality = 0;
                                if (values != null && values.size() > 0) {
                                    cardinality = new StringListAdapter().marshal(values.get(0).getValue()).size();
                                }
                                InternalStateHandler handler = new LoopStateHandler(new LoopState(cardinality, 1));
                                groupTask.setStateHandler(handler);
                                // we need to keep this mapping because ExecutionTask.stateHandler is transient
                                this.groupStateHandlers.put(groupTask.getId(), handler);
                                if (values != null && values.size() > 0) {
                                    Variable theOnlyValue = values.get(0);
                                    groupTask.setInputParameterValue(groupTask.getInputParameterValues().get(0).getKey(),
                                                                     theOnlyValue.getValue());
                                }
                                persistenceManager.updateExecutionTask(groupTask);
                            }
                            logger.fine(String.format("Task %s about to start.", nextTask.getId()));
                            nextTask.getInputParameterValues().forEach(
                                    v -> logger.fine(String.format("Input: %s=%s", v.getKey(), v.getValue()))
                            );
                            TaskCommand.START.applyTo(nextTask);
                        }
                    }
                } else {
                    logger.fine("No more child tasks to execute after the current task");
                    ExecutionJob job = task.getJob();
                    job.setExecutionStatus(ExecutionStatus.DONE);
                    persistenceManager.updateExecutionJob(job);
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
                    Messaging.send(SystemPrincipal.instance(), Topics.INFORMATION, this, msg);
                }
            }
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
        }
    }

    /**
     * An execution job is considered duplicate if:
     * 1) it is created for the same workflow and
     * 2) it has the same inputs and
     * 3) the previously created job is in one of the following states: UNDETERMINED, QUEUED_ACTIVE, RUNNING
     *
     * @param job       An existing job to check
     * @param inputs    The inputs to be checked against
     * @return          <code>true</code> if the given job doesn't match the inputs, <code>false</code> otherwise
     */
    private boolean checkExistingJob(ExecutionJob job, Map<String, String> inputs) {
        boolean existing = false;
        switch (job.getExecutionStatus()) {
            case UNDETERMINED:
            case QUEUED_ACTIVE:
            case RUNNING:
                List<ExecutionTask> tasks = job.getTasks();
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

    private void statusChanged(ExecutionTask task) {
        ExecutionGroup groupTask = task.getGroupTask();
        if (groupTask != null) {
            notifyTaskGroup(groupTask, task);
        } else {
            ExecutionJob job = task.getJob();
            notifyJob(job, task);
        }
    }

    private void notifyTaskGroup(ExecutionGroup groupTask, ExecutionTask task) {
        ExecutionStatus groupStatus = groupTask.getExecutionStatus();
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
                    bulkSetStatus(groupTask, task, taskStatus);
                    groupTask.setExecutionStatus(taskStatus);
                    break;
                case DONE:
                    // If the task is the last one executing of this group
                    if (tasks.stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
                        if (groupTask.getStateHandler() == null) {
                            groupTask.setStateHandler(groupStateHandlers.get(groupTask.getId()));
                        }
                        LoopState nextState = (LoopState) groupTask.nextInternalState();
                        if (nextState != null) {
                            bulkSetStatus(groupTask, null, ExecutionStatus.UNDETERMINED);
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
            ExecutionStatus currenStatus = groupTask.getExecutionStatus();
            if (groupStatus != null && groupStatus != currenStatus) {
                persistenceManager.updateExecutionTask(groupTask);
                ExecutionJob job = groupTask.getJob();
                notifyJob(job, groupTask);
            }
        } catch (PersistenceException pex) {
            logger.severe(pex.getMessage());
        }
    }

    private void notifyJob(ExecutionJob job, ExecutionTask changedTask) {
        ExecutionStatus taskStatus = changedTask.getExecutionStatus();
        try {
            switch (taskStatus) {
                case SUSPENDED:
                case CANCELLED:
                case FAILED:
                    bulkSetStatus(job, changedTask, taskStatus);
                    job.setExecutionStatus(taskStatus);
                    persistenceManager.updateExecutionJob(job);
                    break;
                case RUNNING:
                    ExecutionStatus jobStatus = job.getExecutionStatus();
                    if (jobStatus == ExecutionStatus.QUEUED_ACTIVE || jobStatus == ExecutionStatus.UNDETERMINED) {
                        job.setExecutionStatus(ExecutionStatus.RUNNING);
                        persistenceManager.updateExecutionJob(job);
                    }
                    break;
                case DONE:
                    if (job.getTasks().stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
                    /*List<ExecutionTask> tasks = job.orderTasks();
                    if (tasks.get(tasks.size() - 1).getId().equals(changedTask.getId())) {*/
                        job.setExecutionStatus(ExecutionStatus.DONE);
                        job.setEndTime(LocalDateTime.now());
                        persistenceManager.updateExecutionJob(job);
                    }
                    break;
                default:
                    // do nothing for other states
                    break;
            }
        } catch (PersistenceException pex) {
            logger.severe(pex.getMessage());
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

    private void bulkSetStatus(ExecutionGroup group, ExecutionTask firstExculde, ExecutionStatus status) throws PersistenceException {
        List<ExecutionTask> tasks = group.getTasks();
        if (tasks == null) {
            return;
        }
        int idx = 0;
        boolean found = false;
        if (firstExculde == null) {
            firstExculde = tasks.get(0);
            firstExculde.setExecutionStatus(status);
            persistenceManager.updateExecutionTask(firstExculde);
        }
        while (idx < tasks.size()) {
            ExecutionTask task = tasks.get(idx);
            if (!found) {
                found = task.getId().equals(firstExculde.getId());
            } else {
                task.setExecutionStatus(status);
                persistenceManager.updateExecutionTask(task);
            }
            idx++;
        }
        if (!found) {
            throw new IllegalArgumentException("Task not found");
        }
    }

    private void bulkSetStatus(ExecutionJob job, ExecutionTask firstExculde, ExecutionStatus status) {
        List<ExecutionTask> tasks = job.orderTasks();
        if (tasks == null) {
            return;
        }
        int idx = 0;
        boolean found = false;
        while (idx < tasks.size()) {
            if (!found) {
                found = tasks.get(idx).getId().equals(firstExculde.getId());
            } else {
                tasks.get(idx).setExecutionStatus(status);
            }
            idx++;
        }
        if (!found) {
            throw new IllegalArgumentException("Task not found");
        }
    }

    private List<ExecutionTask> findNextTasks(ExecutionTask task) {
        ExecutionGroup groupTask = task instanceof ExecutionGroup ? (ExecutionGroup) task : task.getGroupTask();
        ExecutionJob job = task.getJob();
        return (groupTask != null) ? this.groupTaskSelector.chooseNext(groupTask, task) :
                this.jobTaskSelector.chooseNext(job, task);
    }

    private List<Variable> deserializeResults(String jsonValue) {
        List<Variable> pairs = null;
        try {
            MapAdapter mapAdapter = new MapAdapter();
            Map<String, String> map = mapAdapter.marshal(jsonValue);
            if (map != null) {
                pairs = map.entrySet().stream().map(e -> new Variable(e.getKey(), e.getValue())).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.severe("Serialization of results failed: " + e.getMessage());
        }
        return pairs;
    }
}
