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
package ro.cs.tao.component.execution;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.Variable;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class ExecutionGroup extends ExecutionTask {

    private List<ExecutionTask> tasks;

    @XmlTransient
    public List<ExecutionTask> getTasks() { return tasks; }
    public void setTasks(List<ExecutionTask> tasks) { this.tasks = tasks; }

    public ExecutionGroup() { }

    public ExecutionGroup(ProcessingComponent processingComponent) {
        throw new IllegalArgumentException("Cannot assign a component to a task group");
    }

    @Override
    public void setParameterValue(String parameterId, String value) {
        super.setParameterValue(parameterId, value);
        if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(0).setParameterValue(parameterId, value);
        }
    }

    @Override
    public void setInputParameterValues(List<Variable> inputParameterValues) {
        super.setInputParameterValues(inputParameterValues);
        if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(0).setInputParameterValues(inputParameterValues);
        }
    }

    @Override
    public String buildExecutionCommand() {
        throw new java.lang.UnsupportedOperationException("Operation not permitted on a task group");
    }

    @Override
    public void statusChanged(ExecutionTask changedTask) {
        ExecutionStatus previous = this.executionStatus;
        ExecutionStatus taskStatus = changedTask.getExecutionStatus();
        switch (taskStatus) {
            case SUSPENDED:
            case CANCELLED:
            case FAILED:
                bulkSetStatus(changedTask, taskStatus);
                this.executionStatus = taskStatus;
                break;
            case DONE:
                if (this.tasks.get(this.tasks.size() - 1).getId().equals(changedTask.getId())) {
                    this.executionStatus = ExecutionStatus.DONE;
                }
                break;
            default:
                // do nothing for other states
                break;
        }
        if (previous != null && previous != this.executionStatus) {
            this.job.statusChanged(this);
        }
    }

    public void addTask(ExecutionTask task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        if (!contains(task)) {
            this.tasks.add(task);
        }
        task.setGroupTask(this);
    }

    public void removeTask(ExecutionTask task) {
        if (contains(task)) {
            Iterator<ExecutionTask> iterator = this.tasks.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getId().equals(task.getId())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    public ExecutionTask getNext() {
        ExecutionTask next = null;
        if (this.tasks != null && this.tasks.size() > 0) {
            switch (this.executionStatus) {
                case UNDETERMINED:
                case QUEUED_ACTIVE:
                    next = this.tasks.get(0);
                    break;
                case SUSPENDED:
                    next = this.tasks.stream()
                                     .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                                     .findFirst().orElse(null);
                    break;
                case RUNNING:
                    for (ExecutionTask task : this.tasks) {
                        if (task.getExecutionStatus() != ExecutionStatus.RUNNING) {
                            next = task;
                            break;
                        }
                    }
                    break;
                case DONE:
                case FAILED:
                case CANCELLED:
                default:
                    break;
            }
        }
        return next;
    }

    public ExecutionTask getByWorkflowNode(Long workflowNodeId) {
        ExecutionTask task = null;
        if (this.tasks != null) {
            task = this.tasks.stream()
                    .filter(t -> t.getWorkflowNodeId() != null && t.getWorkflowNodeId().equals(workflowNodeId))
                    .findFirst().orElse(null);
        }
        return task;
    }

    @Override
    void internalStatusChange(ExecutionStatus status) {
        this.executionStatus = status;
        switch (status) {
            case SUSPENDED:
            case CANCELLED:
            case FAILED:
                for (int i = this.internalState; i < this.tasks.size(); i++) {
                    this.tasks.get(i).setExecutionStatus(status);
                }
                break;
            default:
                // do nothing for other states
                break;
        }
    }

    private void bulkSetStatus(ExecutionTask firstExculde, ExecutionStatus status) {
        if (this.tasks == null) {
            return;
        }
        int idx = 0;
        boolean found = false;
        while (idx < this.tasks.size()) {
            if (!found) {
                found = this.tasks.get(idx).getId().equals(firstExculde.getId());
            } else {
                this.tasks.get(idx).internalStatusChange(status);
            }
            idx++;
        }
        if (!found) {
            throw new IllegalArgumentException("Task not found");
        }
    }

    private boolean contains(ExecutionTask task) {
        return this.tasks != null && task != null && this.tasks.stream().anyMatch(t -> t.getId().equals(task.getId()));
    }
}
