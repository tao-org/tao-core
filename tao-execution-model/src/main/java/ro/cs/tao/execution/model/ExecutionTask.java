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

import ro.cs.tao.component.Variable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cosmin Udroiu
 */
public abstract class ExecutionTask implements StatusChangeListener {
    private Long id;
    private ExecutionTask groupTask;
    private Long workflowNodeId;
    //protected T component;
    private int level;
    private String resourceId;
    private String executionNodeHostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    protected List<Variable> inputParameterValues;
    private String internalState;
    private ExecutionJob job;
    private ExecutionStatus executionStatus = ExecutionStatus.UNDETERMINED;

    public ExecutionTask() { }

    /*public ExecutionTask(T component) {
        this.component = component;
    }*/

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public ExecutionTask getGroupTask() { return groupTask; }
    public void setGroupTask(ExecutionTask groupTask) { this.groupTask = groupTask; }

    public Long getWorkflowNodeId() { return workflowNodeId; }
    public void setWorkflowNodeId(Long workflowNodeId) { this.workflowNodeId = workflowNodeId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

/*public abstract void setComponent(T component);
    public abstract T getComponent();*/

    /**
     * Sets the status of this task.
     * This method is intended to be called by the underlying persistence layer.
     * If the caller wants to change the status of the task,
     * the <code>changeStatus(ExecutionStatus)</code> should be used instead.
     */
    public void setExecutionStatus(ExecutionStatus executionStatus) { this.executionStatus = executionStatus; }
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    /**
     * Changes the status of this task and signals this to the parent group, if available.
     *
    public void changeStatus(ExecutionStatus status) {
        ExecutionStatus previous = this.executionStatus;
        this.executionStatus = status;
        if (groupTask != null && previous != null && previous != status) {
            groupTask.statusChanged(this);
        } else {
            this.job.statusChanged(this);
        }
    }*/

    public String getInternalState() { return internalState; }
    public void setInternalState(String internalState) { this.internalState = internalState; }

    /**
     * Generic method to handle the advancement to the next internal state.
     * It returns <code>null</code> if there is no internal state or the current state was the last one.
     * @param <S>   The type of the state data
     */
    public <S> S nextInternalState() { return null; }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    public String getResourceId() {
        return resourceId;
    }

    public String getExecutionNodeHostName() {
        return executionNodeHostName;
    }
    public void setExecutionNodeHostName(String executionNodeHostName) {
        this.executionNodeHostName = executionNodeHostName;
    }

    public List<Variable> getInputParameterValues() {
        return inputParameterValues;
    }
    public void setInputParameterValues(List<Variable> inputParameterValues) {
        this.inputParameterValues = inputParameterValues;
    }
    public abstract void setParameterValue(String parameterId, String value);

    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ExecutionJob getJob() { return job; }
    public void setJob(ExecutionJob job) {
        this.job = job;
    }

    public abstract String buildExecutionCommand();
}
