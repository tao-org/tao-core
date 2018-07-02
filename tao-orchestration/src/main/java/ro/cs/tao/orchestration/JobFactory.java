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

import ro.cs.tao.component.*;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SessionStore;
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

public class JobFactory {

    private final PersistenceManager persistenceManager;
    private final Logger logger;

    JobFactory(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.logger = Logger.getLogger(JobFactory.class.getName());
    }

    public ExecutionJob createJob(WorkflowDescriptor workflow, Map<String, Map<String, String>> inputs) throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setUserName(SessionStore.currentContext().getPrincipal().getName());
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            job = persistenceManager.saveExecutionJob(job);
            List<WorkflowNodeDescriptor> nodes = workflow.getOrderedNodes();
            Set<WorkflowNodeGroupDescriptor> groups = nodes.stream()
                                                            .filter(n -> n instanceof WorkflowNodeGroupDescriptor)
                                                            .map(n -> (WorkflowNodeGroupDescriptor)n)
                                                            .collect(Collectors.toSet());
            for (int i = 0; i < nodes.size(); i++) {
                WorkflowNodeDescriptor node = nodes.get(i);
                // A workflow contains also the nodes of a node group.
                // Hence, in order not to duplicate the tasks, the nodes from group are temporary removed from workflow.
                if (groups == null ||
                        groups.stream().noneMatch(g -> g.getNodes().stream()
                                                                    .anyMatch(c -> c.getId().equals(node.getId())))) {
                    Map<String, String> taskInputs = inputs != null ? inputs.get(node.getName()) : null;
                    ExecutionTask task = createTask(job, workflow, node, taskInputs);
                    if (i == 0 && taskInputs != null && !(task instanceof DataSourceExecutionTask)) {
                        for (Map.Entry<String, String> entry : taskInputs.entrySet()) {
                            task.setInputParameterValue(entry.getKey(), entry.getValue());
                        }
                    }
                    task.setLevel(node.getLevel());
                    if (task.getId() == null) {
                        persistenceManager.saveExecutionTask(task, job);
                    } else {
                        persistenceManager.updateExecutionTask(task);
                    }
                    logger.fine(String.format("Created task for node %s [level %s]", node.getId(), task.getLevel()));
                }
            }
        }
        return job;
    }

    public ExecutionTask createTaskGroup(ExecutionJob job,
                                         WorkflowDescriptor workflow,
                                         WorkflowNodeGroupDescriptor groupNode,
                                         Map<String, String> inputs) throws PersistenceException {
        ExecutionGroup group = new ExecutionGroup();
        group.setWorkflowNodeId(groupNode.getId());
        group.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        group.setResourceId("group-" + groupNode.getId() + "-" + job.getId());
        persistenceManager.saveExecutionTask(group, job);
        List<WorkflowNodeDescriptor> nodes = groupNode.getOrderedNodes();
        for (WorkflowNodeDescriptor node : nodes) {
            ExecutionTask task = createTask(job, workflow, node, inputs);
            task.setLevel(groupNode.getLevel() + 1);
            persistenceManager.saveExecutionTask(task, job);
            group.addTask(task);
        }
        List<ExecutionTask> tasks = group.getTasks();
        if (tasks != null && tasks.size() > 0) {
            List<ComponentLink> links = groupNode.getIncomingLinks();
            links.forEach(link -> {
                String name = link.getOutput().getName();
                group.setInputParameterValue(name, null);
            });
            GroupComponent component = persistenceManager.getGroupComponentById(groupNode.getComponentId());
            if (component != null) {
                // Placeholders for outputs of this task
                List<TargetDescriptor> targets = component.getTargets();
                targets.forEach(t -> group.setOutputParameterValue(t.getName(), null));
            }
        }
        return group;
    }

    public ExecutionTask createTask(ExecutionJob job,
                                    WorkflowDescriptor workflow,
                                    WorkflowNodeDescriptor workflowNode,
                                    Map<String, String> inputs) throws PersistenceException {
        ExecutionTask task;
        if (workflowNode instanceof WorkflowNodeGroupDescriptor) {
            task = createTaskGroup(job, workflow, (WorkflowNodeGroupDescriptor) workflowNode, inputs);
        } else {
            TaoComponent component = null;
            List<ParameterValue> customValues = workflowNode.getCustomValues();
            List<ComponentLink> links = workflowNode.getIncomingLinks();
            //try {
                component = persistenceManager.getProcessingComponentById(workflowNode.getComponentId());
            //} catch (PersistenceException ignored) { }
            if (component != null) {
                task = new ProcessingExecutionTask();
                ((ProcessingExecutionTask) task).setComponent((ProcessingComponent) component);
                // Placeholders for inputs of previous tasks
                links.forEach(link -> {
                    String name = link.getOutput().getName();
                    task.setInputParameterValue(name, null);
                });
                // Placeholders for outputs of this task
                List<TargetDescriptor> targets = component.getTargets();
                targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
                if (customValues != null) {
                    for (ParameterValue param : customValues) {
                        task.setInputParameterValue(param.getParameterName(), param.getParameterValue());
                    }
                }
            } else {
                task = new DataSourceExecutionTask();
                final Map<String, String> taskInputs;
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
                component = persistenceManager.getDataSourceInstance(workflowNode.getComponentId());
                Query query = persistenceManager.getQueries(SessionStore.currentContext().getPrincipal().getName(),
                                                            ((DataSourceComponent) component).getSensorName(),
                                                            ((DataSourceComponent) component).getDataSourceName(),
                                                            workflowNode.getId());
                if (query == null) {
                    query = new Query();
                    query.setUser(SessionStore.currentContext().getPrincipal().getName());
                    query.setSensor(((DataSourceComponent) component).getSensorName());
                    query.setDataSource(((DataSourceComponent) component).getDataSourceName());
                    query.setWorkflowNodeId(workflowNode.getId());
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
                persistenceManager.saveQuery(query);
                ((DataSourceExecutionTask) task).setComponent((DataSourceComponent) component);
                task.setInputParameterValue("query", query.toString());
            }
            task.setWorkflowNodeId(workflowNode.getId());
            String nodeAffinity = component.getNodeAffinity();
            if (nodeAffinity != null && !"Any".equals(nodeAffinity)) {
                task.setExecutionNodeHostName(nodeAffinity);
            }
            task.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        }
        return task;
    }

}
