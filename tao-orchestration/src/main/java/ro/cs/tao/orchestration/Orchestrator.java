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

import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.SerializerFactory;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import javax.xml.transform.stream.StreamSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class Orchestrator extends Notifiable {

    private static final Orchestrator instance;

    static {
        instance = new Orchestrator();
    }

    public static Orchestrator getInstance() { return instance; }

    private final Logger logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    private PersistenceManager persistenceManager;
    private TaskSelector<ExecutionGroup> groupTaskSelector;
    private TaskSelector<ExecutionJob> jobTaskSelector;
    private final LoopStateHandler groupInternalStateHandler;

    private Orchestrator() {
        this.groupInternalStateHandler = new LoopStateHandler();
        subscribe(Topics.TASK_STATUS_CHANGED);
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        JobCommand.setPersistenceManager(persistenceManager);
        TaskCommand.setPersistenceManager(persistenceManager);
        this.groupTaskSelector = new DefaultGroupTaskSelector(this.persistenceManager);
        this.jobTaskSelector = new DefaultJobTaskSelector(this.persistenceManager);
    }

    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param workflowId    The workflow identifier
     *
     * @throws ExecutionException   In case anything goes wrong or a job for this workflow was already created
     */
    public void startWorkflow(long workflowId) throws ExecutionException {
        try {
            ExecutionJob job = persistenceManager.getJob(workflowId);
            if (job == null) {
                WorkflowDescriptor descriptor = persistenceManager.getWorkflowDescriptor(workflowId);
                job = create(descriptor);
            }
            JobCommand.START.applyTo(job);
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

    private ExecutionJob checkWorkflow(long workflowId) {
        ExecutionJob job = persistenceManager.getJob(workflowId);
        if (job == null) {
            throw new ExecutionException(String.format("No job exists for workflow %s", workflowId));
        }
        return job;
    }

    private ExecutionJob create(WorkflowDescriptor workflow) throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setUserName("admin");
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            job = persistenceManager.saveExecutionJob(job);
            List<WorkflowNodeDescriptor> nodes = workflow.getOrderedNodes();
            for (WorkflowNodeDescriptor node : nodes) {
                ExecutionTask task = createTask(node);
                task.setLevel(node.getLevel());
                persistenceManager.saveExecutionTask(task, job);
                System.out.println(String.format("Created task for node %s [level %s]", node.getId(), task.getLevel()));
            }
        }
        return job;
    }

    private ExecutionTask createGroup(WorkflowNodeGroupDescriptor groupNode) throws PersistenceException {
        ExecutionGroup group = new ExecutionGroup();
        List<WorkflowNodeDescriptor> nodes = groupNode.getOrderedNodes();
        for (WorkflowNodeDescriptor node : nodes) {
            ExecutionTask task = createTask(node);
            group.addTask(task);
        }
        group.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        return group;
    }

    private ExecutionTask createTask(WorkflowNodeDescriptor workflowNode) throws PersistenceException {
        ExecutionTask task;
        if (workflowNode instanceof WorkflowNodeGroupDescriptor) {
            task = createGroup((WorkflowNodeGroupDescriptor) workflowNode);
        } else {
            TaoComponent component;
            List<ParameterValue> customValues = workflowNode.getCustomValues();
            List<ComponentLink> links = workflowNode.getIncomingLinks();
            if (links != null) {
                task = new ProcessingExecutionTask();
                component = persistenceManager.getProcessingComponentById(workflowNode.getComponentId());
                ((ProcessingExecutionTask) task).setComponent((ProcessingComponent) component);
                links.forEach(link -> {
                    String name = link.getOutput().getName();
                    String value = link.getInput().getDataDescriptor().getLocation();
                    task.setParameterValue(name, value);
                });
            } else {
                task = new DataSourceExecutionTask();
                component = persistenceManager.getDataSourceInstance(workflowNode.getComponentId());
                ((DataSourceExecutionTask) task).setComponent((DataSourceComponent) component);
            }
            task.setWorkflowNodeId(workflowNode.getId());
            if (customValues != null) {
                for (ParameterValue customValue : customValues) {
                    task.setParameterValue(customValue.getParameterName(), customValue.getParameterValue());
                }
            }
            String nodeAffinity = component.getNodeAffinity();
            if (nodeAffinity != null && !"Any".equals(nodeAffinity)) {
                task.setExecutionNodeHostName(nodeAffinity);
            }
            task.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        }
        return task;
    }

    @Override
    protected void onMessageReceived(Message message) {
        try {
            String taskId = message.getItem(Message.SOURCE_KEY);
            ExecutionStatus status = ExecutionStatus.getEnumConstantByValue(Integer.parseInt(message.getItem(Message.PAYLOAD_KEY)));
            ExecutionTask task = persistenceManager.getTaskById(Long.parseLong(taskId));
            logger.fine(String.format("Status change for task %s: %s", taskId, status));
            System.out.println(String.format("Status change for task %s [node %s]: %s", taskId, task.getWorkflowNodeId(), status.name()));
            statusChanged(task);
            persistenceManager.updateExecutionJob(task.getJob());
            if (status == ExecutionStatus.DONE) {
                List<ExecutionTask> nextTasks = getNext(task);
                if (nextTasks != null && nextTasks.size() > 0) {
                    System.out.println(String.format("Has %s next tasks", nextTasks.size()));
                    for (ExecutionTask nextTask : nextTasks) {
                        if (nextTask != null) {
                            TaskCommand.START.applyTo(nextTask);
                        }
                    }
                } else {
                    logger.fine("No more child tasks to execute after the current task");
                    System.out.println(String.format("No more child tasks to execute after the current task"));
                }
            }
            System.out.println("Job status: " + task.getJob().getExecutionStatus().name());
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
        }
    }

    private void statusChanged(ExecutionTask task) {
        ExecutionGroup groupTask = (ExecutionGroup) task.getGroupTask();
        if (groupTask != null) {
            notifyGroupStatusChanged(groupTask, task);
        } else {
            notifytJobStatusChanged(persistenceManager.getJobById(task.getJob().getId()), task);
        }
    }

    private void notifyGroupStatusChanged(ExecutionGroup groupTask, ExecutionTask task) {
        ExecutionStatus groupStatus = groupTask.getExecutionStatus();
        List<ExecutionTask> tasks = groupTask.getTasks();
        ExecutionStatus taskStatus = task.getExecutionStatus();
        switch (taskStatus) {
            case SUSPENDED:
            case CANCELLED:
            case FAILED:
                bulkSetStatus(groupTask, task, taskStatus);
                groupTask.setExecutionStatus(taskStatus);
                break;
            case DONE:
                // If the task is the last one of this group
                if (tasks.get(tasks.size() - 1).getId().equals(task.getId())) {
                    Integer nextState = this.groupInternalStateHandler.apply(groupTask.getInternalState());
                    if (nextState != null) {
                        bulkSetStatus(groupTask, null, ExecutionStatus.UNDETERMINED);
                        groupTask.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                        try {
                            BaseSerializer<LoopState> serializer = SerializerFactory.create(LoopState.class, MediaType.JSON);
                            LoopState current = serializer.deserialize(new StreamSource(groupTask.getInternalState()));
                            current.setCurrent(nextState);
                            groupTask.setInternalState(serializer.serialize(current));
                        } catch (SerializationException e) {
                            e.printStackTrace();
                        }
                    } else {
                        groupTask.setExecutionStatus(ExecutionStatus.DONE);
                    }
                }
                break;
            default:
                // do nothing for other states
                break;
        }
        if (groupStatus != null && groupStatus != groupTask.getExecutionStatus()) {
            notifytJobStatusChanged(groupTask.getJob(), groupTask);
        }
    }

    private void notifytJobStatusChanged(ExecutionJob job, ExecutionTask changedTask) {
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
                case DONE:
                    List<ExecutionTask> tasks = job.orderTasks();
                    if (tasks.get(tasks.size() - 1).getId().equals(changedTask.getId())) {
                        job.setExecutionStatus(ExecutionStatus.DONE);
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

    private void bulkSetStatus(ExecutionGroup group, ExecutionTask firstExculde, ExecutionStatus status) {
        List<ExecutionTask> tasks = group.getTasks();
        if (tasks == null) {
            return;
        }
        int idx = 0;
        boolean found = false;
        if (firstExculde == null) {
            firstExculde = tasks.get(0);
            firstExculde.setExecutionStatus(status);
        }
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

    private List<ExecutionTask> getCandidates(ExecutionTask task) {
        if (task == null || task.getExecutionStatus() != ExecutionStatus.DONE) {
            return null;
        }
        WorkflowNodeDescriptor workflowNode = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        if (workflowNode == null) {
            logger.severe(String.format("No workflow node with id %s was found in the database", task.getWorkflowNodeId()));
            return null;
        }
        WorkflowDescriptor workflow = workflowNode.getWorkflow();
        ExecutionJob job = task.getJob();
        List<WorkflowNodeDescriptor> childNodes = workflow.findChildren(workflow.getNodes(), workflowNode);
        if (childNodes == null || childNodes.size() == 0) {
            return null;
        }
        return childNodes.stream().filter(n ->
                n.getIncomingLinks().stream().allMatch(link -> {
                    List<WorkflowNodeDescriptor> parents =
                            persistenceManager.getWorkflowNodesByComponentId(workflow.getId(),link.getInput().getParentId());
                    return parents.stream().allMatch(p -> {
                        ExecutionTask taskByNode = persistenceManager.getTaskByJobAndNode(job.getId(), p.getId());
                        return taskByNode != null && taskByNode.getExecutionStatus() == ExecutionStatus.DONE;
                    });
                }))
                .map(n -> persistenceManager.getTaskByJobAndNode(job.getId(), n.getId()))
                .collect(Collectors.toList());
    }

    private List<ExecutionTask> getNext(ExecutionTask task) {
        ExecutionGroup groupTask = (ExecutionGroup) task.getGroupTask();
        return groupTask != null ? this.groupTaskSelector.chooseNext(groupTask, task) :
                this.jobTaskSelector.chooseNext(task.getJob(), task);
    }
}
