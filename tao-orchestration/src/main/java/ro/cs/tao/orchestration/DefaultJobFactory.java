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
public class DefaultJobFactory {

    protected static final int DEFAULT_INSTANCE = 1;
    protected static final String invalidChars = "[\\~\\#\\%\\&\\*\\{\\}\\\\\\:\\<\\>\\?\\/\\+\\|, ]";
    protected final ExecutionJobProvider jobProvider;
    protected final ExecutionTaskProvider taskProvider;
    protected final QueryProvider queryProvider;
    protected final ProcessingComponentProvider processingComponentProvider;
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
        this.logger = Logger.getLogger(DefaultJobFactory.class.getName());
    }

    /**
     * Creates an execution job for the given workflow, with the given input parameter values
     *
     * @param context   The current user session context
     * @param appId     The orchestrator identifier
     * @param jobName   The name of the new job
     * @param workflow  The workflow
     * @param inputs    The (possibly overridden) input parameter values
     * @param jobType   The output job format
     */
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
            final boolean preComputeOutputs = job.getJobType() != JobType.EXECUTION;
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
                    task.setContext(context);
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
                                                                                    scriptTask.computeTargetOutput(t.getDataDescriptor().getLocation())));
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

    /**
     * Creates an execution job with a single task for a WPS remote service invocation, with the given input parameter values
     *
     * @param context   The invocation context
     * @param appId     The identifier of the Orchestrator instance
     * @param jobName   The name of the new job
     * @param inputs    The (possibly overridden) input parameter values
     */
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
            task.setContext(context);
            if (task.getId() == null) {
                taskProvider.save(task, job);
            } else {
                taskProvider.update(task);
            }
            logger.finest(String.format("Created WPS task %s.%s", jobName, task.getId()));
        }
        return job;
    }

    /**
     * Creates an execution job with a single task for a WMS remote service invocation, with the given input parameter values
     *
     * @param context   The invocation context
     * @param appId     The identifier of the Orchestrator instance
     * @param jobName   The name of the new job
     * @param inputs    The (possibly overridden) input parameter values
     */
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
        task.setContext(context);
        if (task.getId() == null) {
            taskProvider.save(task, job);
        } else {
            taskProvider.update(task);
        }
        logger.finest(String.format("Created WMS task %s.%s", jobName, task.getId()));
        return job;
    }

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
                    task = createProcessingExecutionTask(workflowNode, links, inputs);
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

    protected ExecutionTask createProcessingExecutionTask(WorkflowNodeDescriptor workflowNode, Set<ComponentLink> incomingLinks,
                                                        Map<String, String> inputs) {
        final List<ParameterValue> customValues = workflowNode.getCustomValues();
        final ProcessingComponent component = processingComponentProvider.get(workflowNode.getComponentId());
        final ProcessingExecutionTask task = new ProcessingExecutionTask();
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
        final String nodeAffinity = affinityParam != null
                ? affinityParam.getParameterValue()
                : component.getNodeAffinity();
        if (nodeAffinity != null && !"Any".equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity);
        }
        if (component.getComponentType() == ProcessingComponentType.EXTERNAL && customValues != null) {
            task.setCommand(component.buildExecutionCommand(customValues.stream().collect(Collectors.toMap(ParameterValue::getParameterName, ParameterValue::getParameterValue)),
                                                            null));
        }
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
        }
        task.setCardinality(TaskUtilities.getTargetCardinality(component));
        task.setUsedRAM(TaskUtilities.getComponentMemoryRequirements(component));
        return task;
    }

    protected ExecutionTask createDataSourceExecutionTask(ExecutionJob job, WorkflowNodeDescriptor workflowNode,
                                                        Map<String, String> inputs)
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
        int val;
        String value = taskInputs.get("pageNumber");
        if (value != null) {
            val = Integer.parseInt(value);
            taskInputs.remove("pageNumber");
        } else {
            val = 1;
        }
        query.setPageNumber(val);
        value = taskInputs.get("pageSize");
        if (value != null) {
            val = Integer.parseInt(value);
            taskInputs.remove("pageSize");
        } else {
            val = 1;
        }
        query.setPageSize(val);
        value = taskInputs.get("limit");
        if (value != null) {
            val = Integer.parseInt(value);
            taskInputs.remove("limit");
        } else {
            val = 1;
        }
        query.setLimit(val);
        query.setValues(taskInputs);
        query.setUserId(job.getUserId());
        if (query.getId() == null || query.getId() == 0) {
            queryProvider.save(query);
        } else {
            queryProvider.update(query);
        }
        task.setComponent(dsComponent);
        task.setCardinality(TaskUtilities.getTargetCardinality(dsComponent));
        task.setInputParameterValue("query", query.toString());
        return task;
    }

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
        int val;
        String value = taskInputs.get("pageNumber");
        if (value != null) {
            val = Integer.parseInt(value);
            taskInputs.remove("pageNumber");
        } else {
            val = 1;
        }
        localQuery.setPageNumber(val);
        value = taskInputs.get("pageSize");
        if (value != null) {
            val = Integer.parseInt(value);
            taskInputs.remove("pageSize");
        } else {
            val = 1;
        }
        localQuery.setPageSize(val);
        value = taskInputs.get("limit");
        if (value != null) {
            val = Integer.parseInt(value);
            taskInputs.remove("limit");
        } else {
            val = 1;
        }
        localQuery.setLimit(val);
        localQuery.setValues(taskInputs);
        localQuery.setUserId(job.getUserId());
        queryProvider.save(localQuery);
        task.setComponent(component);
        task.setCardinality(TaskUtilities.getTargetCardinality(component));
        task.setInputParameterValue("local.query", localQuery.toString());
        task.setInputParameterValue("remote.query", query.toString());
        return task;
    }

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
        final String nodeAffinity = affinityParam != null
                ? affinityParam.getParameterValue()
                : component.getNodeAffinity();
        if (nodeAffinity != null && !"Any".equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity);
        }
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                task.setInputParameterValue(entry.getKey(), entry.getValue());
            }
        }
        return task;
    }

    protected WPSExecutionTask createWPSComponentExecutionTask(String wpsComponentId, Map<String, String> inputs) {
        final WPSComponent component = wpsComponentProvider.get(wpsComponentId);
        final WPSExecutionTask task = new WPSExecutionTask();
        task.setComponent(component);
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        final String nodeAffinity = component.getNodeAffinity();
        if (nodeAffinity != null && !"Any".equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity);
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

    protected WMSExecutionTask createWMSComponentExecutionTask(String wmsComponentId, Map<String, String> inputs) {
        final WMSComponent component = wmsComponentProvider.get(wmsComponentId);
        final WMSExecutionTask task = new WMSExecutionTask();
        task.setComponent(component);
        // Placeholders for outputs of this task
        final List<TargetDescriptor> targets = component.getTargets();
        if (targets != null) {
            targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
        }
        final String nodeAffinity = component.getNodeAffinity();
        if (nodeAffinity != null && !"Any".equals(nodeAffinity)) {
            task.setExecutionNodeHostName(nodeAffinity);
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
     * Creates a list of execution jobs from a job based on the list of values of the entry data source task.
     * The first value is discarded, assuming it is taken by the original job.
     *
     * @param job       The job to be split
     *
     */
    public List<ExecutionJob> splitJob(ExecutionJob job) throws PersistenceException, CloneNotSupportedException {
        final List<ExecutionJob> jobs = new ArrayList<>();
        List<ExecutionTask> tasks = job.getTasks();
        //jobs.add(job);
        if (tasks != null) {
            //DataSourceExecutionTask dataTask = (DataSourceExecutionTask) tasks.stream().filter(t -> t instanceof DataSourceExecutionTask).findFirst().orElse(null);
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
                            ExecutionTask clonedPrimaryDataTask = clonedJob.getTasks().stream()
                                                                     .filter(t -> t instanceof DataSourceExecutionTask
                                                                             && !dependencies.containsKey(String.valueOf(t.getId())))
                                                                     .findFirst().orElse(null);
                            if (clonedPrimaryDataTask == null) {
                                throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                            }
                            ExecutionTask clonedSecondaryDataTask = clonedJob.getTasks().stream()
                                                                       .filter(t -> t instanceof DataSourceExecutionTask
                                                                               && dependencies.containsKey(String.valueOf(t.getId())))
                                                                       .findFirst().orElse(null);

                            if (clonedSecondaryDataTask == null) {
                                throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                            }
                            // Reset the output values to prevent appending to any existing value
                            clonedPrimaryDataTask.setOutputParameterValues(null);
                            clonedPrimaryDataTask.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, tuples.get(i).getKeyOne());
                            clonedPrimaryDataTask.setContext(primaryTask.getContext());
                            clonedPrimaryDataTask.setCardinality(1);
                            taskProvider.update(clonedPrimaryDataTask);
                            clonedSecondaryDataTask.setOutputParameterValues(null);
                            clonedSecondaryDataTask.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, tuples.get(i).getKeyTwo());
                            clonedSecondaryDataTask.setContext(dataTask.getContext());
                            clonedSecondaryDataTask.setCardinality(1);
                            taskProvider.update(clonedSecondaryDataTask);
                            clonedJob = jobProvider.update(clonedJob);
                            jobs.add(clonedJob);
                        }
                        // Reset the output values to prevent appending to any existing value
                        primaryTask.setOutputParameterValues(null);
                        primaryTask.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, tuples.get(0).getKeyOne());
                        primaryTask.setCardinality(1);
                        taskProvider.update(primaryTask);
                        dataTask.setOutputParameterValues(null);
                        dataTask.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, tuples.get(0).getKeyTwo());
                        dataTask.setCardinality(1);
                        taskProvider.update(dataTask);
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
                        if (list != null && list.size() > 1) {
                            final int size = list.size();
                            for (int i = 1; i < size; i++) {
                                ExecutionJob clonedJob = cloneJob(job, i + 1, size);
                                ExecutionTask clonedDataTask = clonedJob.getTasks().stream().filter(t -> t instanceof DataSourceExecutionTask).findFirst().orElse(null);
                                if (clonedDataTask == null) {
                                    throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                                }
                                // Reset the output values to prevent appending to any existing value
                                clonedDataTask.setOutputParameterValues(null);
                                clonedDataTask.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, list.get(i));
                                clonedDataTask.setCardinality(1);
                                clonedDataTask.setContext(dataTask.getContext());
                                clonedDataTask.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                                taskProvider.update(clonedDataTask);
                                clonedJob = jobProvider.update(clonedJob);
                                jobs.add(clonedJob);
                            }
                            // Reset the output values to prevent appending to any existing value
                            dataTask.setOutputParameterValues(null);
                            dataTask.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, list.get(0));
                            dataTask.setCardinality(1);
                            taskProvider.update(dataTask);
                            job = jobProvider.update(job);
                        }
                    }
                }
            }
        }
        return jobs;
    }


    /**
     * Creates a list of execution jobs from a job based on the list of values of the entry data source task.
     * The first value is discarded, assuming it is taken by the original job.
     *
     * @param job       The job to be split
     *
     */
    public List<ExecutionJob> splitJobBeforeExecution(ExecutionJob job) throws PersistenceException, CloneNotSupportedException {
        final List<ExecutionJob> jobs = new ArrayList<>();
        List<ExecutionTask> tasks = job.getTasks();
        //jobs.add(job);
        if (tasks != null) {
            //DataSourceExecutionTask dataTask = (DataSourceExecutionTask) tasks.stream().filter(t -> t instanceof DataSourceExecutionTask).findFirst().orElse(null);
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
                            clonedPrimaryDataTask.setContext(primaryTask.getContext());
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
                            clonedSecondaryDataTask.setContext(dataTask.getContext());
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
                        SourceDescriptor querryDescriptor = clonedPrimaryComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                        if (querryDescriptor == null) {
                        	throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned correctly. Missing querry parameter descriptor");
                        }
                        querryDescriptor.setCardinality(1);
                        querryDescriptor.getDataDescriptor().setLocation(tuples.get(0).getKeyOne());
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
                        querryDescriptor = clonedSecondaryComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                        if (querryDescriptor == null) {
                        	throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned correctly. Missing querry parameter descriptor");
                        }
                        querryDescriptor.setCardinality(1);
                        querryDescriptor.getDataDescriptor().setLocation(tuples.get(0).getKeyTwo());
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
                	
                	//Variable values = dataTask.getInputParameterValues().stream().filter(v -> v.getKey().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                	
//                    Variable values = dataTask.getOutputParameterValues()
//                                              .stream()
//                                              .filter(v -> v.getKey().equals(DataSourceComponent.RESULTS_PARAMETER))
//                                              .findFirst().orElse(null);
                    if (sourceDescriptor != null && sourceDescriptor.getDataDescriptor() != null) {
                        List<String> list = Arrays.asList(sourceDescriptor.getDataDescriptor().getLocation().split(","));
                        if (list != null && list.size() > 1) {
                            final int size = list.size();
                            for (int i = 1; i < size; i++) {
                                ExecutionJob clonedJob = cloneJob(job, i + 1, size);
                                DataSourceExecutionTask clonedDataTask = (DataSourceExecutionTask) clonedJob.getTasks().stream().filter(t -> t instanceof DataSourceExecutionTask).findFirst().orElse(null);
                                if (clonedDataTask == null) {
                                    throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned");
                                }
                                final DataSourceComponent clonedComponent = clonedDataTask.getComponent().clone();
                                clonedComponent.setId(clonedComponent.getId() + "_" + i);
                                clonedComponent.setVolatile(true);
                                clonedComponent.getSources().stream().forEach(s -> s.setParentId(clonedComponent.getId()));
                                clonedComponent.getTargets().stream().forEach(t ->
                                	{t.setParentId(clonedComponent.getId());t.setCardinality(1);}
                                );
                                // Reset the output values to prevent appending to any existing value
                                final SourceDescriptor querryDescriptor = clonedComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                                if (querryDescriptor == null) {
                                	throw new CloneNotSupportedException("DataSourceExecutionTask was not cloned correctly. Missing querry parameter descriptor");
                                }
                                querryDescriptor.setCardinality(1);
                                querryDescriptor.getDataDescriptor().setLocation(list.get(i));
                                dataSourceComponentProvider.save(clonedComponent);
                                clonedDataTask.setComponent(clonedComponent);
                                clonedDataTask.setCardinality(1);
                                clonedDataTask.setContext(dataTask.getContext());
                                clonedDataTask.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                                taskProvider.update(clonedDataTask);
                                clonedJob = jobProvider.update(clonedJob);
                                jobs.add(clonedJob);
                            }
                            // Reset the output values to prevent appending to any existing value
//                            dataTask.setOutputParameterValues(null);
//                            dataTask.setOutputParameterValue(DataSourceComponent.RESULTS_PARAMETER, list.get(0));
                            final DataSourceComponent clonedComponent = dataTask.getComponent().clone();
                            clonedComponent.setId(clonedComponent.getId() + "_0");
                            clonedComponent.setVolatile(true);
                            clonedComponent.getSources().stream().forEach(s -> s.setParentId(clonedComponent.getId()));
                            clonedComponent.getTargets().stream().forEach(t ->
                        		{t.setParentId(clonedComponent.getId());t.setCardinality(1);}
                            );

                            final SourceDescriptor querryDescriptor = clonedComponent.getSources().stream().filter(sd -> sd.getName().equals(DataSourceComponent.QUERY_PARAMETER)).findFirst().orElse(null);
                            if (querryDescriptor == null) {
                            	throw new CloneNotSupportedException("Invalid DataSourceExecutionTask. Missing querry parameter descriptor");
                            }
                            querryDescriptor.setCardinality(1);
                            querryDescriptor.getDataDescriptor().setLocation(list.get(0));
                            dataSourceComponentProvider.save(clonedComponent);
                            dataTask.setComponent(clonedComponent);
                            dataTask.setCardinality(1);
                            taskProvider.update(dataTask);
                            job = jobProvider.update(job);
                        }
                    }
                }
            }
        }
        return jobs;
    }

    /**
     * Remove volatile components.
     * 
     * @param job the finished job
     * @throws PersistenceException if the removal fails
     */
    public void removeVolatileComponents(final ExecutionJob job) throws PersistenceException {
        for (final ExecutionTask task : job.getTasks()) {
        	if (task instanceof DataSourceExecutionTask) {
        		// get the component
        		final DataSourceComponent dsc = ((DataSourceExecutionTask) task).getComponent();
        		if (dsc != null && dsc.getVolatile()) {
        			dataSourceComponentProvider.delete(dsc);
        		}
        	}
        }
    }
    
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
            clone.setJobOutputPath(original.getJobOutputPath() + "_" + cloneNumber);
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

    public ExecutionTask cloneTask(ExecutionJob clonedJob, ExecutionTask original) throws CloneNotSupportedException, PersistenceException {
        ExecutionTask clone = null;
        WorkflowNodeDescriptor node = workflowNodeProvider.get(original.getWorkflowNodeId());
        final Map<String, String> params = new HashMap<>();
        List<Variable> values = original.getInputParameterValues();
        if (values != null) {
            values.forEach(v -> params.put(v.getKey(), v.getValue()));
        }
        if (original instanceof ProcessingExecutionTask && !(original instanceof ScriptTask)) {
            clone = createProcessingExecutionTask(node, node.getIncomingLinks(), params);
        } else if (original instanceof WPSExecutionTask) {
            clone = createWPSComponentExecutionTask(node, node.getIncomingLinks(), params);
        } else if (original instanceof DataSourceExecutionTask) {
            //throw new CloneNotSupportedException(DataSourceExecutionTask.class.getSimpleName());
            clone = createDataSourceExecutionTask(clonedJob, node, params);
            clone.setInputParameterValues(null);
        } else if (original instanceof ExecutionGroup) {
            clone = createTaskGroup(clonedJob, node.getWorkflow(), (WorkflowNodeGroupDescriptor) node, params);
        }
        if (clone == null) {
            throw new CloneNotSupportedException(String.valueOf(original.getId()));
        }
        clone.setLevel(original.getLevel());
        clone.setWorkflowNodeId(node.getId());
        clone.setContext(original.getContext());
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
}
