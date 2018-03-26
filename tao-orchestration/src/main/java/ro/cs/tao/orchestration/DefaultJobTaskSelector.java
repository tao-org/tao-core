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

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.TaskSelector;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Default implementation for choosing the next task to be executed from a job.
 */
public class DefaultJobTaskSelector implements TaskSelector<ExecutionJob> {

    private final PersistenceManager persistenceManager;
    private final Logger logger = Logger.getLogger(DefaultJobTaskSelector.class.getName());

    DefaultJobTaskSelector(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public List<ExecutionTask> chooseNext(ExecutionJob job, ExecutionTask currentTask) {
        List<ExecutionTask> next = null;
        List<ExecutionTask> tasks = job.orderTasks();
        if (tasks != null && tasks.size() > 0) {
            next = new ArrayList<>();
            switch (job.getExecutionStatus()) {
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
                    /*next = tasks.stream()
                            .filter(t -> t.getExecutionStatus() == ExecutionStatus.UNDETERMINED)
                            .findFirst().orElse(null);*/
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
        WorkflowNodeDescriptor workflowNode = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        if (workflowNode == null) {
            logger.severe(String.format("No workflow node with id %s was found in the database", task.getWorkflowNodeId()));
            return null;
        }
        WorkflowDescriptor workflow = workflowNode.getWorkflow();
        List<WorkflowNodeDescriptor> childNodes = workflow.findChildren(workflow.getNodes(), workflowNode);
        if (childNodes == null || childNodes.size() == 0) {
            return null;
        }
        return childNodes.stream().filter(n ->
                n.getIncomingLinks().stream().allMatch(link -> {
                    List<WorkflowNodeDescriptor> parents =
                            persistenceManager.getWorkflowNodesByComponentId(workflow.getId(),link.getInput().getParentId());
                    return parents.stream().allMatch(p -> {
                        ExecutionTask taskByNode = persistenceManager.getTaskByJobAndNode(job.getId(), p.getId());
                        return taskByNode != null && taskByNode.getExecutionStatus() == ExecutionStatus.DONE;
                    });
                }))
                .map(n -> persistenceManager.getTaskByJobAndNode(job.getId(), n.getId()))
                .collect(Collectors.toList());
    }
}
