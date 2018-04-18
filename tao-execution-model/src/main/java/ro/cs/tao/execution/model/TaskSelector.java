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

import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Interface for implementors of task choosing algorithms for a job
 *
 * @author Cosmin Cara
 */
public interface TaskSelector<T> {
    void setWorkflowProvider(Function<Long, WorkflowNodeDescriptor> workflowProvider);
    void setTaskByNodeProvider(BiFunction<Long, Long, ExecutionTask> taskByNodeProvider);
    void setNodesByComponentProvider(BiFunction<Long, String, List<WorkflowNodeDescriptor>> nodesByComponentProvider);
    Class<T> getTaskContainerClass();
    List<ExecutionTask> chooseNext(T job, ExecutionTask currentTask);
}
