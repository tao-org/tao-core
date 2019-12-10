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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import ro.cs.tao.execution.model.ExecutionGroup;
import ro.cs.tao.execution.model.ExecutionTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Light wrapper over ExecutionGroup entity for services operations purpose
 * @author Oana H.
 */
public class ExecutionGroupInfo extends ExecutionTaskInfo {

    @JsonManagedReference
    private List<ExecutionTaskInfo> tasks;

    public ExecutionGroupInfo(){}

    public ExecutionGroupInfo(final ExecutionGroup executionGroup){
        this.tasks = new ArrayList<>();
        for(ExecutionTask task : executionGroup.getTasks()){
            tasks.add(new ExecutionTaskInfo(task));
        }
    }

    public List<ExecutionTaskInfo> getTasks() {
        return tasks;
    }

    public void setTasks(List<ExecutionTaskInfo> tasks) {
        this.tasks = tasks;
    }
}
