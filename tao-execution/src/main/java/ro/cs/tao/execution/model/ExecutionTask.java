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
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.user.UserPreference;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.logger.Logger;

import java.beans.Transient;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Retrieves the execution group associated with this task.
     * The execution group represents a collection or grouping
     * of tasks that share a common context or configuration.
     *
     * @return the associated ExecutionGroup object
     */
    public ExecutionGroup getGroupTask() { return groupTask; }
    public void setGroupTask(ExecutionGroup groupTask) { this.groupTask = groupTask; }

    /**
     * Retrieves the identifier of the workflow node associated with this execution task.
     * The workflow node ID represents a unique identifier for a specific node within a workflow.
     *
     * @return the workflow node ID as a {@code Long}, or {@code null} if not set
     */
    public Long getWorkflowNodeId() { return workflowNodeId; }
    public void setWorkflowNodeId(Long workflowNodeId) { this.workflowNodeId = workflowNodeId; }

    /**
     * Retrieves the current level of the execution task.
     * The level typically represents the task's depth or hierarchy within a workflow.
     *
     * @return the level of the execution task as an integer
     */
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    /**
     * Retrieves the command string associated with this object.
     *
     * @return the command string
     */
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

    /**
     * Retrieves the log message.
     *
     * @return the log message as a string
     */
    public String getLog() { return log; }
    public void setLog(String log) { this.log = log; }

    /**
     * Sets the state handler for the execution task. This method interacts with the provided
     * {@link InternalStateHandler} to initialize or synchronize the internal state of the task.
     * If the internal state is not already set, it attempts to serialize it using the provided handler.
     * If the internal state is already defined, the handler sets the current state.
     * Finally, the handler is assigned to the task for future operations.
     *
     * @param handler the {@link InternalStateHandler} responsible for managing the internal state of the task.
     *                It should define methods for serializing, deserializing, and advancing the state.
     */
    public void setStateHandler(InternalStateHandler<?> handler) {
        this.stateHandler = handler;
        if (this.internalState == null) {
            try {
                this.internalState = this.stateHandler.serializeState();
            } catch (SerializationException e) {
                Logger.getLogger(getClass()).warning(e.getMessage());
            }
        } else {
            try {
                this.stateHandler.setCurrentState(this.internalState);
            } catch (SerializationException e) {
                Logger.getLogger(getClass()).warning(e.getMessage());
            }
        }
        this.stateHandler.assignTask(this);
    }

    /**
     * Retrieves the internal state handler associated with the execution task.
     * The state handler is responsible for managing and advancing the internal
     * state of the task, including serialization and deserialization of the state.
     *
     * @return the {@link InternalStateHandler} managing the internal state of the task
     */
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

    /**
     * Sets the resource ID for this instance.
     *
     * @param resourceId the identifier of the resource to set
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Retrieves the hostname of the execution node.
     *
     * @return the hostname of the execution node as a String
     */
    public String getExecutionNodeHostName() {
        return executionNodeHostName;
    }
    public void setExecutionNodeHostName(String executionNodeHostName) {
        this.executionNodeHostName = executionNodeHostName;
    }

    /**
     * Retrieves the list of input parameter values. These values represent
     * the input parameters required for a specific operation or process.
     *
     * @return a list of Variable objects representing the input parameter values
     */
    public List<Variable> getInputParameterValues() {
        return inputParameterValues;
    }
    public void setInputParameterValues(List<Variable> inputParameterValues) {
        this.inputParameterValues = inputParameterValues;
    }

    /**
     * Sets the input parameter value for the specified parameter ID.
     *
     * @param parameterId the unique identifier of the parameter whose value is to be set
     * @param value the value to assign to the specified parameter
     */
    public abstract void setInputParameterValue(String parameterId, String value);

    /**
     * Sets the value of an input parameter identified by its parameter ID.
     *
     * @param parameterId the unique identifier of the input parameter
     * @param value the value to set for the input parameter
     * @param sortLexicographically a flag indicating whether the input parameter value should be sorted lexicographically
     */
    public void setInputParameterValue(String parameterId, String value, boolean sortLexicographically) {
        setInputParameterValue(parameterId, value);
    }

    /**
     * Retrieves the list of output parameter values.
     *
     * @return a list of Variable objects representing the output parameter values.
     */
    public List<Variable> getOutputParameterValues() {
        return outputParameterValues;
    }
    public void setOutputParameterValues(List<Variable> parameterValues) {
        this.outputParameterValues = parameterValues;
    }

    /**
     * Sets the value of an output parameter identified by its ID.
     *
     * @param parameterId the identifier of the output parameter to be set.
     * @param value the value to assign to the specified output parameter.
     */
    public abstract void setOutputParameterValue(String parameterId, String value);

    /**
     * Retrieves the start time of the event or process.
     *
     * @return the start time as a LocalDateTime object
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Retrieves the end time associated with this object.
     *
     * @return the end time as a LocalDateTime object
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Retrieves the timestamp of the last update.
     *
     * @return a LocalDateTime object representing the last updated date and time.
     */
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    @JsonBackReference
    public ExecutionJob getJob() { return job; }
    public void setJob(ExecutionJob job) {
        this.job = job;
    }

    /**
     * Retrieves the {@link SessionContext} associated with the execution task.
     * If the context is not already initialized, it creates a new instance
     * of {@link SessionContext} with custom implementations for setting
     * the principal and preferences.
     *
     * @return the {@link SessionContext} representing the current session
     *         for this execution task.
     */
    public SessionContext getContext() {
        if (this.context == null) {
            this.context = new SessionContext() {
                @Override
                public Principal setPrincipal(Principal principal) {
                    return ExecutionTask.this.job != null
                            ? new UserPrincipal(ExecutionTask.this.job.getUserId())
                            : SystemPrincipal.instance();
                }

                @Override
                protected List<UserPreference> setPreferences() {
                    return List.of();
                }
            };
        }
        return this.context;
    }

    /**
     * Returns the identifier of the orchestrator instance that launched this task.
     *
     * @return the instance ID as an integer
     */
    public int getInstanceId() { return instanceId; }
    public void setInstanceId(int instanceId) { this.instanceId = instanceId; }

    /**
     * Retrieves the amount of CPU currently in use.
     *
     * @return the amount of CPU currently in use as an integer
     */
    public int getUsedCPU() {
        if (usedCPU == null) {
            usedCPU = 0;
        }
        return usedCPU;
    }
    public void setUsedCPU(Integer usedCPU) { this.usedCPU = usedCPU; }

    /**
     * Retrieves the amount of currently used RAM.
     * If the used RAM value is not initialized, it defaults to 0.
     *
     * @return the amount of used RAM in integer format
     */
    public int getUsedRAM() {
        if (usedRAM == null) {
            usedRAM = 0;
        }
        return usedRAM;
    }
    public void setUsedRAM(Integer usedRAM) { this.usedRAM = usedRAM; }

    /**
     * Retrieves the temporary output for the current instance.
     *
     * @return the temporary output associated with the instance
     */
    public String getInstanceTemporaryOutput() {
        return instanceTemporaryOutput;
    }

    public void setInstanceTemporaryOutput(String instanceTemporaryOutput) {
        this.instanceTemporaryOutput = instanceTemporaryOutput;
    }

    /**
     * Retrieves the target output of the instance.
     *
     * @return the target output of the instance as a String
     */
    public String getInstanceTargetOutput() {
        return instanceTargetOutput;
    }

    public void setInstanceTargetOutput(String instanceTargetOutput) {
        this.instanceTargetOutput = instanceTargetOutput;
    }

    /**
     * Retrieves the cardinality.
     *
     * @return the cardinality as an Integer
     */
    public Integer getCardinality() {
        return cardinality;
    }

    public void setCardinality(Integer cardinality) {
        this.cardinality = cardinality;
    }

    @Transient
    public boolean isRunning() {
        return this.executionStatus == ExecutionStatus.RUNNING;
    }

    @Transient
    public boolean hasNotCompleted() {
        return this.executionStatus == ExecutionStatus.UNDETERMINED ||
               this.executionStatus == ExecutionStatus.QUEUED_ACTIVE ||
               this.executionStatus == ExecutionStatus.RUNNING;
    }

    /**
     * Constructs and returns the command string to be executed.
     *
     * The implementation defines how the command is constructed, which
     * may include parameters, flags, or other details required to
     * execute the command in a specific context.
     *
     * @return a string representing the constructed execution command
     */
    public abstract String buildExecutionCommand();

    /**
     * Converts a delimited string into a list of string values using a StringListAdapter.
     *
     * @param valuesAsString the delimited string containing the list values
     * @return a list of string values parsed from the input string
     */
    @Transient
    protected List<String> getListParameterValues(String valuesAsString) {
        return new StringListAdapter().marshal(valuesAsString);
    }

    /**
     * Converts a list of strings into a single string value.
     *
     * @param values the list of strings to be converted
     * @return a single string value representing the content of the provided list
     */
    protected String convertListToSingleValue(List<String> values) {
        return new StringListAdapter().unmarshal(values);
    }

    /**
     * Retrieves a specific value from a delimited string by its 1-based index.
     *
     * @param valuesAsString the string containing delimited values
     * @param index the 1-based index of the value to retrieve
     * @return the value at the specified index in the list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Transient
    protected String getListValue(String valuesAsString, int index) {
        List<String> values = getListParameterValues(valuesAsString);
        return values.get(index - 1);
    }

    /**
     * Appends a new value to a comma-separated list of values, optionally provided as a single string.
     * If the new value is not already present in the list, it will be added.
     * Optionally, the resulting list can be sorted lexicographically.
     *
     * @param valuesAsString the existing comma-separated list of values as a string,
     *                       or null/empty if no values are present.
     * @param newValue the new value to be added to the list.
     * @return a string representing the updated list of values, including the new value
     *         if it was added.
     */
    protected String appendValueToList(String valuesAsString, String newValue) {
        return appendValueToList(valuesAsString, newValue, true);
    }

    /**
     * Appends a new value to a given comma-separated list of values, and optionally sorts the resulting list lexicographically.
     * If the provided value already exists in the list or is invalid, it will not be appended.
     * Null or invalid entries in the list are removed before processing.
     *
     * @param valuesAsString the existing comma-separated list of values as a string,
     *                       or null/empty if no values are currently present.
     * @param newValue the new value to be added to the list. It will not be added if it is null,
     *                 contains "[null]" or already exists in the list.
     * @param sortLexicographically a boolean flag indicating whether the resulting list should be sorted
     *                              lexicographically by filename, or left in its original input order.
     * @return a string representing the updated, comma-separated list of values after appending
     *         the new value (if valid and added) and optionally sorting the list.
     */
    protected String appendValueToList(String valuesAsString, String newValue, boolean sortLexicographically) {
        List<String> values = (valuesAsString == null || valuesAsString.isEmpty()) ?
                new ArrayList<>() :
                getListParameterValues(valuesAsString);
        values.removeIf(v -> v.endsWith("null") || v.contains("[null]"));
        if (!values.contains(newValue) && newValue != null && !newValue.contains("[null]")) {
            values.add(newValue);
        }
        if (sortLexicographically) {
            values.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if (StringUtilities.isNullOrEmpty(o1) || StringUtilities.isNullOrEmpty(o2)) {
                        return 0;
                    }
                    try {
                        return Paths.get(o1).getFileName().compareTo(Paths.get(o2).getFileName());
                    } catch (Exception e) {
                        return o1.compareTo(o2);
                    }
                }
            });
        }
        return convertListToSingleValue(values);
    }
}
