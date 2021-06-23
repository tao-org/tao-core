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
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.datasource.param.CommonParameterNames;
import ro.cs.tao.datasource.persistence.DataSourceComponentGroupProvider;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.datasource.persistence.QueryProvider;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.GroupComponentProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Factory class for creating execution jobs.
 *
 * @author Cosmin Cara
 */
public class DefaultJobFactory {

    protected static final int DEFAULT_INSTANCE = 1;
    protected final ExecutionJobProvider jobProvider;
    protected final ExecutionTaskProvider taskProvider;
    protected final QueryProvider queryProvider;
    protected final ProcessingComponentProvider processingComponentProvider;
    protected final DataSourceComponentProvider dataSourceComponentProvider;
    protected final DataSourceComponentGroupProvider dataSourceComponentGroupProvider;
    protected final GroupComponentProvider groupComponentProvider;
    protected final Logger logger;

    DefaultJobFactory() {
        this.jobProvider = SpringContextBridge.services().getService(ExecutionJobProvider.class);
        this.taskProvider = SpringContextBridge.services().getService(ExecutionTaskProvider.class);
        this.queryProvider = SpringContextBridge.services().getService(QueryProvider.class);
        this.processingComponentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        this.dataSourceComponentProvider = SpringContextBridge.services().getService(DataSourceComponentProvider.class);
        this.dataSourceComponentGroupProvider = SpringContextBridge.services().getService(DataSourceComponentGroupProvider.class);
        this.groupComponentProvider = SpringContextBridge.services().getService(GroupComponentProvider.class);
        this.logger = Logger.getLogger(DefaultJobFactory.class.getName());
    }

    /**
     * Creates an execution job for the given workflow, with the given input parameter values
     *
     * @param jobName   The name of the new job
     * @param workflow  The workflow
     * @param inputs    The (possibly overridden) input parameter values
     */
    public ExecutionJob createJob(String jobName, WorkflowDescriptor workflow, Map<String, Map<String, String>> inputs)
            throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setName(jobName);
            job.setUserName(SessionStore.currentContext().getPrincipal().getName());
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            job = jobProvider.save(job);
            List<WorkflowNodeDescriptor> nodes = workflow.getOrderedNodes();
            Set<WorkflowNodeGroupDescriptor> groups = nodes.stream()
                                                            .filter(n -> n instanceof WorkflowNodeGroupDescriptor)
                                                            .map(n -> (WorkflowNodeGroupDescriptor)n)
                                                            .collect(Collectors.toSet());
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
                        taskProvider.save(task, job);
                    } else {
                        taskProvider.update(task);
                    }
                    logger.finest(String.format("Created task %s.%s for node %s [id=%s, level %s]",
                            jobName, task.getId(), node.getName(), node.getId(), task.getLevel()));
                }
            }
        }
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
        if (tasks != null && tasks.size() > 0) {
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
        if (customValues != null) {
            for (ParameterValue param : customValues) {
                task.setInputParameterValue(param.getParameterName(), param.getParameterValue());
            }
        }
        final String nodeAffinity = component.getNodeAffinity();
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
        Query query = queryProvider.get(SessionStore.currentContext().getPrincipal().getName(),
                                        dsComponent.getSensorName(),
                                        dsComponent.getDataSourceName(),
                                        workflowNode.getId());
        if (query == null) {
            query = new Query();
            query.setUser(SessionStore.currentContext().getPrincipal().getName());
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
        query.setUserId(job.getUserName());
        if (query.getId() == null || query.getId() == 0) {
            queryProvider.save(query);
        } else {
            queryProvider.update(query);
        }
        task.setComponent(dsComponent);
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
        Query localQuery = queryProvider.get(SessionStore.currentContext().getPrincipal().getName(),
                                             component.getSensorName(),
                                             component.getDataSourceName(),
                                             workflowNode.getId());
        if (localQuery == null) {
            localQuery = new Query();
            localQuery.setUser(SessionStore.currentContext().getPrincipal().getName());
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
        localQuery.setUserId(job.getUserName());
        queryProvider.save(localQuery);
        task.setComponent(component);
        task.setInputParameterValue("local.query", localQuery.toString());
        task.setInputParameterValue("remote.query", query.toString());
        return task;
    }
}
