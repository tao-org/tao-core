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
package ro.cs.tao.execution.model;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Udroiu
 */
public class ExecutionJob implements StatusChangeListener {
    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long workflowId;
    private String queryId;
    private String userName;
    private ExecutionStatus executionStatus;
    private List<ExecutionTask> tasks;
    private static TaskSelector taskSelector;

    public ExecutionJob() {}

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getQueryId() { return queryId; }
    public void setQueryId(String queryId) { this.queryId = queryId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public long getWorkflowId() { return workflowId; }
    public void setWorkflowId(long workflowId) { this.workflowId = workflowId; }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setTasks(List<ExecutionTask> tasks) {
        this.tasks = tasks;
    }
    public List<ExecutionTask> getTasks() {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    @Transient
    public void setTaskSelector(TaskSelector visitor) { taskSelector = visitor; }
    public TaskSelector getTaskSelector() { return taskSelector; }

    public void addTask(ExecutionTask task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        if (this.tasks.stream().noneMatch(t -> t.getId() != null && t.getId().equals(task.getId()))) {
            this.tasks.add(task);
        }
    }

    /**
     * Returns the next task chosen to be executed.
     * The actual selection is performed by a concrete <code>TaskSelector</code>.
     */
    public ExecutionTask getNextTask() {
        if (this.executionStatus == ExecutionStatus.DONE || this.executionStatus == ExecutionStatus.FAILED) {
            return null;
        }
        if (taskSelector == null) {
            throw new IllegalArgumentException("No algorithm for choosing tasks is set");
        }
        return taskSelector.chooseNext(this);
    }

    public List<ExecutionTask> find(ExecutionStatus status) {
        List<ExecutionTask> running = null;
        if (this.tasks != null && this.tasks.size() > 0) {
            running = this.tasks.stream()
                                .filter(t -> t.getExecutionStatus() == status)
                                .collect(Collectors.toList());
        }
        return running;
    }

    @Override
    public void statusChanged(ExecutionTask changedTask) {
        ExecutionStatus taskStatus = changedTask.getExecutionStatus();
        switch (taskStatus) {
            case SUSPENDED:
            case CANCELLED:
            case FAILED:
                bulkSetStatus(changedTask, taskStatus);
                this.executionStatus = taskStatus;
                break;
            case RUNNING:
                if (this.executionStatus == ExecutionStatus.QUEUED_ACTIVE ||
                        this.executionStatus == ExecutionStatus.UNDETERMINED) {
                    this.executionStatus = ExecutionStatus.RUNNING;
                }
            case DONE:
                if (this.tasks.get(this.tasks.size() - 1).getId().equals(changedTask.getId())) {
                    this.executionStatus = ExecutionStatus.DONE;
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
                this.tasks.get(idx).changeStatus(status);
            }
            idx++;
        }
        if (!found) {
            throw new IllegalArgumentException("Task not found");
        }
    }
}
