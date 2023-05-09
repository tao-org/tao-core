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

import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Component;
import ro.cs.tao.component.*;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceTopic;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.ExecutionsManager;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.ProgressNotifier;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.GroupComponentProvider;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.persistence.WorkflowNodeProvider;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.nio.file.Files;
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
    /*private static final ExecutionJobProvider jobProvider;*/
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
        /*jobProvider = SpringContextBridge.services().getService(ExecutionJobProvider.class);*/
        taskProvider = SpringContextBridge.services().getService(ExecutionTaskProvider.class);
        processingComponentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        dataSourceComponentProvider = SpringContextBridge.services().getService(DataSourceComponentProvider.class);
        groupComponentProvider = SpringContextBridge.services().getService(GroupComponentProvider.class);
        containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
    }

    /**
     * Returns the workflow node whose instance is this execution task
     * @param task  The task
     */
    public static WorkflowNodeDescriptor getWorkflowNode(ExecutionTask task) {
        if (task == null || task.getWorkflowNodeId() == null) {
            return null;
        }
        return workflowNodeProvider.get(task.getWorkflowNodeId());
    }

    /**
     * Returns the container of the component wrapped by a workflow node
     * @param node  The workflow node
     */
    public static Container getComponentContainer(WorkflowNodeDescriptor node) {
        final ProcessingComponent component = processingComponentProvider.get(node.getComponentId());
        return containerProvider.get(component.getContainerId());
    }

    public static int getComponentMemoryRequirements(ProcessingComponent component) {
        Container container = containerProvider.get(component.getContainerId());
        Application app = container.getApplications().stream().filter(a -> a.getName().equals(component.getId())).findFirst().orElse(null);
        return app != null ? app.getMemoryRequirements() : 0;
    }

    /**
     * Returns the defined transition behavior for the given task.
     * @param task  The task
     */
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
    /**
     * Returns the component associated with an execution task
     * @param node  The workflow node
     */
    public static TaoComponent getComponentFor(WorkflowNodeDescriptor node) {
        TaoComponent component = null;
        if (node != null) {
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


    /**
     * Returns a list with identifiers of parent tasks for a given task, or null if the task doesn't have any parents
     * @param task  The task
     */
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
    /**
     * Returns the cardinality of inputs for a given component
     * @param component      The component
     */
    public static int getSourceCardinality(TaoComponent component) {
        int cardinality = -1;
        if (component != null) {
            List<SourceDescriptor> sources = component.getSources();
            if (sources != null && sources.size() > 0) {
                /*if (sources.size() == 1) {
                    cardinality = sources.get(0).getCardinality();
                } else {*/
                    cardinality = sources.stream()
                            .max(Comparator.comparingInt(SourceDescriptor::getCardinality))
                            .get().getCardinality();
                /*}*/
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
    /**
     * Returns the cardinality of outputs for a given component
     * @param component      The component
     */
    public static int getTargetCardinality(TaoComponent component) {
        int cardinality = -1;
        if (component != null) {
            List<TargetDescriptor> targets = component.getTargets();
            if (targets != null && targets.size() > 0) {
                /*if (targets.size() == 1) {
                    cardinality = targets.get(0).getCardinality();
                } else {*/
                    cardinality = targets.stream().mapToInt(TargetDescriptor::getCardinality).sum();
                /*}*/
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
        boolean completed = taskProvider.getRunningParents(task.getJob().getId(), task.getId()) == 0;
        if (!completed) {
            logger.fine(String.format("Not all the ancestors of the task %s have completed", task.getId()));
        }
        return completed;
    }

    public static boolean haveAllTasksCompleted(ExecutionJob job) {
        // TODO: replace with a count sql query
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
                        toTask.setInputParameterValue(toSourceName, relativizePathForExecution(outValue, toTask));
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

    /**
     * Checks if this data source task is synchronized with another data source task
     * @param task  The data source task that is inspected
     */
    public static boolean isSynchronized(DataSourceExecutionTask task) {
        final Set<ComponentLink> links = workflowNodeProvider.get(task.getWorkflowNodeId()).getIncomingLinks();
        if (links == null || links.isEmpty()) {
            return false;
        }
        final ComponentLink link = links.iterator().next();
        final String portId = link.getOutput().getId();
        return task.getComponent().getSources().stream().anyMatch(s -> s.getId().equals(portId));

    }

    /**
     * Performs grouping of the outputs of the two data source tasks.
     * Returns a list of pairs [first_task_output, corresponding_second_task_output]
     * @param primaryTask       The primary data source task
     * @param secondaryTask     The secondary data source task (which is synchronized on the primary one)
     */
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
        final Pattern datePattern = Pattern.compile("(\\d{4}[0-1]\\d[0-3]\\d)");
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

    /**
     * Checks if this task is an externally (i.e. not TAO-specific) executing task
     * @param task  The task
     */
    public static boolean isExternal(ExecutionTask task) {
        if (task instanceof ProcessingExecutionTask) {
            final List<ParameterValue> additionalInfo = workflowNodeProvider.get(task.getWorkflowNodeId()).getAdditionalInfo();
            return additionalInfo != null && additionalInfo.stream().anyMatch(p -> "externalTaskId".equals(p.getParameterName()));
        }
        return false;
    }

    /**
     * Returns a friendly name of this task, in the format:
     *      task_id (component_id, job job_id)
     * @param task  The task
     */
    public static String getTaskName(ExecutionTask task) {
        final String component;
        if (task instanceof DataSourceExecutionTask) {
            component = ((DataSourceExecutionTask) task).getComponent().getId();
        } else if (task instanceof ProcessingExecutionTask) {
            component = ((ProcessingExecutionTask) task).getComponent().getId();
        } else if (task instanceof WPSExecutionTask) {
            component = ((WPSExecutionTask) task).getComponent().getId();
        } else {
            component = "unknown component";
        }
        return task.getId() + " (" + component + ", job " + task.getJob().getId() + ")";
    }

    /**
     * Computes a relative to a docker container path of the given path
     * @param path  The path to be made relative
     */
    public static String relativizePathForExecution(String path, ExecutionTask task) {
        if (path.startsWith(DOCKER_MOUNT_POINT) &&
                !path.startsWith(FileUtilities.asUnixPath(SystemVariable.ROOT.value(), true))) {
            // Path is already inside the docker container
            return path;
        }
        String result;
        final String strPath = path.startsWith("[") ? path.substring(1, path.length() - 1) : path;
        try {
            Path p = FileUtilities.toPath(strPath).toAbsolutePath();
            Path absPath = FileUtilities.resolveSymLinks(FileUtilities.toPath(strPath)).toAbsolutePath();
            if (Files.isSymbolicLink(p)) {
                // If the path is a symlink to a /eodata product, maybe we want to temporarily copy it locally
                if (!absPath.startsWith(DOCKER_EODATA_HOST_POINT)) {
                    p = absPath;
                } else {
                    Messaging.send(task.getJob().getUserName(),
                                   Topic.INFORMATION.getCategory(),
                                   "Product " + p.getFileName() + " will be temporarily brought into workspace");
                    final Path linkTarget = FileUtilities.replaceLink(p,
                                                                      new ProgressNotifier(() -> task.getJob().getUserName(),
                                                                                           task.getJob().getName(), DataSourceTopic.PRODUCT_PROGRESS));
                    final Path link = p;
                    ExecutionsManager.getInstance().registerPostExecuteAction(task, () -> {
                        logger.fine("Restoring symlink to " + link);
                        if (Files.isRegularFile(link)) {
                            Files.delete(link);
                        } else {
                            FileUtilities.deleteTreeUnix(link);
                        }
                        FileUtilities.linkFile(linkTarget, link);
                        return null;
                    });
                }
            }
            final Path root;
            final String mountPoint;
            if (DOCKER_EODATA_HOST_POINT != null && p.startsWith(DOCKER_EODATA_HOST_POINT)) {
                root = Paths.get(DOCKER_EODATA_HOST_POINT);
                mountPoint = DOCKER_EODATA_MOUNT_POINT;
            } else {
                root = SystemUtils.IS_OS_WINDOWS
                       ? FileUtilities.resolveSymLinks(Paths.get(SystemVariable.ROOT.value()).toAbsolutePath())
                       : Paths.get(SystemVariable.ROOT.value()).toAbsolutePath();
                mountPoint = DOCKER_MOUNT_POINT;
            }
            final String unixPath = FileUtilities.asUnixPath(root.relativize(p), true);
            result = mountPoint + (mountPoint.endsWith("/") ? "" : "/") + (unixPath.startsWith("/") ? unixPath.substring(1) : unixPath);
        } catch (Exception e1) {
            e1.printStackTrace();
            result = strPath;
        }
        return result;
    }

    /**
     * Returns the DataSourceExecutionTasks of a job
     * @param job   The job
     */
    public static List<DataSourceExecutionTask> findDataSourceTasks(ExecutionJob job) {
        List<ExecutionTask> tasks = taskProvider.getDataSourceTasks(job.getId());
        return tasks.stream().map(t -> (DataSourceExecutionTask) t).collect(Collectors.toList());
    }
}
