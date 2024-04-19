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

import ro.cs.tao.component.Variable;
import ro.cs.tao.serialization.SerializationException;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specialization of an execution task to encapsulate the execution of a group of tasks.
 *
 * @author Cosmin Cara
 */
public class ExecutionGroup extends ExecutionTask {

    private List<ExecutionTask> tasks;

    @XmlTransient
    public List<ExecutionTask> getTasks() { return tasks; }
    public void setTasks(List<ExecutionTask> tasks) { this.tasks = tasks; }

    public ExecutionGroup() { }

    @Override
    public void setInputParameterValue(String parameterId, String value) {
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        Variable existing = this.inputParameterValues.stream()
                .filter(v -> v.getKey().equals(parameterId)).findFirst().orElse(null);
        if (existing != null) {
            existing.setValue(value);
        } else {
            this.inputParameterValues.add(new Variable(parameterId, value));
        }
        if (this.tasks != null && !this.tasks.isEmpty()) {
            // We may have a parallelism > 1, case in which there should be more than one initial task in the group
            List<ExecutionTask> firstTasks = this.tasks.stream().filter(t -> t.getLevel() == this.getLevel() + 1).collect(Collectors.toList());
            for (ExecutionTask task : firstTasks) {
                if (this.stateHandler != null) {
                    task.setInputParameterValues(this.stateHandler.advanceToNextState(this.inputParameterValues));
                } else {
                    task.setInputParameterValues(this.inputParameterValues);
                }
            }
        }
    }

    @Override
    public void setInputParameterValues(List<Variable> inputParameterValues) {
        super.setInputParameterValues(inputParameterValues);
    }

    @Override
    public void setOutputParameterValue(String parameterId, String value) {
        if (this.outputParameterValues == null) {
            this.outputParameterValues = new ArrayList<>();
        }
        Variable variable = this.outputParameterValues.stream()
                .filter(o -> o.getKey().equals(parameterId)).findFirst().orElse(null);
        if (variable != null) {
            variable.setValue(appendValueToList(variable.getValue(), value));
        } else {
            this.outputParameterValues.add(new Variable(parameterId, appendValueToList(null, value)));
        }
    }

    @Override
    public void setOutputParameterValues(List<Variable> parameterValues) {
        super.setOutputParameterValues(parameterValues);
    }

    @Override
    public void setInternalState(String internalState) {
        super.setInternalState(internalState);
        if (this.internalState != null && this.stateHandler != null) {
            try {
                this.stateHandler.setCurrentState(this.internalState);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String buildExecutionCommand() {
        throw new UnsupportedOperationException("Operation not permitted on a task group");
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

    public ExecutionTask getByWorkflowNode(Long workflowNodeId) {
        ExecutionTask task = null;
        if (this.tasks != null) {
            task = this.tasks.stream()
                    .filter(t -> t.getWorkflowNodeId() != null && t.getWorkflowNodeId().equals(workflowNodeId))
                    .findFirst().orElse(null);
        }
        return task;
    }

    /**
     * Returns a list of mapped variables for the first tasks in group.
     * If the <code>inputParameterValues</code> collection contains multiple variables, the mapping is done as follows:
     * - if the variable value is a single value, it is transferred as-is
     * - if no state handler is attached to this group, the variable value is transferred as-is
     * - if the variable value is a serialized list, the item of the list corresponding to the internal state index is transferred
     *   and the internal state index is incremented
     */
    /*private List<Variable> mapInput(List<Variable> inputs) {
        setInputParameterValues(inputs);
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        List<Variable> mappedInput = new ArrayList<>();
        for (Variable inputParameterValue : this.inputParameterValues) {
            Variable newVar = new Variable();
            newVar.setKey(inputParameterValue.getKey());
            newVar.setValue(inputParameterValue.getValue());
            mappedInput.add(newVar);
        }
        return mappedInput;
    }*/

    /**
     *
     * TASK ID is NULL until save, so this allows only adding the first sub-task on the group, the other are seen as duplicates, since all have identical ID null
     */
    private boolean contains(ExecutionTask task) {
        // TASK ID is NULL until save, so no comparison by ID should be made in this case, because it will allow only adding the first sub-task on the group, the other are seen as duplicates, since all have identical ID null
        if (task != null && task.getId() == null) {
            return this.tasks != null && this.tasks.contains(task);
        }

        if (task != null && task.getId() != null) {
            return this.tasks != null && this.tasks.stream().anyMatch(t -> t.getId().equals(task.getId()));
        }

        return false;
    }
}
