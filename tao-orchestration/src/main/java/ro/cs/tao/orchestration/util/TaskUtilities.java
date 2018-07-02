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

package ro.cs.tao.orchestration.util;

import org.springframework.stereotype.Component;
import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for shortcutting various operations on ExecutionTasks.
 *
 * @author Cosmin Cara
 */
@Component
public class TaskUtilities {

    private static PersistenceManager persistenceManager;
    private static final Logger logger = Logger.getLogger(TaskUtilities.class.getName());

    /**
     * Setter for the persistence manager
     */
    public static void setPersistenceManager(PersistenceManager manager) {
        persistenceManager = manager;
    }
    /**
     * Returns the component associated with an execution task
     * @param task  The execution task
     */
    public static TaoComponent getComponentFor(ExecutionTask task) {
        if (persistenceManager == null) {
            return null;
        }
        TaoComponent component = null;
        if (task != null) {
            WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
            switch (node.getComponentType()) {
                case PROCESSING:
                    component = persistenceManager.getProcessingComponentById(node.getComponentId());
                    break;
                case DATASOURCE:
                    component = persistenceManager.getDataSourceInstance(node.getComponentId());
                    break;
                case GROUP:
                    component = persistenceManager.getGroupComponentById(node.getComponentId());
                    break;
            }
        }
        return component;
    }

    /**
     * Returns the cardinality of inputs for a given execution task
     * @param task      The task
     */
    public static int getSourceCardinality(ExecutionTask task) {
        TaoComponent component = getComponentFor(task);
        return component != null ? component.getSourceCardinality() : -1;
    }
    /**
     * Returns the cardinality of outputs for a given execution task
     * @param task      The task
     */
    public static int getTargetCardinality(ExecutionTask task) {
        TaoComponent component = getComponentFor(task);
        return component != null ? component.getTargetCardinality() : -1;
    }
    /**
     * Returns the mapping between two tasks. The map keys are the targetTask's inputs, while the map values
     * are the sourceTask's outputs.
     */
    public static Map<String, String> getConnectedInputs(ExecutionTask sourceTask, ExecutionTask targetTask) {
        if (persistenceManager == null) {
            return null;
        }
        Map<String, String> connections = new LinkedHashMap<>();
        WorkflowNodeDescriptor targetNode = persistenceManager.getWorkflowNodeById(targetTask.getWorkflowNodeId());
        WorkflowNodeDescriptor sourceNode = persistenceManager.getWorkflowNodeById(sourceTask.getWorkflowNodeId());
        List<ComponentLink> links = targetNode.getIncomingLinks();
        if (links != null) {
            links.stream()
                    .filter(l -> l.getSourceNodeId() == sourceNode.getId())
                    .forEach(l -> connections.put(l.getOutput().getName(), l.getInput().getName()));
        }
        return connections;
    }

    /**
     * Checks if all the parent tasks of this task have completed.
     *
     * @param task  The task whose parents are to be checked
     */
    public static boolean haveParentsCompleted(ExecutionTask task) {
        boolean completed = true;
        WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        ExecutionJob job = task.getJob();
        List<ComponentLink> links = node.getIncomingLinks();
        if (links != null) {
            for (ComponentLink link : links) {
                ExecutionTask parentTask = persistenceManager.getTaskByJobAndNode(job.getId(), link.getSourceNodeId());
                WorkflowNodeDescriptor parentNode = persistenceManager.getWorkflowNodeById(link.getSourceNodeId());
                completed = (parentNode.getBehavior() == TransitionBehavior.FAIL_ON_ERROR ?
                        parentTask.getExecutionStatus() == ExecutionStatus.DONE &&
                                parentTask.getOutputParameterValues() != null &&
                                parentTask.getOutputParameterValues().stream().anyMatch(o -> o.getValue() != null) :
                        (parentTask.getExecutionStatus() == ExecutionStatus.DONE &&
                                parentTask.getOutputParameterValues() != null &&
                                parentTask.getOutputParameterValues().stream().anyMatch(o -> o.getValue() != null)) ||
                                parentTask.getExecutionStatus() == ExecutionStatus.FAILED);
                if (!completed) {
                    logger.info(String.format("Task %s appears not to be completed", parentTask.getId()));
                    break;
                }
            }
        }
        return completed;
    }

    public static boolean haveAllTasksCompleted(ExecutionJob job) {
        List<ExecutionTask> tasks = job.getTasks();
        return tasks != null &&
                tasks.stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE ||
                                             t.getExecutionStatus() == ExecutionStatus.FAILED ||
                                             t.getExecutionStatus() == ExecutionStatus.CANCELLED);
    }

    /**
     * Transfers the parent outputs to this task, following the defined links on the task.
     *
     * @param toTask    The current task.
     */
    public static ExecutionTask transferParentOutputs(ExecutionTask toTask) {
        if (toTask instanceof DataSourceExecutionTask) {
            throw new IllegalArgumentException("DataSourceExecutionTask cannot accept incoming links");
        }
        WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(toTask.getWorkflowNodeId());
        ExecutionJob job = toTask.getJob();
        List<ComponentLink> links = node.getIncomingLinks();
        if (links != null) {
            for (ComponentLink link : links) {
                ExecutionTask parentTask = persistenceManager.getTaskByJobAndNode(job.getId(), link.getSourceNodeId());
                Variable out = parentTask.getOutputParameterValues()
                        .stream().filter(o -> o.getKey().equals(link.getInput().getName()))
                        .findFirst().orElse(null);
                if (out != null && out.getValue() != null) {
                    toTask.setInputParameterValue(link.getOutput().getName(), out.getValue());
                } else {
                    logger.severe(String.format("No output was set for task %s", parentTask.getId()));
                }
            }
        }
        return toTask;
    }
}
