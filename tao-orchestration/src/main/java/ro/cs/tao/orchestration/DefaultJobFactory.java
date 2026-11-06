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

import ro.cs.tao.component.*;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.component.ogc.WMSComponent;
import ro.cs.tao.component.ogc.WPSComponent;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.datasource.param.CommonParameterNames;
import ro.cs.tao.datasource.persistence.DataSourceComponentGroupProvider;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.datasource.persistence.QueryProvider;
import ro.cs.tao.drmaa.Environment;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.execution.util.ComponentUtilities;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.persistence.*;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Factory class for creating execution jobs.
 *
 * @author Cosmin Cara
 */
public class DefaultJobFactory implements JobFactory {

    protected static final int DEFAULT_INSTANCE = 1;
    protected static final String invalidChars = "[\\(\\)\\[\\]\\~\\#\\%\\&\\*\\{\\}\\\\\\:\\<\\>\\?\\/\\+\\|, ]";
    protected final ExecutionJobProvider jobProvider;
    protected final ExecutionTaskProvider taskProvider;
    protected final QueryProvider queryProvider;
    protected final ProcessingComponentProvider processingComponentProvider;
    protected final ConditionComponentProvider conditionComponentProvider;
    protected final WPSComponentProvider wpsComponentProvider;
    protected final WMSComponentProvider wmsComponentProvider;
    protected final DataSourceComponentProvider dataSourceComponentProvider;
    protected final DataSourceComponentGroupProvider dataSourceComponentGroupProvider;
    protected final GroupComponentProvider groupComponentProvider;
    protected final WorkflowNodeProvider workflowNodeProvider;
    protected final Logger logger;

    DefaultJobFactory() {
        this.jobProvider = SpringContextBridge.services().getService(ExecutionJobProvider.class);
        this.taskProvider = SpringContextBridge.services().getService(ExecutionTaskProvider.class);
        this.queryProvider = SpringContextBridge.services().getService(QueryProvider.class);
        this.processingComponentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        this.wpsComponentProvider = SpringContextBridge.services().getService(WPSComponentProvider.class);
        this.wmsComponentProvider = SpringContextBridge.services().getService(WMSComponentProvider.class);
        this.dataSourceComponentProvider = SpringContextBridge.services().getService(DataSourceComponentProvider.class);
        this.dataSourceComponentGroupProvider = SpringContextBridge.services().getService(DataSourceComponentGroupProvider.class);
        this.groupComponentProvider = SpringContextBridge.services().getService(GroupComponentProvider.class);
        this.workflowNodeProvider = SpringContextBridge.services().getService(WorkflowNodeProvider.class);
        this.conditionComponentProvider = SpringContextBridge.services().getService(ConditionComponentProvider.class);
        this.logger = Logger.getLogger(DefaultJobFactory.class.getName());
    }

    @Override
    public ExecutionJob createJob(SessionContext context, String appId, String jobName, WorkflowDescriptor workflow,
                                  Map<String, Map<String, String>> inputs, JobType jobType, Environment environment)
            throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setName(jobName);
            job.setAppId(appId);
            job.setJobType(jobType);
            job.setBatchId(UUID.randomUUID().toString());
            job.setUserId(context.getPrincipal().getName());
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            job.setJobOutputPath(Paths.get(SystemVariable.ROOT.value())
                                      .resolve(context.getPrincipal().getName())
                                      .resolve(jobName.replaceAll(invalidChars, "_"))
                                      .toString());
            job.setEnvironment(environment);
            job = jobProvider.save(job);
            List<WorkflowNodeDescriptor> nodes = workflow.getOrderedNodes();
            Set<WorkflowNodeGroupDescriptor> groups = nodes.stream()
                    .filter(n -> n instanceof WorkflowNodeGroupDescriptor)
                    .map(n -> (WorkflowNodeGroupDescriptor) n)
                    .collect(Collectors.toSet());
            final boolean preComputeOutputs = job.getJobType() != JobType.EXECUTION || job.getEnvironment() == Environment.SIMPIPE;
            for (WorkflowNodeDescriptor node : nodes) {
                // A workflow contains also the nodes of a node group.
                // Hence, in order not to duplicate the tasks, the nodes from group are temporary removed from workflow.
                if (groups.stream().noneMatch(g -> g.getNodes().stream()
                        .anyMatch(c -> c.getId().equals(node.getId())))) {
                    Map<String, String> taskInputs = null;
                    if (inputs != null) {
                        String key = node.getName();
                        if (inputs.containsKey(key)) {
                            taskInputs = inputs.get(node.getName());
                        } else {
                            key = node.getId() + ":" + node.getName();
                            taskInputs = inputs.get(key);
                        }
                    }
                    ExecutionTask task = createTask(job, workflow, node, taskInputs);
                    task.setLevel(node.getLevel());
                    if (task.getId() == null) {
                        task = taskProvider.save(task, job);
                    } else {
                        task = taskProvider.update(task);
                    }
                    // In the case of script tasks, outputs should be pre-computed as they will not be set by the Orchestrator
                    if (preComputeOutputs && task instanceof ScriptTask) {
                        ScriptTask scriptTask = (ScriptTask) task;
                        final List<TargetDescriptor> targets = scriptTask.getComponent().getTargets();
                        if (targets != null) {
                            targets.forEach(t -> scriptTask.setOutputParameterValue(t.getName(),
                                                                                    scriptTask.computeTargetOutput(t.getName(),
                                                                                                                   t.getDataDescriptor().getLocation())));
                            taskProvider.update(scriptTask);
                        }

                    }
                    logger.finest(String.format("Created task %s.%s for node %s [id=%s, level %s]",
                                                jobName, task.getId(), node.getName(), node.getId(), task.getLevel()));
                }
            }
            // re-iterate to determine task dependencies
            final List<ExecutionTask> tasks = job.getTasks();
            if (tasks != null) {
                for (ExecutionTask task : tasks) {
                    final List<Long> parentIds = TaskUtilities.getParentIds(task);
                    if (parentIds != null) {
                        for (Long parentId : parentIds) {
                            job.addTaskDependency(task.getId(), parentId);
                        }
                    }
                }
                jobProvider.update(job);
            }
        }
        return job;
    }

    @Override
    public ExecutionJob createWPSJob(SessionContext context, String appId, String jobName, Map<String, Map<String, String>> inputs)
            throws PersistenceException {
        ExecutionJob job = new ExecutionJob();
        job.setName(jobName);
        job.setAppId(appId);
        job.setBatchId(UUID.randomUUID().toString());
        job.setUserId(context.getPrincipal().getName());
        job.setStartTime(LocalDateTime.now());
        job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        job.setJobType(JobType.EXECUTION);
        job = jobProvider.save(job);
        for (Map.Entry<String, Map<String, String>> entry : inputs.entrySet()) {
            ExecutionTask task = createWPSComponentExecutionTask(entry.getKey(), entry.getValue());
            task.setLevel(1);
            if (task.getId() == null) {
                taskProvider.save(task, job);
            } else {
                taskProvider.update(task);
            }
            logger.finest(String.format("Created WPS task %s.%s", jobName, task.getId()));
        }
        return job;
    }

    @Override
    public ExecutionJob createWMSJob(SessionContext context, String appId, String jobName, Map<String, String> inputs)
            throws PersistenceException {
        ExecutionJob job = new ExecutionJob();
        job.setName(jobName);
        job.setAppId(appId);
        job.setBatchId(UUID.randomUUID().toString());
        job.setUserId(context.getPrincipal().getName());
        job.setStartTime(LocalDateTime.now());
        job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        job.setJobType(JobType.EXECUTION);
        job.setJobOutputPath(Paths.get(SystemVariable.ROOT.value())
                                  .resolve(context.getPrincipal().getName())
                                  .resolve(jobName.replaceAll(invalidChars, "_"))
                                  .toString());
        job = jobProvider.save(job);
        ExecutionTask task = createWMSComponentExecutionTask(jobName, inputs);
        task.setLevel(1);
        if (task.getId() == null) {
            taskProvider.save(task, job);
        } else {
            taskProvider.update(task);
        }
        logger.finest(String.format("Created WMS task %s.%s", jobName, task.getId()));
        return job;
    }

    /**
     * Creates a task group for the specified execution job and workflow group node.
     * The method initializes an {@link ExecutionGroup}, assigns tasks based on the provided
     * workflow group node, and manages their parallelism and dependencies.
     *
     * @param job The execution job for which the task group is created.
     * @param workflow The workflow descriptor containing workflow details.
     * @param groupNode The group node descriptor representing a collection of workflow nodes.
     * @param inputs A map of input parameters and their values passed to the tasks.
     * @return An {@link ExecutionTask} representing the created task group.
     * @throws PersistenceException If there is an error during task group persistence.
     */
    protected ExecutionTask createTaskGroup(ExecutionJob job, WorkflowDescriptor workflow, WorkflowNodeGroupDescriptor groupNode,
                                            Map<String, String> inputs)
            throws PersistenceException {
        final ExecutionGroup group = new ExecutionGroup();
        group.setWorkflowNodeId(groupNode.getId());
        group.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        group.setResourceId("group-" + groupNode.getId() + "-" + job.getId());
        taskProvider.save(group, job);
        List<WorkflowNodeDescriptor> nodes = groupNode.getOrderedNodes();
        final GroupComponent component = groupComponentProvider.get(groupNode.getComponentId());
        final int parallelism = component != null ? component.getParallelism() : 1;
        for (WorkflowNodeDescriptor node : nodes) {
            for (int i = 1; i <= parallelism; i++) {
                ExecutionTask task = createTask(job, workflow, node, inputs);
                //task.setLevel(groupNode.getLevel() + 1);
                task.setLevel(node.getLevel());
                task.setInstanceId(i);
                taskProvider.save(task, job);
                group.addTask(task);
            }
        }
        List<ExecutionTask> tasks = group.getTasks();
        if (tasks != null && !tasks.isEmpty()) {
            Set<ComponentLink> links = groupNode.getIncomingLinks();
            links.forEach(link -> {
                String name = link.getOutput().getName();
                group.setInputParameterValue(name, null);
            });
            if (component != null) {
                // Placeholders for outputs of this task
                List<TargetDescriptor> targets = component.getTargets();
                targets.forEach(t -> group.setOutputParameterValue(t.getName(), null));
            }
        }
        return group;
    }

    /**
     * Creates an {@link ExecutionTask} representing a group task for a data source within the
     * specified execution job and workflow node. This method initializes an {@link ExecutionGroup},
     * assigns tasks based on the provided workflow node and its associated data source components,
     * and persists the tasks and group in the context of the provided execution job.
     *
     * @param job The execution job for which the data source group task is created.
     * @param groupNode The workflow node descriptor representing the data source group to process.
     * @return An {@link ExecutionTask} that represents the created group task.
     * @throws PersistenceException If there is an error while persisting the group or tasks.
     */
    protected  ExecutionTask createDataSourceGroupTask(ExecutionJob job, WorkflowNodeDescriptor groupNode)
            throws PersistenceException {
        ExecutionGroup group = new ExecutionGroup();
        group.setWorkflowNodeId(groupNode.getId());
        group.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        group.setResourceId("group-" + groupNode.getId() + "-" + job.getId());
        taskProvider.save(group, job);
        DataSourceComponentGroup dscGroup = dataSourceComponentGroupProvider.get(groupNode.getComponentId());
        List<DataSourceComponent> components = dscGroup.getDataSourceComponents();
        for (DataSourceComponent component : components) {
            Query query = dscGroup.getDataSourceQueries().stream()
                    .filter(q -> component.getId().equals(q.getComponentId())).findFirst().orElse(null);
            if (query != null) {
                ExecutionTask task = createDataSourceExecutionTask(job, groupNode, component, query);
                task.setLevel(groupNode.getLevel());
                taskProvider.save(task, job);
                group.addTask(task);
            } else {
                logger.warning(String.format("No query is associated with data source '%s'", component.getId()));
            }
        }
        return group;
    }

    /**
     * Creates an execution task for the specified execution job and workflow node.
     * Based on the type of workflow node, this method initializes the appropriate
     * type of {@link ExecutionTask}, sets its properties such as workflow node ID
     * and execution status, and returns the created task. If the workflow node
     * represents a group, a task group is created instead.
     *
     * @param job The execution job for which the task is being created.
     * @param workflow The workflow descriptor containing the details of the workflow.
     * @param workflowNode The node descriptor representing the workflow node for which the task is being created.
     * @param inputs A map of input parameters and their values to be provided to the created task.
     * @return The created {@link ExecutionTask} representing the task for the specified workflow node.
     * @throws PersistenceException If there is an error during task creation or persistence.
     */
    protected ExecutionTask createTask(ExecutionJob job, WorkflowDescriptor workflow, WorkflowNodeDescriptor workflowNode,
                                     Map<String, String> inputs)
            throws PersistenceException {
        ExecutionTask task;
        if (workflowNode instanceof WorkflowNodeGroupDescriptor) {
            task = createTaskGroup(job, workflow, (WorkflowNodeGroupDescriptor) workflowNode, inputs);
        } else {
            Set<ComponentLink> links = workflowNode.getIncomingLinks();
            switch (workflowNode.getComponentType()) {
                case PROCESSING:
                    task = createProcessingExecutionTask(job, workflowNode, links, inputs);
                    break;
                case WPS_COMPONENT:
                    task = createWPSComponentExecutionTask(workflowNode, links, inputs);
                    break;
                case DATASOURCE:
                    task = createDataSourceExecutionTask(job, workflowNode, inputs);
                    break;
                case DATASOURCE_GROUP:
                    task = createDataSourceGroupTask(job, workflowNode);
                    break;
                case CONDITION:
                    task = createConditionExecutionTask(workflowNode, links, inputs);
                    break;
                case GROUP:
                default:
                    throw new PersistenceException("Wrong component type");
            }
            task.setWorkflowNodeId(workflowNode.getId());
            task.setInstanceId(DEFAULT_INSTANCE);
            task.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        }
        return task;
    }

    /**
     * Creates and configures a new {@link ProcessingExecutionTask} based on the provided execution job,
     * workflow node descriptor, input links, and additional input parameters.
     *
     * @param job           The {@link ExecutionJob} associated with this processing task. This contains
     *                      metadata and contextual information about the execution environment and user.
     * @param workflowNode  The {@link WorkflowNodeDescriptor} representing the workflow node to be processed.
     *                      It defines the component to execute and any custom parameter values.
     * @param incomingLinks A set of {@link ComponentLink} objects that represent connections from prior
     *                      tasks or components in the workflow. These are used to configure input placeholders.
     * @param inputs        A map of additional input parameter names and their corresponding values. These
     *                      are used to override or supplement task-specific input configurations.
     * @return A fully configured {@link ProcessingExecutionTask} instance containing all necessary
     *         parameters, component configurations, and execution settings.
     */
    protected ExecutionTask createProcessingExecutionTask(ExecutionJob job,
                                                          WorkflowNodeDescriptor workflowNode, Set<ComponentLink> incomingLinks,
                                                          Map<String, String> inputs) {
        final List<ParameterValue> customValues = workflowNode.getCustomValues();
        final ProcessingComponent component = processingComponentProvider.get(workflowNode.getComponentId());
        final ProcessingExecutionTask task = new ProcessingExecutionTask();
        task.setComponent(component);
        // Placeholders for inputs of previous tasks
        if (incomingLinks != null) {
            incomingLinks.stream().sorted(new Comparator<ComponentLink>() {
                @Override
                public int compare(ComponentLink o1, ComponentLink o2) {
                    Aggregator a1 = o1.getAggregator();
                    Aggregator a2 = o2.getAggregator();
                    return a1 != null && a2 != null && a1.getOrder() != null && a2.getOrder() != null
                            ? Integer.compare(a1.getOrder(), a2.getOrder())
                            : 0;
                }
            }).forEach(link -> {
                String name = link.getOutput().getName();
                task.setInputParameterValue(name, null);
            });
        }
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        ParameterValue affinityParam = null;
        if (customValues != null) {
            for (ParameterValue param : customValues) {
                switch (param.getParameterName()) {
                    case "nodeAffinity":
                        affinityParam = param;
                        break;
                    case "formatName":
                        task.setExternalCommonParameters(new HashSet<String>() {{
                                add("formatName");
                            }});
                        task.setOutputParameterValue(param.getParameterName(), param.getParameterValue());
                        break;
                    default:
                        task.setInputParameterValue(param.getParameterName(), param.getParameterValue());
                        break;
                }
            }
        }
        final NodeAffinity nodeAffinity = affinityParam != null
                ? NodeAffinity.of(affinityParam.getParameterValue())
                : component.getNodeAffinity();
        if (nodeAffinity != null
                && !(NodeAffinity.Any.equals(nodeAffinity) || NodeAffinity.SameNode.equals(nodeAffinity))) {
            task.setExecutionNodeHostName(nodeAffinity.getValue());
        }
        if (component.getComponentType() == ProcessingComponentType.EXTERNAL && customValues != null) {
            task.setCommand(component.buildExecutionCommand(customValues.stream().collect(Collectors.toMap(ParameterValue::getParameterName, ParameterValue::getParameterValue)),
                                                            null, job.getUserId()));
        }
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
        }
        task.setCardinality(ComponentUtilities.getTargetCardinality(component));
        task.setUsedRAM(ComponentUtilities.getComponentMemoryRequirements(component));
        return task;
    }

    /**
     * Creates and returns a {@link DataSourceExecutionTask} for a given execution job and workflow node.
     * The method initializes the task with inputs, performs query operations, and sets the necessary parameters.
     *
     * @param job the {@link ExecutionJob} for which the task is being created
     * @param workflowNode the {@link WorkflowNodeDescriptor} representing the node within the workflow
     * @param inputs a map of input parameters to be used for the task
     * @return a fully initialized {@link DataSourceExecutionTask}
     * @throws PersistenceException if there is an error interacting with the persistence layer during query operations
     */
    protected ExecutionTask createDataSourceExecutionTask(ExecutionJob job, WorkflowNodeDescriptor workflowNode,
                                                          Map<String, String> inputs) throws PersistenceException {
        DataSourceExecutionTask task = new DataSourceExecutionTask();
        final Map<String, String> taskInputs;
        List<ParameterValue> customValues = workflowNode.getCustomValues();
        if (customValues != null) {
            taskInputs = new HashMap<>(customValues.stream()
                                               .collect(Collectors.toMap(ParameterValue::getParameterName,
                                                                         ParameterValue::getParameterValue)));
        } else {
            taskInputs = new HashMap<>();
        }
        if (inputs != null) {
            taskInputs.putAll(inputs);
        }
        DataSourceComponent dsComponent = dataSourceComponentProvider.get(workflowNode.getComponentId());
        Query query = queryProvider.get(job.getUserId(),
                                        dsComponent.getSensorName(),
                                        dsComponent.getDataSourceName(),
                                        workflowNode.getId());
        if (query == null) {
            query = new Query();
            query.setUser(job.getUserId());
            query.setSensor(dsComponent.getSensorName());
            query.setDataSource(dsComponent.getDataSourceName());
            query.setWorkflowNodeId(workflowNode.getId());
            if (!dsComponent.getSystem() && dsComponent.getDataSourceName().equals("Local Database") &&
                    dsComponent.getSources() != null && dsComponent.getSources().size() == 1) {
                SourceDescriptor source = dsComponent.getSources().get(0);
                String location = source.getDataDescriptor().getLocation();
                if (location != null) {
                    taskInputs.put(CommonParameterNames.PRODUCT, "[" + location + "]");
                    taskInputs.put("pageNumber", "1");
                    taskInputs.put("pageSize", String.valueOf(source.getCardinality()));
                    taskInputs.put("limit", String.valueOf(source.getCardinality()));
                }
            }
        }
        query.setPageNumber(Integer.parseInt(taskInputs.getOrDefault("pageNumber", "1")));
        taskInputs.remove("pageNumber");
        query.setPageSize(Integer.parseInt(taskInputs.getOrDefault("pageSize", "1")));
        taskInputs.remove("pageSize");
        query.setLimit(Integer.parseInt(taskInputs.getOrDefault("limit", "1")));
        taskInputs.remove("limit");
        query.setValues(taskInputs);
        query.setUserId(job.getUserId());
        if (query.getId() == null || query.getId() == 0) {
            queryProvider.save(query);
        } else {
            queryProvider.update(query);
        }
        task.setComponent(dsComponent);
        task.setCardinality(ComponentUtilities.getTargetCardinality(dsComponent));
        task.setInputParameterValue(DataSourceComponent.QUERY_PARAMETER, query.toString());
        return task;
    }

    /**
     * Creates and configures a DataSourceExecutionTask for the given execution job, workflow node,
     * data source component, and query. The task prepares and persists a local query representation,
     * processes custom values, and sets task-specific input parameters.
     *
     * @param job The execution job triggering the creation of the data source execution task.
     * @param workflowNode The descriptor of the workflow node for which the task is being created.
     * @param component The data source component defining the context of the task.
     * @param query The query to be modified and attached to the execution task.
     * @return A configured instance of DataSourceExecutionTask for execution.
     * @throws PersistenceException If an error occurs while saving the query or interacting with the persistence layer.
     */
    protected ExecutionTask createDataSourceExecutionTask(ExecutionJob job, WorkflowNodeDescriptor workflowNode,
                                                          DataSourceComponent component, Query query)
            throws PersistenceException {
        DataSourceExecutionTask task = new DataSourceExecutionTask();
        final Map<String, String> taskInputs;
        List<ParameterValue> customValues = workflowNode.getCustomValues();
        if (customValues != null) {
            taskInputs = new HashMap<>(customValues.stream()
                                                   .collect(Collectors.toMap(ParameterValue::getParameterName,
                                                                             ParameterValue::getParameterValue)));
        } else {
            taskInputs = new HashMap<>();
        }
        Query localQuery = queryProvider.get(job.getUserId(),
                                             component.getSensorName(),
                                             component.getDataSourceName(),
                                             workflowNode.getId());
        if (localQuery == null) {
            localQuery = new Query();
            localQuery.setUser(job.getUserId());
            localQuery.setSensor(component.getSensorName());
            localQuery.setDataSource(component.getDataSourceName());
            localQuery.setWorkflowNodeId(workflowNode.getId());
            if (!component.getSystem() && component.getDataSourceName().equals("Local Database") &&
                    component.getSources() != null && component.getSources().size() == 1) {
                SourceDescriptor source = component.getSources().get(0);
                String location = source.getDataDescriptor().getLocation();
                if (location != null) {
                    taskInputs.put("name", "[" + location + "]");
                    taskInputs.put("pageNumber", "1");
                    taskInputs.put("pageSize", String.valueOf(source.getCardinality()));
                    taskInputs.put("limit", String.valueOf(source.getCardinality()));
                }
            }
        }
        query.setPageNumber(Integer.parseInt(taskInputs.getOrDefault("pageNumber", "1")));
        taskInputs.remove("pageNumber");
        query.setPageSize(Integer.parseInt(taskInputs.getOrDefault("pageSize", "1")));
        taskInputs.remove("pageSize");
        query.setLimit(Integer.parseInt(taskInputs.getOrDefault("limit", "1")));
        taskInputs.remove("limit");
        localQuery.setValues(taskInputs);
        localQuery.setUserId(job.getUserId());
        queryProvider.save(localQuery);
        task.setComponent(component);
        task.setCardinality(ComponentUtilities.getTargetCardinality(component));
        task.setInputParameterValue("local.query", localQuery.toString());
        task.setInputParameterValue("remote.query", query.toString());
        return task;
    }

    /**
     * Creates a WPS component execution task for a given workflow node. This method initializes
     * the task with input and output parameter placeholders, custom parameter values, node affinity,
     * and any additional input mappings provided.
     *
     * @param workflowNode the descriptor of the workflow node for which the task is being created.
     * @param incomingLinks a set of component links representing the input connections to the workflow node.
     * @param inputs a map of input parameter names and their respective values to be set for the task.
     * @return a fully configured {@link WPSExecutionTask} ready for execution.
     */
    protected WPSExecutionTask createWPSComponentExecutionTask(WorkflowNodeDescriptor workflowNode, Set<ComponentLink> incomingLinks,
                                                               Map<String, String> inputs) {
        final List<ParameterValue> customValues = workflowNode.getCustomValues();
        final WPSComponent component = wpsComponentProvider.get(workflowNode.getComponentId());
        final WPSExecutionTask task = new WPSExecutionTask();
        task.setComponent(component);
        // Placeholders for inputs of previous tasks
        if (incomingLinks != null) {
            incomingLinks.forEach(link -> {
                String name = link.getOutput().getName();
                task.setInputParameterValue(name, null);
            });
        }
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        ParameterValue affinityParam = null;
        if (customValues != null) {
            for (ParameterValue param : customValues) {
                if (!"nodeAffinity".equals(param.getParameterName())) {
                    task.setInputParameterValue(param.getParameterName(), param.getParameterValue());
                } else {
                    affinityParam = param;
                }
            }
        }
        final NodeAffinity nodeAffinity = affinityParam != null
                                          ? NodeAffinity.of(affinityParam.getParameterValue())
                                          : component.getNodeAffinity();
        if (nodeAffinity != null && !NodeAffinity.Any.equals(nodeAffinity) && !NodeAffinity.SameNode.equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity.getValue());
        }
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
        }
        return task;
    }

    /**
     * Creates a WPS (Web Processing Service) component execution task configured with the specified
     * component identifier and input parameters.
     *
     * @param wpsComponentId The unique identifier of the WPS component to be executed.
     * @param inputs A map of input parameter names and their corresponding values. The map may include
     *               parameters prefixed with "service~", which will be processed separately as extra parameters.
     * @return A configured {@link WPSExecutionTask} instance ready for execution.
     */
    protected WPSExecutionTask createWPSComponentExecutionTask(String wpsComponentId, Map<String, String> inputs) {
        final WPSComponent component = wpsComponentProvider.get(wpsComponentId);
        final WPSExecutionTask task = new WPSExecutionTask();
        task.setComponent(component);
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        final NodeAffinity nodeAffinity = component.getNodeAffinity();
        if (nodeAffinity != null && !NodeAffinity.Any.equals(nodeAffinity) && !NodeAffinity.SameNode.equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity.getValue());
        }
        if (inputs != null) {
            final Map<String, String> extraParams = new HashMap<>();
            final Iterator<Map.Entry<String, String>> iterator = inputs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (entry.getKey().startsWith("service~")) {
                    extraParams.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            }
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : extraParams.entrySet()) {
                task.getInputParameterValues().add(new Variable(entry.getKey(), entry.getValue()));
            }
        }
        return task;
    }

    /**
     * Creates a WMSComponentExecutionTask for the specified WMS component ID and input parameters.
     * The task is configured with the corresponding component, its target descriptors, node affinity,
     * and input parameters.
     *
     * @param wmsComponentId the unique identifier of the WMS component to be executed
     * @param inputs a map containing input parameter names and their respective values
     * @return a fully configured instance of WMSExecutionTask for the specified WMS component
     */
    protected WMSExecutionTask createWMSComponentExecutionTask(String wmsComponentId, Map<String, String> inputs) {
        final WMSComponent component = wmsComponentProvider.get(wmsComponentId);
        final WMSExecutionTask task = new WMSExecutionTask();
        task.setComponent(component);
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        final NodeAffinity nodeAffinity = component.getNodeAffinity();
        if (nodeAffinity != null && !NodeAffinity.Any.equals(nodeAffinity) && !NodeAffinity.SameNode.equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity.getValue());
        }
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
        }
        final List<ParameterDescriptor> parameters = component.getParameters();
        for (ParameterDescriptor parameter : parameters) {
            if (inputs == null || !inputs.containsKey(parameter.getId())) {
                task.setInputParameterValue(parameter.getId(), parameter.getDefaultValue());
            }
        }
        return task;
    }

    /**
     * Creates a {@link ConditionExecutionTask} based on the provided workflow node descriptor,
     * incoming links, and input parameters. The task is initialized with component information,
     * placeholders for inputs and outputs, node affinity information, and any custom parameters.
     *
     * @param workflowNode the descriptor of the workflow node associated with the task
     * @param incomingLinks a set of incoming links providing dependencies for the node; can be null
     * @param inputs a map of input parameter names and their corresponding values; can be null
     * @return a newly created and configured {@link ConditionExecutionTask} instance
     */
    protected ConditionExecutionTask createConditionExecutionTask(WorkflowNodeDescriptor workflowNode, Set<ComponentLink> incomingLinks,
                                                                  Map<String, String> inputs) {
        final List<ParameterValue> customValues = workflowNode.getCustomValues();
        final ConditionComponent component = conditionComponentProvider.get(workflowNode.getComponentId());
        final ConditionExecutionTask task = new ConditionExecutionTask();
        task.setComponent(component);
        // Placeholders for inputs of previous tasks
        if (incomingLinks != null) {
            incomingLinks.forEach(link -> {
                String name = link.getOutput().getName();
                task.setInputParameterValue(name, null);
            });
        }
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        ParameterValue affinityParam = null;
        if (customValues != null) {
            for (ParameterValue param : customValues) {
                if ("nodeAffinity".equals(param.getParameterName())) {
                    affinityParam = param;
                } else {
                    task.setInputParameterValue(param.getParameterName(), param.getParameterValue());
                }
            }
        }
        final NodeAffinity nodeAffinity = affinityParam != null
                                          ? NodeAffinity.of(affinityParam.getParameterValue())
                                          : component.getNodeAffinity();
        if (nodeAffinity != null && !NodeAffinity.Any.equals(nodeAffinity) && !NodeAffinity.SameNode.equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity.getValue());
        }
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
        }
        task.setCardinality(1);
        return task;
    }

    @Override
    public List<ExecutionJob> splitJob(ExecutionJob job) throws PersistenceException, CloneNotSupportedException {
        final List<ExecutionJob> jobs = new ArrayList<>();
        List<ExecutionTask> tasks = job.getTasks();
        if (tasks != null) {
            final List<DataSourceExecutionTask> dataTasks = tasks.stream().filter(t -> t instanceof DataSourceExecutionTask)
                                                           .map(t -> (DataSourceExecutionTask) t).collect(Collectors.toList());
            if (dataTasks.size() == 2) {
                DataSourceExecutionTask dataTask = dataTasks.stream().filter(TaskUtilities::isSynchronized).findFirst().orElse(null);
                if (dataTask != null) {
                    DataSourceExecutionTask primaryTask = dataTasks.stream().filter(d -> !d.getId().equals(dataTask.getId())).findFirst().get();
                    List<Tuple<String, String>> tuples = TaskUtilities.coupleTaskOutputs(primaryTask, dataTask);
                    final int size = tuples.size();
                    if (size > 1) {
                        for (int i = 1; i < size; i++) {
                            ExecutionJob clonedJob = cloneJob(job, i + 1, size);
                            final Map<String, List<String>> dependencies = clonedJob.getTaskDependencies();
                            DataSourceExecutionTask clonedPrimaryDataTask = clonedJob.getTasks().stream()
                                                                     .filter(t -> t instanceof DataSourceExecutionTask
                                                                             && !dependencies.containsKey(String.valueOf(t.getId())))
                                                                     .map(DataSourceExecutionTask.class::cast)
                                                                     .findFirst().orElse(null);
                            if (clonedPrimaryDataTask == null) {
                                throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                            }
                            DataSourceExecutionTask clonedSecondaryDataTask = clonedJob.getTasks().stream()
                                                                       .filter(t -> t instanceof DataSourceExecutionTask
                                                                               && dependencies.containsKey(String.valueOf(t.getId())))
                                                                       .map(DataSourceExecutionTask.class::cast)
                                                                       .findFirst().orElse(null);

                            if (clonedSecondaryDataTask == null) {
                                throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                            }
                            assignResult(clonedPrimaryDataTask, tuples.get(i).getKeyOne());
                            assignResult(clonedSecondaryDataTask, tuples.get(i).getKeyTwo());
                            clonedJob = jobProvider.update(clonedJob);
                            jobs.add(clonedJob);
                        }
                        // Reset the output values to prevent appending to any existing value
                        assignResult(primaryTask, tuples.get(0).getKeyOne());
                        assignResult(dataTask, tuples.get(0).getKeyTwo());
                        job = jobProvider.update(job);
                    }
                }
            } else {
                for (DataSourceExecutionTask dataTask : dataTasks) {
                    Variable values = dataTask.getOutputParameterValues()
                                              .stream()
                                              .filter(v -> v.getKey().equals(DataSourceComponent.RESULTS_PARAMETER))
                                              .findFirst().orElse(null);
                    if (values != null) {
                        List<String> list = deserializeResults(values.getValue());
                        final int size;
                        if (list != null && (size = list.size()) > 1) {
                            for (int i = 1; i < size; i++) {
                                ExecutionJob clonedJob = cloneJob(job, i + 1, size);
                                ExecutionTask clonedDataTask = findClone(clonedJob, dataTask);
                                if (clonedDataTask == null) {
                                    throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                                }
                                assignResult((DataSourceExecutionTask) clonedDataTask, list.get(i));
                                clonedJob = jobProvider.update(clonedJob);
                                jobs.add(clonedJob);
                            }
                            assignResult(dataTask, list.get(0));
                            job = jobProvider.update(job);
                        }
                    }
                }
            }
        }
        return jobs;
    }

    @Override
    public List<ExecutionJob> splitJobBeforeExecution(ExecutionJob job) throws PersistenceException, CloneNotSupportedException {
        final List<ExecutionJob> jobs = new ArrayList<>();
        List<ExecutionTask> tasks = job.getTasks();
        if (tasks != null) {
            final List<DataSourceExecutionTask> dataTasks = tasks.stream().filter(t -> t instanceof DataSourceExecutionTask)
                                                           .map(t -> (DataSourceExecutionTask) t).collect(Collectors.toList());
            if (dataTasks.size() == 2) {
                DataSourceExecutionTask dataTask = dataTasks.stream().filter(TaskUtilities::isSynchronized).findFirst().orElse(null);
                if (dataTask != null) {
                    DataSourceExecutionTask primaryTask = dataTasks.stream().filter(d -> !d.getId().equals(dataTask.getId())).findFirst().get();
                    List<Tuple<String, String>> tuples = TaskUtilities.coupleTaskInputs(primaryTask, dataTask);
                    final int size = tuples.size();
                    if (size > 1) {
                        for (int i = 1; i < size; i++) {
                            ExecutionJob clonedJob = cloneJob(job, i + 1, size);
                            final Map<String, List<String>> dependencies = clonedJob.getTaskDependencies();
                            DataSourceExecutionTask clonedPrimaryDataTask = (DataSourceExecutionTask) clonedJob.getTasks().stream()
                                                                     .filter(t -> t instanceof DataSourceExecutionTask
                                                                             && !dependencies.containsKey(String.valueOf(t.getId())))
                                                                     .findFirst().orElse(null);
                            if (clonedPrimaryDataTask == null) {
                                throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                            }
                            DataSourceExecutionTask clonedSecondaryDataTask = (DataSourceExecutionTask) clonedJob.getTasks().stream()
                                                                       .filter(t -> t instanceof DataSourceExecutionTask
                                                                               && dependencies.containsKey(String.valueOf(t.getId())))
                                                                       .findFirst().orElse(null);

                            if (clonedSecondaryDataTask == null) {
                                throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                            }

                            final DataSourceComponent clonedPrimaryComponent = clonedPrimaryDataTask.getComponent().clone();
                            clonedPrimaryComponent.setId(clonedPrimaryComponent.getId() + "_" + i);
                            clonedPrimaryComponent.setVolatile(true);
                            clonedPrimaryComponent.getSources().stream().forEach(s -> s.setParentId(clonedPrimaryComponent.getId()));
                            clonedPrimaryComponent.getTargets().stream().forEach(t ->
                            	{t.setParentId(clonedPrimaryComponent.getId());t.setCardinality(1);}
                            );
                            SourceDescriptor querryDescriptor = clonedPrimaryComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                            if (querryDescriptor == null) {
                            	throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned correctly. Missing querry parameter descriptor");
                            }
                            querryDescriptor.setCardinality(1);
                            querryDescriptor.getDataDescriptor().setLocation(tuples.get(i).getKeyOne());
                            dataSourceComponentProvider.save(clonedPrimaryComponent);
                            clonedPrimaryDataTask.setComponent(clonedPrimaryComponent);
                            clonedPrimaryDataTask.setCardinality(1);
                            taskProvider.update(clonedPrimaryDataTask);

                            
                            final DataSourceComponent clonedSecondaryComponent = clonedSecondaryDataTask.getComponent().clone();
                            clonedSecondaryComponent.setId(clonedSecondaryComponent.getId() + "_" + i);
                            clonedSecondaryComponent.setVolatile(true);
                            clonedSecondaryComponent.getSources().stream().forEach(s -> s.setParentId(clonedSecondaryComponent.getId()));
                            clonedSecondaryComponent.getTargets().stream().forEach(t ->
                            	{t.setParentId(clonedSecondaryComponent.getId());t.setCardinality(1);}
                            );
                            querryDescriptor = clonedSecondaryComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                            if (querryDescriptor == null) {
                            	throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned correctly. Missing querry parameter descriptor");
                            }
                            querryDescriptor.setCardinality(1);
                            querryDescriptor.getDataDescriptor().setLocation(tuples.get(i).getKeyTwo());
                            dataSourceComponentProvider.save(clonedSecondaryComponent);
                            clonedSecondaryDataTask.setComponent(clonedSecondaryComponent);
                            clonedSecondaryDataTask.setCardinality(1);
                            taskProvider.update(clonedSecondaryDataTask);
                            clonedJob = jobProvider.update(clonedJob);
                            jobs.add(clonedJob);
                        }
                        // Reset the output values to prevent appending to any existing value
                        final DataSourceComponent clonedPrimaryComponent = primaryTask.getComponent().clone();
                        clonedPrimaryComponent.setId(clonedPrimaryComponent.getId() + "_0");
                        clonedPrimaryComponent.setVolatile(true);
                        clonedPrimaryComponent.getSources().stream().forEach(s -> s.setParentId(clonedPrimaryComponent.getId()));
                        clonedPrimaryComponent.getTargets().stream().forEach(t ->
                        	{t.setParentId(clonedPrimaryComponent.getId());t.setCardinality(1);}
                        );
                        SourceDescriptor queryDescriptor = clonedPrimaryComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                        if (queryDescriptor == null) {
                        	throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned correctly. Missing querry parameter descriptor");
                        }
                        queryDescriptor.setCardinality(1);
                        queryDescriptor.getDataDescriptor().setLocation(tuples.get(0).getKeyOne());
                        dataSourceComponentProvider.save(clonedPrimaryComponent);
                        primaryTask.setComponent(clonedPrimaryComponent);
                        primaryTask.setCardinality(1);
                        taskProvider.update(primaryTask);

                        final DataSourceComponent clonedSecondaryComponent = dataTask.getComponent().clone();
                        clonedSecondaryComponent.setId(clonedSecondaryComponent.getId() + "_0");
                        clonedSecondaryComponent.setVolatile(true);
                        clonedSecondaryComponent.getSources().stream().forEach(s -> s.setParentId(clonedSecondaryComponent.getId()));
                        clonedSecondaryComponent.getTargets().stream().forEach(t ->
                        	{t.setParentId(clonedSecondaryComponent.getId());t.setCardinality(1);}
                        );
                        queryDescriptor = clonedSecondaryComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                        if (queryDescriptor == null) {
                        	throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned correctly. Missing querry parameter descriptor");
                        }
                        queryDescriptor.setCardinality(1);
                        queryDescriptor.getDataDescriptor().setLocation(tuples.get(0).getKeyTwo());
                        dataSourceComponentProvider.save(clonedSecondaryComponent);
                        dataTask.setComponent(clonedSecondaryComponent);
                        dataTask.setCardinality(1);
                        taskProvider.update(dataTask);
                        job = jobProvider.update(job);
                    }
                }
            } else {
                for (DataSourceExecutionTask dataTask : dataTasks) {
                	SourceDescriptor sourceDescriptor;
					sourceDescriptor = dataTask.getComponent().getSources().stream().filter(s -> s.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                    if (sourceDescriptor != null && sourceDescriptor.getDataDescriptor() != null
                            && sourceDescriptor.getDataDescriptor().getLocation() != null) {
                        List<String> list = Arrays.asList(sourceDescriptor.getDataDescriptor().getLocation().split(","));
                        if (list.size() > 1) {
                            final int size = list.size();
                            for (int i = 1; i < size; i++) {
                                ExecutionJob clonedJob = cloneJob(job, i + 1, size);
                                DataSourceExecutionTask clonedDataTask = findClone(clonedJob, dataTask);
                                if (clonedDataTask == null) {
                                    throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                                }
                                assignSourceLocation(clonedDataTask, list.get(i), "_" + i);
                                clonedJob = jobProvider.update(clonedJob);
                                jobs.add(clonedJob);
                            }
                            assignSourceLocation(dataTask, list.get(0), "_0");
                            job = jobProvider.update(job);
                        }
                    }
                }
            }
        }
        return jobs;
    }

    @Override
    public void removeVolatileComponents(final ExecutionJob job) throws PersistenceException {
        for (final ExecutionTask task : job.getTasks()) {
        	if (task instanceof DataSourceExecutionTask) {
                try {
                    // get the component
                    DataSourceComponent dsc = ((DataSourceExecutionTask) task).getComponent();
                    // The object might be a proxy, so check the actual entity
                    dsc = dataSourceComponentProvider.get(dsc.getId());
                    if (dsc != null && dsc.getVolatile()) {
                        dataSourceComponentProvider.delete(dsc);
                    }
                } catch (Throwable e) {
                    logger.warning(e.getMessage());
                }
        	}
        }
    }
    
    @Override
    public ExecutionJob cloneJob(ExecutionJob original, int cloneNumber, int total) throws CloneNotSupportedException, PersistenceException {
        return TransactionalMethod.withExceptionType(PersistenceException.class).execute(() -> {
            ExecutionJob clone = new ExecutionJob();
            clone.setName(original.getName() + " (" + cloneNumber + "/" + total + ")");
            clone.setAppId(original.getAppId());
            clone.setWorkflowId(original.getWorkflowId());
            clone.setBatchId(original.getBatchId());
            clone.setExternal(original.isExternal());
            clone.setQueryId(original.getQueryId());
            clone.setUserId(original.getUserId());
            clone.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            final boolean useParentFolder = ConfigurationManager.getInstance().getBooleanValue("execution.use.parent.job.folder");
            clone.setJobOutputPath(original.getJobOutputPath() + (useParentFolder ? "" : "_" + cloneNumber));
            clone.setJobType(original.getJobType());
            clone.setEnvironment(original.getEnvironment());
            clone = jobProvider.save(clone);
            final List<ExecutionTask> originalTasks = original.getTasks();
            final Map<String, String> cloneTaskMap = new HashMap<>();
            for (ExecutionTask originalTask : originalTasks) {
                ExecutionTask task = cloneTask(clone, originalTask);
                cloneTaskMap.put(String.valueOf(originalTask.getId()), String.valueOf(task.getId()));
            }
            Map<String, List<String>> originalDependencies = original.getTaskDependencies();
            Map<String, List<String>> clonedDependencies = new HashMap<>();
            if (originalDependencies != null) {
                for (Map.Entry<String, List<String>> entry : originalDependencies.entrySet()) {
                    String key = entry.getKey();
                    clonedDependencies.put(cloneTaskMap.get(key),
                                           entry.getValue().stream().map(cloneTaskMap::get).collect(Collectors.toList()));
                }
                clone.setTaskDependencies(clonedDependencies);
            }
            return jobProvider.update(clone);
        });
    }

    @Override
    public ExecutionTask cloneTask(ExecutionJob clonedJob, ExecutionTask original) throws CloneNotSupportedException, PersistenceException {
        ExecutionTask clone = null;
        WorkflowNodeDescriptor node = workflowNodeProvider.get(original.getWorkflowNodeId());
        final Map<String, String> params = new HashMap<>();
        List<Variable> values = original.getInputParameterValues();
        if (values != null) {
            values.forEach(v -> params.put(v.getKey(), v.getValue()));
        }
        if (original instanceof ProcessingExecutionTask && !(original instanceof ScriptTask)) {
            clone = createProcessingExecutionTask(clonedJob, node, node.getIncomingLinks(), params);
        } else if (original instanceof WPSExecutionTask) {
            clone = createWPSComponentExecutionTask(node, node.getIncomingLinks(), params);
        } else if (original instanceof DataSourceExecutionTask) {
            clone = createDataSourceExecutionTask(clonedJob, node, params);
            //clone.setInputParameterValues(null);
            clone.setInputParameterValues(copyVariables(original.getInputParameterValues()));
        } else if (original instanceof ExecutionGroup) {
            clone = createTaskGroup(clonedJob, node.getWorkflow(), (WorkflowNodeGroupDescriptor) node, params);
        } else if (original instanceof ConditionExecutionTask){
            clone = createConditionExecutionTask(node, node.getIncomingLinks(), params);
        }
        if (clone == null) {
            throw new CloneNotSupportedException(String.valueOf(original.getId()));
        }
        clone.setLevel(original.getLevel());
        clone.setWorkflowNodeId(node.getId());
        return taskProvider.save(clone, clonedJob);
    }

    private List<String> deserializeResults(String list) {
        List<String> names = null;
        try {
            StringListAdapter adapter = new StringListAdapter();
            names = adapter.marshal(list);
        } catch (Exception e) {
            logger.severe("Deserialization of list failed: " + e.getMessage());
        }
        return names;
    }

    private List<Variable> copyVariables(List<Variable> variables) {
        return variables != null
                ? variables.stream().map(v -> new Variable(v.getKey(), v.getValue()))
                           .collect(Collectors.toList())
                : null;
    }

    private DataSourceExecutionTask findClone(ExecutionJob clonedJob, DataSourceExecutionTask original) {
        return clonedJob.getTasks().stream()
                        .filter(t -> t instanceof DataSourceExecutionTask)
                        .map(DataSourceExecutionTask.class::cast)
                        .filter(t -> original.getWorkflowNodeId().equals(t.getWorkflowNodeId()))
                        .findFirst().orElse(null);
    }

    private void assignSourceLocation(DataSourceExecutionTask task, String location, String suffix) throws CloneNotSupportedException, PersistenceException {
        final DataSourceComponent clonedComponent = task.getComponent().clone();
        clonedComponent.setId(clonedComponent.getId() + suffix);
        clonedComponent.setVolatile(true);
        clonedComponent.getSources().forEach(s -> s.setParentId(clonedComponent.getId()));
        clonedComponent.getTargets().forEach(t -> {
            t.setParentId(clonedComponent.getId());
            t.setCardinality(1);
        });
        SourceDescriptor queryDescriptor = clonedComponent.getSources().stream()
                                                          .filter(s -> DataSourceComponent.QUERY_PARAMETER.equals(s.getName()))
                                                          .findFirst()
                                                          .orElseThrow(() -> new CloneNotSupportedException("DataSourceExecutionTask was not cloned properly. Missing query parameter"));
        queryDescriptor.setCardinality(1);
        queryDescriptor.getDataDescriptor().setLocation(location);
        dataSourceComponentProvider.save(clonedComponent);
        task.setComponent(clonedComponent);
        task.setCardinality(1);
        taskProvider.update(task);
    }

    private void assignResult(DataSourceExecutionTask task, String result) throws PersistenceException {
        task.setOutputParameterValues(null);
        task.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, result);
        task.setCardinality(1);
        task.setResourceId(UUID.randomUUID().toString());
        task.setExecutionStatus(ExecutionStatus.DONE);
        taskProvider.update(task);
    }
}
