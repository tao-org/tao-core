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

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.SerializerFactory;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.stream.StreamSource;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author Cosmin Cara
 */
public class ExecutionGroup<T extends TaoComponent> extends ExecutionTask<T> {

    private List<ExecutionTask> tasks;
    private TaskSelector taskSelector;
    private Function<String, Integer> internalStateHandler;

    @XmlTransient
    public List<ExecutionTask> getTasks() { return tasks; }
    public void setTasks(List<ExecutionTask> tasks) { this.tasks = tasks; }

    public ExecutionGroup() { }

    public ExecutionGroup(T processingComponent) {
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

    @Transient
    public void setTaskSelector(TaskSelector visitor) { this.taskSelector = visitor; }
    public TaskSelector getTaskSelector() { return this.taskSelector; }

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
                // If the task is the last one of this group
                if (this.tasks.get(this.tasks.size() - 1).getId().equals(changedTask.getId())) {
                    Integer nextState = nextInternalState();
                    if (nextState != null) {
                        bulkSetStatus(null, ExecutionStatus.UNDETERMINED);
                        this.executionStatus = ExecutionStatus.UNDETERMINED;
                        try {
                            BaseSerializer<LoopState> serializer = SerializerFactory.create(LoopState.class, MediaType.JSON);
                            LoopState current = serializer.deserialize(new StreamSource(this.internalState));
                            current.setCurrent(nextState);
                            this.internalState = serializer.serialize(current);
                        } catch (SerializationException e) {
                            e.printStackTrace();
                        }
                    } else {
                        this.executionStatus = ExecutionStatus.DONE;
                    }
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
        if (this.taskSelector == null) {
            throw new IllegalArgumentException("No algorithm for choosing tasks is set");
        }
        return this.taskSelector.chooseNext(this);
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
    public void changeStatus(ExecutionStatus status) {
        this.executionStatus = status;
        switch (status) {
            case SUSPENDED:
            case CANCELLED:
            case FAILED:
                for (ExecutionTask task : this.tasks) {
                    ExecutionStatus taskStatus = task.getExecutionStatus();
                    if (taskStatus != ExecutionStatus.DONE) {
                        task.setExecutionStatus(status);
                    }
                }
                break;
            default:
                // do nothing for other states
                break;
        }
    }

    @Override
    Integer nextInternalState() {
        if (this.internalState == null || this.internalStateHandler == null) {
            return null;
        }
        return this.internalStateHandler.apply(this.internalState);
    }

    @Transient
    public void setInternalStateHandler(Function<String, Integer> stateHandler) {
        this.internalStateHandler = stateHandler;
    }

    private void bulkSetStatus(ExecutionTask firstExculde, ExecutionStatus status) {
        if (this.tasks == null) {
            return;
        }
        int idx = 0;
        boolean found = false;
        if (firstExculde == null) {
            firstExculde = this.tasks.get(0);
            firstExculde.setExecutionStatus(status);
        }
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

    private boolean contains(ExecutionTask task) {
        return this.tasks != null && task != null && this.tasks.stream().anyMatch(t -> t.getId() == task.getId());
    }
}
