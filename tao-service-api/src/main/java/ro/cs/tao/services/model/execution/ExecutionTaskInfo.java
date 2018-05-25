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
package ro.cs.tao.services.model.execution;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Light wrapper over ExecutionTask entity for services operations purpose
 * @author Oana H.
 */
public class ExecutionTaskInfo {
    private Long id;

    @JsonBackReference
    private ExecutionGroupInfo groupTask;

    private Long workflowNodeId;
    private int level;
    private String resourceId;
    private String executionNodeHostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    protected String internalState;
    private String log;

    private ExecutionStatus executionStatus;
    List<Variable> inputParameterValues;
    List<Variable> outputParameterValues;

    public ExecutionTaskInfo(){}

    public ExecutionTaskInfo(final ExecutionTask executionTask) {
        this.id = executionTask.getId();
        this.groupTask = executionTask.getGroupTask() != null ? new ExecutionGroupInfo(executionTask.getGroupTask()) : null;
        this.workflowNodeId = executionTask.getWorkflowNodeId();
        this.level = executionTask.getLevel();
        this.resourceId = executionTask.getResourceId();
        this.executionNodeHostName = executionTask.getExecutionNodeHostName();
        this.startTime = executionTask.getStartTime();
        this.endTime = executionTask.getEndTime();
        this.internalState = executionTask.getInternalState();
        this.log = executionTask.getLog();
        this.executionStatus = executionTask.getExecutionStatus();
        this.inputParameterValues = executionTask.getInputParameterValues();
        this.outputParameterValues = executionTask.getOutputParameterValues();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExecutionGroupInfo getGroupTask() {
        return groupTask;
    }

    public void setGroupTask(ExecutionGroupInfo groupTask) {
        this.groupTask = groupTask;
    }

    public Long getWorkflowNodeId() {
        return workflowNodeId;
    }

    public void setWorkflowNodeId(Long workflowNodeId) {
        this.workflowNodeId = workflowNodeId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getExecutionNodeHostName() {
        return executionNodeHostName;
    }

    public void setExecutionNodeHostName(String executionNodeHostName) {
        this.executionNodeHostName = executionNodeHostName;
    }

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

    public String getInternalState() {
        return internalState;
    }

    public void setInternalState(String internalState) {
        this.internalState = internalState;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public List<Variable> getInputParameterValues() {
        return inputParameterValues;
    }

    public void setInputParameterValues(List<Variable> inputParameterValues) {
        this.inputParameterValues = inputParameterValues;
    }

    public List<Variable> getOutputParameterValues() {
        return outputParameterValues;
    }

    public void setOutputParameterValues(List<Variable> outputParameterValues) {
        this.outputParameterValues = outputParameterValues;
    }
}
