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

import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Default implementation for choosing the next task to be executed from a job.
 */
public class DefaultJobTaskSelector implements TaskSelector<ExecutionJob> {

    private Function<Long, WorkflowNodeDescriptor> workflowProvider;
    private BiFunction<Long, Long, ExecutionTask> taskByNodeProvider;
    private BiFunction<Long, String, List<WorkflowNodeDescriptor>> nodesByComponentProvider;
    private final Logger logger = Logger.getLogger(DefaultJobTaskSelector.class.getName());

    public DefaultJobTaskSelector() { }

    @Override
    public void setWorkflowProvider(Function<Long, WorkflowNodeDescriptor> workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    @Override
    public void setTaskByNodeProvider(BiFunction<Long, Long, ExecutionTask> taskByNodeProvider) {
        this.taskByNodeProvider = taskByNodeProvider;
    }

    @Override
    public void setNodesByComponentProvider(BiFunction<Long, String, List<WorkflowNodeDescriptor>> nodesByComponentProvider) {
        this.nodesByComponentProvider = nodesByComponentProvider;
    }

    @Override
    public Class<ExecutionJob> getTaskContainerClass() {
        return ExecutionJob.class;
    }

    @Override
    public List<ExecutionTask> chooseNext(ExecutionJob job, ExecutionTask currentTask) {
        List<ExecutionTask> next = null;
        List<ExecutionTask> tasks = job.orderTasks();
        if (tasks != null && tasks.size() > 0) {
            next = new ArrayList<>();
            ExecutionStatus status = job.getExecutionStatus();
            switch (status) {
                // If the job is not started, we return the first task in line
                case UNDETERMINED:
                    next.add(tasks.get(0));
                    break;
                // If the job is already queued for execution,
                // there should be already at least one task queued for execution, so we don't return another one
                case QUEUED_ACTIVE:
                    break;
                // If the job is suspended, return the first task that was suspended
                case SUSPENDED:
                    next.add(tasks.stream()
                                  .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                                  .findFirst().orElse(null));
                    break;
                // If the job is running, return the first task that is not started
                case RUNNING:
                    List<ExecutionTask> candidates = getCandidatesForExecution(job, currentTask);
                    if (candidates != null) {
                        next.addAll(candidates);
                    }
                    break;
                // If the job was cancelled, or failed, or completed its execution, do nothing
                case DONE:
                case FAILED:
                case CANCELLED:
                default:
                    break;
            }
        }
        return next;
    }

    private List<ExecutionTask> getCandidatesForExecution(ExecutionJob job, ExecutionTask task) {
        if (task == null || task.getExecutionStatus() != ExecutionStatus.DONE) {
            return null;
        }
        WorkflowNodeDescriptor workflowNode = this.workflowProvider.apply(task.getWorkflowNodeId());
        if (workflowNode == null) {
            logger.severe(String.format("No workflow node with id %s was found in the database", task.getWorkflowNodeId()));
            return null;
        }
        WorkflowDescriptor workflow = workflowNode.getWorkflow();
        List<WorkflowNodeDescriptor> childNodes = workflow.findChildren(workflow.getNodes(), workflowNode);
        if (childNodes == null || childNodes.size() == 0) {
            return null;
        }
        if (childNodes.size() == 1) {
            return childNodes.stream().map(n -> {
                ExecutionTask next = this.taskByNodeProvider.apply(job.getId(), n.getId());
                if (!(task instanceof DataSourceExecutionTask && next instanceof ExecutionGroup)) {
                    List<Variable> outputs = task.getOutputParameterValues();
                    n.getIncomingLinks().forEach(link -> {
                        for (Variable variable : outputs) {
                            if (variable.getKey().equals(link.getInput().getName())) {
                                next.setInputParameterValue(link.getOutput().getName(), variable.getValue());
                            }
                        }
                    });
                }
                return next;
            })
            .collect(Collectors.toList());
        } else {
            return childNodes.stream().filter(n ->
                n.getIncomingLinks().stream().allMatch(link -> {
                  List<WorkflowNodeDescriptor> parents =
                          this.nodesByComponentProvider.apply(workflow.getId(), link.getInput().getParentId());
                  return parents.stream().allMatch(p -> {
                      ExecutionTask taskByNode = this.taskByNodeProvider.apply(job.getId(), p.getId());
                      return taskByNode != null && taskByNode.getExecutionStatus() == ExecutionStatus.DONE;
                  });
                }))
                .map(n -> {
                    ExecutionTask next = this.taskByNodeProvider.apply(job.getId(), n.getId());
                    if (!(task instanceof DataSourceExecutionTask && next instanceof ExecutionGroup)) {
                        List<Variable> outputs = task.getOutputParameterValues();
                        n.getIncomingLinks().forEach(link -> {
                            for (Variable variable : outputs) {
                                if (variable.getKey().equals(link.getInput().getName())) {
                                    next.setInputParameterValue(link.getOutput().getName(), variable.getValue());
                                }
                            }
                        });
                    }
                    return next;
                })
                .collect(Collectors.toList());
        }
    }
}
