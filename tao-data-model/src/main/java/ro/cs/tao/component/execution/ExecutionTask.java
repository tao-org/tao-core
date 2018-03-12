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

import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.validation.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cosmin Udroiu
 */
public class ExecutionTask implements StatusChangeListener {
    private Long id;
    private ExecutionTask groupTask;
    private Long workflowNodeId;
    private ProcessingComponent processingComponent;
    private String resourceId;
    private String executionNodeHostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Variable> inputParameterValues;
    Integer internalState;
    ExecutionJob job;
    ExecutionStatus executionStatus = ExecutionStatus.UNDETERMINED;

    public ExecutionTask() { }

    public ExecutionTask(ProcessingComponent processingComponent) {
        this.processingComponent = processingComponent;
    }

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

    public void setProcessingComponent(ProcessingComponent processingComponent) {
        this.processingComponent = processingComponent;
    }
    public ProcessingComponent getProcessingComponent() {
        return processingComponent;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) { this.executionStatus = executionStatus; }
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }
    void internalStatusChange(ExecutionStatus status) {
        ExecutionStatus previous = this.executionStatus;
        this.executionStatus = status;
        if (groupTask != null && previous != null && previous != status) {
            groupTask.statusChanged(this);
        }
    }

    public Integer getInternalState() { return internalState; }
    public void setInternalState(Integer internalState) { this.internalState = internalState; }

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
    public void setParameterValue(String parameterId, String value) {
        List<ParameterDescriptor> descriptorList = this.processingComponent.getParameterDescriptors();
        boolean descriptorExists = false;
        for(ParameterDescriptor descriptor: descriptorList) {
            if (descriptor.getId().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        if(!descriptorExists) {
            throw new ValidationException("The parameter ID " + parameterId +
                    " does not exists in the processing component " +
                    processingComponent.getLabel());
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        this.inputParameterValues.add(new Variable(parameterId, value));
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

    public ExecutionJob getJob() {
        return job;
    }
    public void setJob(ExecutionJob job) {
        this.job = job;
    }

    public String buildExecutionCommand() {
        if (processingComponent == null) {
            return null;
        }
        Map<String, String> inputParams = new HashMap<>();
        if (inputParameterValues != null) {
            inputParams.putAll(inputParameterValues.stream()
                                       .collect(Collectors.toMap(Variable::getKey, Variable::getValue)));
        }
        return this.processingComponent.buildExecutionCommand(inputParams);
    }

    public ExecutionTask getNext() {
        return groupTask != null ? groupTask.getNext() : job.getNextTask();
    }
}
