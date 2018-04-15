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
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JobFactory {

    private final PersistenceManager persistenceManager;
    private final Logger logger;

    JobFactory(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.logger = Logger.getLogger(JobFactory.class.getName());
    }

    public ExecutionJob createJob(WorkflowDescriptor workflow, Map<String, String> inputs) throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setUserName("admin");
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            job = persistenceManager.saveExecutionJob(job);
            List<WorkflowNodeDescriptor> nodes = workflow.getOrderedNodes();
            for (int i = 0; i < nodes.size(); i++) {
                WorkflowNodeDescriptor node = nodes.get(i);
                ExecutionTask task = createTask(node);
                if (i == 0 && inputs != null) {
                    for (Map.Entry<String, String> entry : inputs.entrySet()) {
                        task.setInputParameterValue(entry.getKey(), entry.getValue());
                    }
                }
                task.setLevel(node.getLevel());
                persistenceManager.saveExecutionTask(task, job);
                logger.fine(String.format("Created task for node %s [level %s]", node.getId(), task.getLevel()));
            }
        }
        return job;
    }

    public ExecutionTask createTaskGroup(WorkflowNodeGroupDescriptor groupNode) throws PersistenceException {
        ExecutionGroup group = new ExecutionGroup();
        List<WorkflowNodeDescriptor> nodes = groupNode.getOrderedNodes();
        for (WorkflowNodeDescriptor node : nodes) {
            ExecutionTask task = createTask(node);
            group.addTask(task);
        }
        group.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        return group;
    }

    public ExecutionTask createTask(WorkflowNodeDescriptor workflowNode) throws PersistenceException {
        ExecutionTask task;
        if (workflowNode instanceof WorkflowNodeGroupDescriptor) {
            task = createTaskGroup((WorkflowNodeGroupDescriptor) workflowNode);
        } else {
            TaoComponent component;
            List<ParameterValue> customValues = workflowNode.getCustomValues();
            List<ComponentLink> links = workflowNode.getIncomingLinks();
            if (links != null && links.size() > 0) {
                task = new ProcessingExecutionTask();
                component = persistenceManager.getProcessingComponentById(workflowNode.getComponentId());
                ((ProcessingExecutionTask) task).setComponent((ProcessingComponent) component);
                // Placeholders for inputs of previous tasks
                links.forEach(link -> {
                    String name = link.getOutput().getName();
                    //String value = link.getInput().getDataDescriptor().getLocation();
                    task.setInputParameterValue(name, null);
                });
                // Placeholders for outputs of this task
                List<TargetDescriptor> targets = component.getTargets();
                targets.forEach(t -> task.setOutputParameterValue(t.getName(), null));
            } else {
                task = new DataSourceExecutionTask();
                component = persistenceManager.getDataSourceInstance(workflowNode.getComponentId());
                ((DataSourceExecutionTask) task).setComponent((DataSourceComponent) component);
            }
            task.setWorkflowNodeId(workflowNode.getId());
            if (customValues != null) {
                for (ParameterValue customValue : customValues) {
                    task.setInputParameterValue(customValue.getParameterName(), customValue.getParameterValue());
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

}
