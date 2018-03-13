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
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.ExecutionGroup;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class Orchestrator extends Notifiable {

    private static final Orchestrator instance;

    static {
        instance = new Orchestrator();
    }

    public static final Orchestrator getInstance() { return instance; }

    private final Logger logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    private PersistenceManager persistenceManager;

    private Orchestrator() {
        subscribe(Topics.TASK_STATUS_CHANGED);
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        JobCommand.setPersistenceManager(persistenceManager);
        TaskCommand.setPersistenceManager(persistenceManager);
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
            job.setTaskSelector(new DefaultTaskSelector());
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
        try {
            ExecutionJob job = persistenceManager.getJob(workflowId);
            if (job == null) {
                throw new ExecutionException(String.format("No job exists for workflow %s", workflowId));
            }
            return job;
        } catch (PersistenceException pex) {
            throw new ExecutionException(pex);
        }
    }

    private ExecutionJob create(WorkflowDescriptor workflow) throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            List<WorkflowNodeDescriptor> nodes = workflow.getOrderedNodes();
            for (WorkflowNodeDescriptor node : nodes) {
                ExecutionTask task = createTask(node);
                job.addTask(task);
                persistenceManager.saveExecutionTask(task, job);
            }
            persistenceManager.saveExecutionJob(job);
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
            task = new ExecutionTask();
            task.setWorkflowNodeId(workflowNode.getId());
            TaoComponent component;
            List<ParameterValue> customValues = workflowNode.getCustomValues();
            List<ComponentLink> links = workflowNode.getIncomingLinks();
            if (links != null) {
                component = persistenceManager.getProcessingComponentById(workflowNode.getComponentId());
                task.setComponent(component);
                links.forEach(link -> {
                    String name = link.getInput().getId();
                    String value = link.getInput().getDataDescriptor().getLocation();
                    task.setParameterValue(name, value);
                });
            } else {
                component = persistenceManager.getDataSourceInstance(workflowNode.getComponentId());
            }
            if (customValues != null) {
                customValues.forEach(v -> task.setParameterValue(v.getParameterName(), v.getParameterValue()));
            }
            String nodeAffinity = component.getNodeAffinity();
            if (nodeAffinity != null) {
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
            ExecutionStatus status = Enum.valueOf(ExecutionStatus.class, message.getItem(Message.PAYLOAD_KEY));
            logger.fine(String.format("Received status change for task %s: %s", taskId, status));
            ExecutionTask task = persistenceManager.getTaskById(Long.parseLong(taskId));
            task.changeStatus(status);
            persistenceManager.updateExecutionTask(task);
            ExecutionJob job = task.getJob();
            job.getTasks().forEach(t -> {
                try {
                    persistenceManager.updateExecutionTask(t);
                } catch (PersistenceException e) {
                    logger.severe(e.getMessage());
                }
            });
            persistenceManager.updateExecutionJob(task.getJob());
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
        }
    }
}
