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

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
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
        this.inputParameterValues.add(new Variable(parameterId, value));
        /*if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(0).setInputParameterValue(parameterId, value);
        }*/
    }

    @Override
    public void setInputParameterValues(List<Variable> inputParameterValues) {
        //super.setInputParameterValues(inputParameterValues);
        if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(0).setInputParameterValues(inputParameterValues);
        }
    }

    @Override
    public void setOutputParameterValue(String parameterId, String value) {
        if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(this.tasks.size() - 1).setOutputParameterValue(parameterId, value);
        }
    }

    @Override
    public void setOutputParameterValues(List<Variable> parameterValues) {
        super.setOutputParameterValues(parameterValues);
        if (this.tasks != null && this.tasks.size() > 0) {
            this.tasks.get(this.tasks.size() - 1).setOutputParameterValues(parameterValues);
        }
    }

    @Override
    public String buildExecutionCommand() {
        throw new java.lang.UnsupportedOperationException("Operation not permitted on a task group");
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
