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

package ro.cs.tao.orchestration.util;

import org.springframework.stereotype.Component;
import ro.cs.tao.component.*;
import ro.cs.tao.execution.ExecutionConfiguration;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private static final String DOCKER_MOUNT_POINT;

    static {
        String value = "/";
        try {
            value = ExecutionConfiguration.getContainerMount();
            value = value.substring(value.indexOf(':') + 1);
            if (!value.endsWith("/")) {
                value += "/";
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        DOCKER_MOUNT_POINT = value;
    }

    /**
     * Setter for the persistence manager
     */
    public static void setPersistenceManager(PersistenceManager manager) {
        persistenceManager = manager;
    }


    public static WorkflowNodeDescriptor getWorkflowNode(ExecutionTask task) {
        if (task == null || task.getWorkflowNodeId() == null) {
            return null;
        }
        return persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
    }

    public static TransitionBehavior getTransitionBehavior(ExecutionTask task) {
        WorkflowNodeDescriptor node = getWorkflowNode(task);
        return node != null ? node.getBehavior() : TransitionBehavior.FAIL_ON_ERROR;
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
        return getSourceCardinality(component);
    }

    public static int getSourceCardinality(TaoComponent component) {
        int cardinality = -1;
        if (component != null) {
            List<SourceDescriptor> sources = component.getSources();
            if (sources != null && sources.size() > 0) {
                if (sources.size() == 1) {
                    cardinality = sources.get(0).getCardinality();
                } else {
                    cardinality = sources.stream()
                            .max(Comparator.comparingInt(SourceDescriptor::getCardinality))
                            .get().getCardinality();
                }
            }
        }
        return cardinality;
    }

    /**
     * Returns the cardinality of outputs for a given execution task
     * @param task      The task
     */
    public static int getTargetCardinality(ExecutionTask task) {
        TaoComponent component = getComponentFor(task);
        return getSourceCardinality(component);
    }

    public static int getTargetCardinality(TaoComponent component) {
        int cardinality = -1;
        if (component != null) {
            List<TargetDescriptor> targets = component.getTargets();
            if (targets != null && targets.size() > 0) {
                if (targets.size() == 1) {
                    cardinality = targets.get(0).getCardinality();
                } else {
                    cardinality = targets.stream().mapToInt(TargetDescriptor::getCardinality).sum();
                }
            }
        }
        return cardinality;
    }
    /**
     * Returns the mapping between two tasks. The map keys are the targetTask's inputs, while the map values
     * are the sourceTask's outputs.
     */
    public static Map<String, String> getConnectedInputs(ExecutionTask sourceTask, ExecutionTask targetTask) {
        if (persistenceManager == null) {
            return null;
        }

        final WorkflowNodeDescriptor targetNode = persistenceManager.getWorkflowNodeById(targetTask.getWorkflowNodeId());
        final Set<ComponentLink> links = targetNode.getIncomingLinks();
        if (links != null && links.size() > 0) {
            final Map<String, String> connections = new LinkedHashMap<>();
            final WorkflowNodeDescriptor sourceNode = persistenceManager.getWorkflowNodeById(sourceTask.getWorkflowNodeId());
            links.stream()
                    .filter(l -> l.getSourceNodeId() == sourceNode.getId())
                    .forEach(l -> connections.put(l.getOutput().getName(), l.getInput().getName()));
            return connections;
        } else {
            return null;
        }
    }

    /**
     * Checks if all the parent tasks of this task have completed.
     *
     * @param task  The task whose parents are to be checked
     */
    public static boolean haveParentsCompleted(ExecutionTask task) {
        boolean completed = true;
        final WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        final ExecutionJob job = task.getJob();
        final Set<ComponentLink> links = node.getIncomingLinks();
        if (links != null && links.size() > 0) {
            for (ComponentLink link : links) {
                ExecutionTask parentTask = persistenceManager.getTaskByJobAndNode(job.getId(), link.getSourceNodeId(), task.getInstanceId());
                WorkflowNodeDescriptor parentNode = persistenceManager.getWorkflowNodeById(link.getSourceNodeId());
                completed = (parentTask.getExecutionStatus() == ExecutionStatus.DONE &&
                             parentTask.getOutputParameterValues() != null &&
                             parentTask.getOutputParameterValues().stream().anyMatch(o -> o.getValue() != null)) ||
                            (parentNode.getBehavior() == TransitionBehavior.FAIL_ON_ERROR &&
                             parentTask.getExecutionStatus() == ExecutionStatus.FAILED);
                if (!completed) {
                    logger.fine(String.format("Task %s appears not to be completed", parentTask.getId()));
                    break;
                }
            }
        }
        return completed;
    }

    public static boolean haveAllTasksCompleted(ExecutionJob job) {
        final List<ExecutionTask> tasks = job.orderedTasks();
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
        final WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(toTask.getWorkflowNodeId());
        final ExecutionJob job = toTask.getJob();
        final Set<ComponentLink> links = node.getIncomingLinks();
        if (links != null && links.size() > 0) {
            for (ComponentLink link : links) {
                final ExecutionTask parentTask = persistenceManager.getTaskByJobAndNode(job.getId(), link.getSourceNodeId(), toTask.getInstanceId());
                final Variable out = parentTask.getOutputParameterValues()
                        .stream().filter(o -> o.getKey().equals(link.getInput().getName()))
                        .findFirst().orElse(null);
                final String outValue = out != null ? out.getValue() : null;
                if (outValue != null) {
                    toTask.setInputParameterValue(link.getOutput().getName(), outValue);
                } else {
                    logger.severe(String.format("No output was set for task %s", parentTask.getId()));
                }
            }
        }
        return toTask;
    }

    public static String relativizePathForExecution(String path) {
        String result = "";
        try {
            Path p = FileUtilities.resolveSymLinks(Paths.get(URI.create(path)).toAbsolutePath());
            Path root = FileUtilities.resolveSymLinks(Paths.get(SystemVariable.ROOT.value()).toAbsolutePath());
            result = DOCKER_MOUNT_POINT + root.relativize(p).toString().replace("\\", "/");
        } catch (Exception e1) {
            e1.printStackTrace();
            try {
                Path p = FileUtilities.resolveSymLinks(Paths.get(path).toAbsolutePath());
                Path root = FileUtilities.resolveSymLinks(Paths.get(SystemVariable.ROOT.value()).toAbsolutePath());
                result = DOCKER_MOUNT_POINT + root.relativize(p).toString().replace("\\", "/");
            } catch (Exception e2) {
                e2.printStackTrace();
                result = path;
            }
        }
        return result;
    }
}
