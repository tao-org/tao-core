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

import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.utils.TriFunction;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Default implementation for choosing the next task to be executed from a job.
 *
 * @author Cosmin Cara
 */
public class DefaultJobTaskSelector implements TaskSelector<ExecutionJob> {

    private Function<Long, WorkflowNodeDescriptor> workflowProvider;
    private TriFunction<Long, Long, Integer, ExecutionTask> taskByNodeProvider;
    private final Logger logger = Logger.getLogger(DefaultJobTaskSelector.class.getName());

    public DefaultJobTaskSelector() { }

    @Override
    public void setWorkflowProvider(Function<Long, WorkflowNodeDescriptor> workflowProvider) {
        this.workflowProvider = workflowProvider;
    }

    @Override
    public void setTaskByNodeProvider(TriFunction<Long, Long, Integer, ExecutionTask> taskByNodeProvider) {
        this.taskByNodeProvider = taskByNodeProvider;
    }

    @Override
    public Class<ExecutionJob> getTaskContainerClass() {
        return ExecutionJob.class;
    }

    @Override
    public List<DataSourceExecutionTask> findDataSourceTasks(ExecutionJob job, ExecutionTask currentTask) {
        final WorkflowNodeDescriptor workflowNode = this.workflowProvider.apply(currentTask.getWorkflowNodeId());
        if (workflowNode == null) {
            logger.severe(String.format("No workflow node with id %s was found in the database",
                                        currentTask.getWorkflowNodeId()));
            return null;
        }
        /*WorkflowDescriptor workflow = workflowNode.getWorkflow();
        final List<WorkflowNodeDescriptor> ancestors = workflow.findAncestors(workflow.getOrderedNodes(), workflowNode);
        return job.orderedTasks().stream().filter(t -> t.getWorkflowNodeId().equals(ancestors.get(0).getId()) &&
                                                       t instanceof DataSourceExecutionTask)
                                          .map(t -> (DataSourceExecutionTask) t)
                                          .findFirst().orElse(null);*/
        return job.orderedTasks().stream()
                  .filter(t -> t instanceof DataSourceExecutionTask)
                  .map(t -> (DataSourceExecutionTask) t)
                  .collect(Collectors.toList());
    }

    @Override
    public List<ExecutionTask> chooseNext(ExecutionJob job, ExecutionTask currentTask) {
        List<ExecutionTask> next = null;
        List<ExecutionTask> tasks = job.orderedTasks();
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
        if (next != null) {
            next.forEach(t -> t.setContext(currentTask.getContext()));
        }
        return next;
    }

    private List<ExecutionTask> getCandidatesForExecution(ExecutionJob job, ExecutionTask task) {
        if (task == null) {
            return null;
        }
        final TransitionBehavior behavior = TaskUtilities.getTransitionBehavior(task);
        if (ExecutionStatus.DONE != task.getExecutionStatus() &&
                (behavior == null || behavior == TransitionBehavior.FAIL_ON_ERROR)) {
            return null;
        }
        final List<ExecutionTask> candidates = new ArrayList<>();
        job.getTaskDependencies().forEach((key, value) -> {
            if (value.contains(String.valueOf(task.getId()))) {
                if (value.stream()
                         .map(v -> job.getTasks().stream().filter(t -> t.getId().equals(Long.parseLong(v))).findFirst().get())
                         .allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE &&
                                        t.getOutputParameterValues() != null)) {
                    candidates.add(job.getTasks().stream().filter(t -> t.getId().equals(Long.parseLong(key))).findFirst().get());
                }
            }
        });
        return candidates;
    }
}
