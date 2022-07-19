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

package ro.cs.tao.execution.util;

import org.springframework.stereotype.Component;
import ro.cs.tao.Tuple;
import ro.cs.tao.component.*;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.GroupComponentProvider;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.persistence.WorkflowNodeProvider;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for shortcutting various operations on ExecutionTasks.
 *
 * @author Cosmin Cara
 */
@Component
public class TaskUtilities {

    private static final WorkflowNodeProvider workflowNodeProvider;
    private static final ProcessingComponentProvider processingComponentProvider;
    private static final DataSourceComponentProvider dataSourceComponentProvider;
    private static final GroupComponentProvider groupComponentProvider;
    private static final ExecutionJobProvider jobProvider;
    private static final ExecutionTaskProvider taskProvider;
    private static final ContainerProvider containerProvider;
    private static final Logger logger = Logger.getLogger(TaskUtilities.class.getName());
    private static final String DOCKER_MOUNT_POINT;
    private static final String DOCKER_EODATA_MOUNT_POINT;
    private static final String DOCKER_EODATA_HOST_POINT;

    static {
        DOCKER_MOUNT_POINT = ExecutionConfiguration.getMasterContainerVolumeMap().getContainerWorkspaceFolder() + "/";
        DOCKER_EODATA_HOST_POINT = ExecutionConfiguration.getMasterContainerVolumeMap().getHostEODataFolder();
        DOCKER_EODATA_MOUNT_POINT = ExecutionConfiguration.getMasterContainerVolumeMap().getContainerEoDataFolder();
        workflowNodeProvider = SpringContextBridge.services().getService(WorkflowNodeProvider.class);
        jobProvider = SpringContextBridge.services().getService(ExecutionJobProvider.class);
        taskProvider = SpringContextBridge.services().getService(ExecutionTaskProvider.class);
        processingComponentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        dataSourceComponentProvider = SpringContextBridge.services().getService(DataSourceComponentProvider.class);
        groupComponentProvider = SpringContextBridge.services().getService(GroupComponentProvider.class);
        containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
    }

    public static WorkflowNodeDescriptor getWorkflowNode(ExecutionTask task) {
        if (task == null || task.getWorkflowNodeId() == null) {
            return null;
        }
        return workflowNodeProvider.get(task.getWorkflowNodeId());
    }

    public static Container getComponentContainer(WorkflowNodeDescriptor node) {
        final ProcessingComponent component = processingComponentProvider.get(node.getComponentId());
        return containerProvider.get(component.getContainerId());
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
        TaoComponent component = null;
        if (task != null) {
            WorkflowNodeDescriptor node = workflowNodeProvider.get(task.getWorkflowNodeId());
            switch (node.getComponentType()) {
                case PROCESSING:
                    component = processingComponentProvider.get(node.getComponentId());
                    break;
                case DATASOURCE:
                    component = dataSourceComponentProvider.get(node.getComponentId());
                    break;
                case GROUP:
                    component = groupComponentProvider.get(node.getComponentId());
                    break;
            }
        }
        return component;
    }

    public static List<Long> getParentIds(ExecutionTask task) {
        List<Long> ids = null;
        final WorkflowNodeDescriptor node = getWorkflowNode(task);
        final Set<ComponentLink> links = node.getIncomingLinks();
        if (links != null) {
            ids = links.stream().map(l -> taskProvider.getByJobAndNode(task.getJob().getId(), l.getSourceNodeId(), task.getInstanceId()).getId()).distinct().collect(Collectors.toList());
        }
        return ids;
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
        if (task.getCardinality() != null) {
            return task.getCardinality();
        }
        TaoComponent component = getComponentFor(task);
        return getTargetCardinality(component);
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
        final WorkflowNodeDescriptor targetNode = workflowNodeProvider.get(targetTask.getWorkflowNodeId());
        final Set<ComponentLink> links = targetNode.getIncomingLinks();
        if (links != null && links.size() > 0) {
            final Map<String, String> connections = new LinkedHashMap<>();
            final WorkflowNodeDescriptor sourceNode = workflowNodeProvider.get(sourceTask.getWorkflowNodeId());
            links.stream()
                    .filter(l -> l.getSourceNodeId() == sourceNode.getId())
                    .forEach(l -> connections.put(l.getOutput().getName(), l.getInput().getName()));
            return connections;
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Checks if all the parent tasks of this task have completed.
     *
     * @param task  The task whose parents are to be checked
     */
    public static boolean haveParentsCompleted(ExecutionTask task) {
        boolean completed = false;
        final ExecutionJob job = jobProvider.get(task.getJob().getId()); // make sure job tasks are in the latest state from DB
        final List<String> strPIds = job.getTaskDependencies().get(String.valueOf(task.getId()));
        if (strPIds != null) {
            Set<Long> ids = strPIds.stream().map(Long::parseLong).collect(Collectors.toSet());
            completed = job.getTasks()
                    .stream().filter(t -> ids.contains(t.getId()))
                    .allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE ||
                                  (t.getExecutionStatus() == ExecutionStatus.FAILED &&
                                   workflowNodeProvider.get(t.getWorkflowNodeId()).getBehavior() == TransitionBehavior.CONTINUE_ON_ERROR));
            if (!completed) {
                logger.fine(String.format("Not all the ancestors of the task %s have completed", task.getId()));
            }
        }
        /*final WorkflowNodeDescriptor node = workflowNodeProvider.get(task.getWorkflowNodeId());
        final ExecutionJob job = job;
        final Set<ComponentLink> links = node.getIncomingLinks();
        if (links != null && links.size() > 0) {
            for (ComponentLink link : links) {
                ExecutionTask parentTask = taskProvider.getByJobAndNode(job.getId(), link.getSourceNodeId(), task.getInstanceId());
                WorkflowNodeDescriptor parentNode = workflowNodeProvider.get(link.getSourceNodeId());
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
        }*/
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
    public static void transferParentOutputs(ExecutionTask toTask) {
        if (toTask instanceof DataSourceExecutionTask) {
            throw new IllegalArgumentException("DataSourceExecutionTask cannot accept incoming links");
        }
        final WorkflowNodeDescriptor node = workflowNodeProvider.get(toTask.getWorkflowNodeId());
        //final ExecutionJob job = toTask.getJob();
        final Set<ComponentLink> links = node.getIncomingLinks();
        if (links != null && links.size() > 0) {
            for (ComponentLink link : links) {
                final ExecutionTask parentTask = taskProvider.getByJobAndNode(toTask.getJob().getId(),
                                                                              link.getSourceNodeId(), toTask.getInstanceId());
                final Variable out = parentTask.getOutputParameterValues()
                        .stream().filter(o -> o.getKey().equals(link.getInput().getName()))
                        .findFirst().orElse(null);
                final String outValue = out != null ? out.getValue() : null;
                if (outValue != null) {
                    final String toSourceName = link.getOutput().getName();
                    final Variable inputVar = toTask.getInputParameterValues().stream().filter(p -> p.getKey().equals(toSourceName)).findFirst().get();
                    final boolean isExternal = isExternal(parentTask);
                    if (!isExternal || inputVar.getValue() == null) {
                        toTask.setInputParameterValue(toSourceName, relativizePathForExecution(outValue));
                        logger.finest(String.format("Transferred output [%s] of task %s to task %s",
                                                    toSourceName, parentTask.getId(), toTask.getId()));
                    } else {
                        logger.finest(String.format("Output [%s] of task %s was NOT transferred to task %s [%s]",
                                                    toSourceName, parentTask.getId(), toTask.getId(),
                                String.format("link destination %s already set", toSourceName)));
                    }
                } else {
                    logger.severe(String.format("No output was set for task %s", parentTask.getId()));
                }
            }
        }
    }

    public static boolean isSynchronized(DataSourceExecutionTask task) {
        final Set<ComponentLink> links = workflowNodeProvider.get(task.getWorkflowNodeId()).getIncomingLinks();
        if (links == null || links.isEmpty()) {
            return false;
        }
        final ComponentLink link = links.iterator().next();
        final String portId = link.getOutput().getId();
        return task.getComponent().getSources().stream().anyMatch(s -> s.getId().equals(portId));

    }

    public static List<Tuple<String, String>> coupleTaskOutputs(DataSourceExecutionTask primaryTask,
                                                                DataSourceExecutionTask secondaryTask) {
        final SourceDescriptor sdTwo = secondaryTask.getComponent().getSources().stream()
                                              .filter(s -> DataSourceComponent.SYNC_PARAMETER.equals(s.getName()))
                                              .findFirst().orElse(null);
        if (sdTwo == null) {
            throw new IllegalArgumentException("Data sources not synchronized");
        }
        final List<Variable> firstValues = primaryTask.getOutputParameterValues();
        if (firstValues == null || firstValues.size() != 1) {
            throw new ExecutionException("Primary data source didn't return any results");
        }
        final List<Variable> secondValues = secondaryTask.getOutputParameterValues();
        if (secondValues == null || secondValues.size() != 1) {
            throw new ExecutionException("Secondary data source didn't return any results");
        }
        final String firstStringList = firstValues.get(0).getValue();
        final String secondStringList = secondValues.get(0).getValue();
        final StringListAdapter adapter = new StringListAdapter();
        final List<String> firstList = adapter.marshal(firstStringList);
        final List<String> secondList = adapter.marshal(secondStringList);
        final Pattern datePattern = Pattern.compile("([0-9]{4}[0-1][0-9][0-3][0-9])");
        final List<Tuple<String, String>> results = new ArrayList<>();
        for (String firstValue : firstList) {
            final Matcher matcher = datePattern.matcher(firstValue);
            if (matcher.find()) {
                final String date = matcher.group(1);
                final String secondValue = secondList.stream().filter(v -> v.contains(date)).findFirst().orElse(null);
                results.add(new Tuple<>(firstValue, secondValue));
            }
        }
        return results;
    }

    public static boolean isExternal(ExecutionTask task) {
        if (task instanceof ProcessingExecutionTask) {
            final List<ParameterValue> additionalInfo = workflowNodeProvider.get(task.getWorkflowNodeId()).getAdditionalInfo();
            return additionalInfo != null && additionalInfo.stream().anyMatch(p -> "externalTaskId".equals(p.getParameterName()));
        }
        return false;
    }

    public static String getTaskName(ExecutionTask task) {
        final String component;
        if (task instanceof DataSourceExecutionTask) {
            component = ((DataSourceExecutionTask) task).getComponent().getId();
        } else if (task instanceof ProcessingExecutionTask) {
            component = ((ProcessingExecutionTask) task).getComponent().getId();
        } else {
            component = "";
        }
        return task.getId() + " (" + component + ", job " + task.getJob().getId() + ")";
    }

    public static String relativizePathForExecution(String path) {
        String result;
        try {
            Path p = FileUtilities.resolveSymLinks(Paths.get(URI.create(path)).toAbsolutePath());
            if (DOCKER_EODATA_HOST_POINT != null && p.startsWith(DOCKER_EODATA_HOST_POINT)) {
                Path root = Paths.get(DOCKER_EODATA_HOST_POINT);
                final String unixPath = FileUtilities.asUnixPath(root.relativize(p), true);
                result = DOCKER_EODATA_MOUNT_POINT + "/" + (unixPath.startsWith("/") ? unixPath.substring(1) : unixPath);
            } else {
                Path root = FileUtilities.resolveSymLinks(Paths.get(SystemVariable.ROOT.value()).toAbsolutePath());
                final String unixPath = FileUtilities.asUnixPath(root.relativize(p), true);
                result = DOCKER_MOUNT_POINT + (unixPath.startsWith("/") ? unixPath.substring(1) : unixPath);
            }
        } catch (Exception e1) {
            //e1.printStackTrace();
            try {
                Path p = FileUtilities.resolveSymLinks(Paths.get(path).toAbsolutePath());
                if (DOCKER_EODATA_HOST_POINT != null && p.startsWith(DOCKER_EODATA_HOST_POINT)) {
                    Path root = Paths.get(DOCKER_EODATA_HOST_POINT);
                    final String unixPath = FileUtilities.asUnixPath(root.relativize(p), true);
                    result = DOCKER_EODATA_MOUNT_POINT + "/" + (unixPath.startsWith("/") ? unixPath.substring(1) : unixPath);
                } else {
                    Path root = FileUtilities.resolveSymLinks(Paths.get(SystemVariable.ROOT.value()).toAbsolutePath());
                    final String unixPath = FileUtilities.asUnixPath(root.relativize(p), true);
                    result = DOCKER_MOUNT_POINT + (unixPath.startsWith("/") ? unixPath.substring(1) : unixPath);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                result = path;
            }
        }
        return result;
    }
}
