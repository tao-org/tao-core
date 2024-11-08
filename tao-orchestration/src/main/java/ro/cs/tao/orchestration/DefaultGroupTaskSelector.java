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

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.utils.TriFunction;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Default implementation for choosing the next task to be executed from a group of tasks.
 *
 * @author Cosmin Cara
 */
public class DefaultGroupTaskSelector implements TaskSelector<ExecutionGroup> {

    private Function<Long, WorkflowNodeDescriptor> workflowProvider;
    private TriFunction<Long, Long, Integer, ExecutionTask> taskByNodeProvider;
    private final Logger logger = Logger.getLogger(DefaultJobTaskSelector.class.getName());

    public DefaultGroupTaskSelector() { }

    @Override
    public void setWorkflowProvider(Function<Long, WorkflowNodeDescriptor> workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    @Override
    public void setTaskByNodeProvider(TriFunction<Long, Long, Integer, ExecutionTask> taskByNodeProvider) {
        this.taskByNodeProvider = taskByNodeProvider;
    }

    @Override
    public Class<ExecutionGroup> getTaskContainerClass() {
        return ExecutionGroup.class;
    }

    @Override
    public List<DataSourceExecutionTask> findDataSourceTasks(ExecutionGroup job, ExecutionTask currentTask) {
        WorkflowNodeDescriptor workflowNode = this.workflowProvider.apply(currentTask.getWorkflowNodeId());
        if (workflowNode == null) {
            logger.severe(String.format("No workflow node with id %s was found in the database",
                                        currentTask.getWorkflowNodeId()));
            return null;
        }
        //WorkflowDescriptor workflow = workflowNode.getWorkflow();
        /*List<WorkflowNodeDescriptor> ancestors = workflow.findAncestors(workflow.getOrderedNodes(), workflowNode);
        return job.getTasks().stream().filter(t -> t.getWorkflowNodeId().equals(ancestors.get(0).getId()) &&
                                                   t instanceof DataSourceExecutionTask)
                                      .map(t -> (DataSourceExecutionTask) t)
                                      .findFirst().orElse(null);*/
        return job.getJob().orderedTasks().stream()
                  .filter(t -> t instanceof DataSourceExecutionTask)
                  .map(t -> (DataSourceExecutionTask) t)
                  .collect(Collectors.toList());
    }

    @Override
    public List<ExecutionTask> chooseNext(ExecutionGroup taskHolder, ExecutionTask currentTask) {
        List<ExecutionTask> next = null;
        List<ExecutionTask> tasks = taskHolder.getTasks();
        if (tasks != null && !tasks.isEmpty()) {
            next = new ArrayList<>();
            switch (taskHolder.getExecutionStatus()) {
                // If the group is not started, we return the first task in line
                case UNDETERMINED:
                    ExecutionTask task = tasks.get(0);
                    if (task instanceof ExecutionGroup) {
                        ExecutionGroup groupTask = (ExecutionGroup) task;
                        ProcessingComponent component = ((ProcessingExecutionTask) currentTask).getComponent();
                        List<TargetDescriptor> targets = component.getTargets();
                        int cardinality = (targets != null && !targets.isEmpty()) ? targets.get(0).getCardinality() : 1;
                        groupTask.setStateHandler(
                                new LoopStateHandler(
                                        new LoopState(cardinality, 1)));
                    }
                    if (tasks.get(tasks.size() - 1).getId().equals(currentTask.getId())) {
                        // force remapping of inputs
                        List<Variable> groupInput = taskHolder.getInputParameterValues();
                        if (groupInput != null) {
                            for (Variable var : groupInput) {
                                taskHolder.setInputParameterValue(var.getKey(), var.getValue());
                            }
                        }
                    } else{
                        List<Variable> values = currentTask.getOutputParameterValues();
                        if (values != null && !values.isEmpty()) {
                            for (Variable variable : values) {
                                task.setInputParameterValue(variable.getKey(), variable.getValue());
                            }
                        }
                    }
                    next.add(task);
                    break;
                // If the group is already queued for execution,
                // there should be already at least one task queued for execution, so we don't return another one
                case QUEUED_ACTIVE:
                    break;
                case SUSPENDED:
                    next.add(tasks.stream()
                                  .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                                  .findFirst().orElse(null));
                    break;
                case RUNNING:
                    List<ExecutionTask> candidates = getCandidatesForExecution(taskHolder, currentTask);
                    if (candidates != null) {
                        next.addAll(candidates);
                    }
                    break;
                case DONE:
                case FAILED:
                case CANCELLED:
                default:
                    break;
            }
        }
        if (next != null) {
            next.forEach(t -> t.setContext(currentTask.getContext()));
        }
        return next;
    }

    private List<ExecutionTask> getCandidatesForExecution(ExecutionGroup group, ExecutionTask task) {
        if (task == null || task.getExecutionStatus() != ExecutionStatus.DONE) {
            return null;
        }
        WorkflowNodeDescriptor workflowNode = this.workflowProvider.apply(task.getWorkflowNodeId());
        if (workflowNode == null) {
            logger.severe(String.format("No workflow node group with id %s was found in the database",
                                        group.getWorkflowNodeId()));
            return null;
        }
        WorkflowNodeGroupDescriptor nodeGroup =
                (WorkflowNodeGroupDescriptor) this.workflowProvider.apply(group.getWorkflowNodeId());
        List<WorkflowNodeDescriptor> childNodes = nodeGroup.findChildren(nodeGroup.getNodes(), workflowNode);
        if (childNodes == null || childNodes.isEmpty()) {
            return null;
        }

        List<ExecutionTask> nextOnes = new ArrayList<>();
        for (WorkflowNodeDescriptor node : childNodes) {
            ExecutionTask t = this.taskByNodeProvider.apply(group.getId(), node.getId(), task.getInstanceId());
            if (t.getExecutionStatus() != ExecutionStatus.QUEUED_ACTIVE && t.getExecutionStatus() != ExecutionStatus.RUNNING) {
                if (TaskUtilities.haveParentsCompleted(t)) {
                    //nextOnes.add(TaskUtilities.transferParentOutputs(t));
                    nextOnes.add(t);
                }
            }
        }
        return nextOnes;
    }
}
