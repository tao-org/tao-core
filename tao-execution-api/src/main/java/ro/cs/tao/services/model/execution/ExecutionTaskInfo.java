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
package ro.cs.tao.services.model.execution;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;

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
    private String componentId;
    private String executionNodeHostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String internalState;
    private String log;

    private ExecutionStatus executionStatus;
    private List<Variable> inputParameterValues;
    private List<Variable> outputParameterValues;

    ExecutionTaskInfo(){}

    public ExecutionTaskInfo(final ExecutionTask executionTask) {
        this.id = executionTask.getId();
        this.groupTask = executionTask.getGroupTask() != null ? new ExecutionGroupInfo(executionTask.getGroupTask()) : null;
        this.workflowNodeId = executionTask.getWorkflowNodeId();
        this.level = executionTask.getLevel();
        try {
            TaoComponent component = null;
            if (executionTask instanceof DataSourceExecutionTask) {
                component = ((DataSourceExecutionTask) executionTask).getComponent();
            } else if (executionTask instanceof ProcessingExecutionTask) {
                component = ((ProcessingExecutionTask) executionTask).getComponent();
            }
            this.componentId = component != null ? component.getId() : "n/a";
        } catch (Exception e) {
            this.componentId = "n/a";
        }
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

    public ExecutionGroupInfo getGroupTask() {
        return groupTask;
    }

    public Long getWorkflowNodeId() {
        return workflowNodeId;
    }

    public int getLevel() {
        return level;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getExecutionNodeHostName() {
        return executionNodeHostName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getInternalState() {
        return internalState;
    }

    public String getLog() {
        return log;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public List<Variable> getInputParameterValues() {
        return inputParameterValues;
    }

    public List<Variable> getOutputParameterValues() {
        return outputParameterValues;
    }
}
