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

import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cosmin Udroiu
 */
public class ExecutionTask<T extends TaoComponent> implements StatusChangeListener {
    private Long id;
    private ExecutionTask groupTask;
    private Long workflowNodeId;
    private T component;
    private String resourceId;
    private String executionNodeHostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Variable> inputParameterValues;
    String internalState;
    ExecutionJob job;
    ExecutionStatus executionStatus = ExecutionStatus.UNDETERMINED;

    public ExecutionTask() { }

    public ExecutionTask(T component) {
        this.component = component;
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

    public void setComponent(T component) {
        this.component = component;
    }
    public T getComponent() {
        return component;
    }

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
     * @param status The new status.
     */
    public void changeStatus(ExecutionStatus status) {
        ExecutionStatus previous = this.executionStatus;
        this.executionStatus = status;
        if (groupTask != null && previous != null && previous != status) {
            groupTask.statusChanged(this);
        }
    }

    public String getInternalState() { return internalState; }
    public void setInternalState(String internalState) { this.internalState = internalState; }

    /**
     * Generic method to handle the advancement to the next internal state.
     * It returns <code>null</code> if there is no internal state or the current state was the last one.
     * @param <S>   The type of the state data
     */
    <S> S nextInternalState() { return null; }

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
        boolean descriptorExists = false;
        if (this.component instanceof ProcessingComponent) {
            List<ParameterDescriptor> descriptorList = ((ProcessingComponent) this.component).getParameterDescriptors();
            for (ParameterDescriptor descriptor : descriptorList) {
                if (descriptor.getId().equals(parameterId)) {
                    descriptorExists = true;
                    break;
                }
            }
        }
        if (this.component instanceof DataSourceComponent) {
            DataSourceComponent component = (DataSourceComponent) this.component;
            Collection<ro.cs.tao.datasource.param.ParameterDescriptor> descriptors =
                    DataSourceManager.getInstance().getSupportedParameters(component.getSensorName(),
                                                                            component.getDataSourceName()).values();
            for (ro.cs.tao.datasource.param.ParameterDescriptor descriptor : descriptors) {
                if (descriptor.getName().equalsIgnoreCase(parameterId)) {
                    descriptorExists = true;
                    break;
                }
            }
        }
        if (!descriptorExists) {
            throw new ValidationException(String.format("The parameter ID [%s] does not exists in the component '%s'",
                                                        parameterId, component.getLabel()));
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
        if (component == null) {
            return null;
        }
        Map<String, String> inputParams = new HashMap<>();
        if (inputParameterValues != null) {
            inputParams.putAll(inputParameterValues.stream()
                                       .collect(Collectors.toMap(Variable::getKey, Variable::getValue)));
        }
        return this.component instanceof ProcessingComponent ?
                ((ProcessingComponent) this.component).buildExecutionCommand(inputParams) : null;
    }

    public ExecutionTask getNext() {
        return groupTask != null ? groupTask.getNext() : job.getNextTask();
    }
}
