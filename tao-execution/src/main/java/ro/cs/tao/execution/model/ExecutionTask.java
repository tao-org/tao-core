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
package ro.cs.tao.execution.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.component.Variable;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.security.SystemSessionContext;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.StringListAdapter;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for an execution item.
 *
 * @author Cosmin Udroiu
 */
public abstract class ExecutionTask extends LongIdentifiable implements StatusChangeListener {

    // format is: jobId-taskId-[internalState-]fileName
    protected static final String nameTemplate = "%s-%s-%s%s";

    private ExecutionGroup groupTask;
    private Long workflowNodeId;
    private int level;
    private String resourceId;
    private String executionNodeHostName;
    private String command;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdated;
    private String log;
    @JsonBackReference
    private ExecutionJob job;
    private ExecutionStatus executionStatus = ExecutionStatus.UNDETERMINED;
    private SessionContext context;
    // In case of parallel executions, the same WF node can produce multiple execution tasks
    private int instanceId;
    private Integer usedCPU;
    private Integer usedRAM;
    protected String instanceTemporaryOutput;
    protected String instanceTargetOutput;
    protected String internalState;
    List<Variable> inputParameterValues = new ArrayList<>();
    List<Variable> outputParameterValues = new ArrayList<>();
    InternalStateHandler<?> stateHandler;
    private Integer cardinality;

    public ExecutionTask() { }

    @Override
    public Long defaultId() { return null; }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public ExecutionGroup getGroupTask() { return groupTask; }
    public void setGroupTask(ExecutionGroup groupTask) { this.groupTask = groupTask; }

    public Long getWorkflowNodeId() { return workflowNodeId; }
    public void setWorkflowNodeId(Long workflowNodeId) { this.workflowNodeId = workflowNodeId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    /**
     * Sets the status of this task.
     * This method is intended to be called by the underlying persistence layer.
     * If the caller wants to change the status of the task,
     * the <code>changeStatus(ExecutionStatus)</code> should be used instead.
     */
    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public String getLog() { return log; }
    public void setLog(String log) { this.log = log; }

    public void setStateHandler(InternalStateHandler<?> handler) {
        this.stateHandler = handler;
        if (this.internalState == null) {
            try {
                this.internalState = this.stateHandler.serializeState();
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.stateHandler.setCurrentState(this.internalState);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }
        this.stateHandler.assignTask(this);
    }

    public InternalStateHandler<?> getStateHandler() { return this.stateHandler; }

    public String getInternalState() { return internalState; }
    public void setInternalState(String internalState) { this.internalState = internalState; }

    /**
     * Generic method to handle the advancement to the next internal state.
     * It should return <code>null</code> if either there is no internal state or the current state was the last one.
     */
    public Object nextInternalState() {
        return this.stateHandler != null ?
            this.stateHandler.nextState() : null;
    }

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
    public abstract void setInputParameterValue(String parameterId, String value);

    public List<Variable> getOutputParameterValues() {
        return outputParameterValues;
    }
    public void setOutputParameterValues(List<Variable> parameterValues) {
        this.outputParameterValues = parameterValues;
    }
    public abstract void setOutputParameterValue(String parameterId, String value);

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

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    @JsonBackReference
    public ExecutionJob getJob() { return job; }
    public void setJob(ExecutionJob job) {
        this.job = job;
    }

    public SessionContext getContext() { return context != null ? context : SystemSessionContext.instance(); }
    public void setContext(SessionContext context) { this.context = context; }

    public int getInstanceId() { return instanceId; }
    public void setInstanceId(int instanceId) { this.instanceId = instanceId; }

    public int getUsedCPU() {
        if (usedCPU == null) {
            usedCPU = 0;
        }
        return usedCPU;
    }
    public void setUsedCPU(Integer usedCPU) { this.usedCPU = usedCPU; }

    public int getUsedRAM() {
        if (usedRAM == null) {
            usedRAM = 0;
        }
        return usedRAM;
    }
    public void setUsedRAM(Integer usedRAM) { this.usedRAM = usedRAM; }

    public String getInstanceTemporaryOutput() {
        return instanceTemporaryOutput;
    }

    public void setInstanceTemporaryOutput(String instanceTemporaryOutput) {
        this.instanceTemporaryOutput = instanceTemporaryOutput;
    }

    public String getInstanceTargetOutput() {
        return instanceTargetOutput;
    }

    public void setInstanceTargetOutput(String instanceTargetOutput) {
        this.instanceTargetOutput = instanceTargetOutput;
    }

    public Integer getCardinality() {
        return cardinality;
    }

    public void setCardinality(Integer cardinality) {
        this.cardinality = cardinality;
    }

    public abstract String buildExecutionCommand();

    @Transient
    protected List<String> getListParameterValues(String valuesAsString) {
        return new StringListAdapter().marshal(valuesAsString);
    }

    protected String convertListToSingleValue(List<String> values) {
        return new StringListAdapter().unmarshal(values);
    }

    // ATTENTION: index is 1-based, while the list is 0-based
    @Transient
    protected String getListValue(String valuesAsString, int index) {
        List<String> values = getListParameterValues(valuesAsString);
        return values.get(index - 1);
    }

    protected String appendValueToList(String valuesAsString, String newValue) {
        List<String> values = (valuesAsString == null || valuesAsString.isEmpty()) ?
                new ArrayList<>() :
                getListParameterValues(valuesAsString);
        values.removeIf(v -> v.endsWith("null") || v.contains("[null]"));
        if (!values.contains(newValue) && newValue != null && !newValue.contains("[null]")) {
            values.add(newValue);
        }
        return convertListToSingleValue(values);
    }
}
