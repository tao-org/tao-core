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

import ro.cs.tao.utils.TriFunction;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.List;
import java.util.function.Function;

/**
 * Interface for implementors of task choosing algorithms for a job
 *
 * @author Cosmin Cara
 */
public interface TaskSelector<T> {
    /**
     * Setter for the functor providing workflow nodes
     */
    void setWorkflowProvider(Function<Long, WorkflowNodeDescriptor> workflowProvider);

    /**
     * Setter for the functor providing execution tasks
     */
    void setTaskByNodeProvider(TriFunction<Long, Long, Integer, ExecutionTask> taskByNodeProvider);

    /**
     * Returns the class for the task container (i.e. either an ExecutionJob or an ExecutionGroup)
     */
    Class<T> getTaskContainerClass();

    /**
     * Given a task, it returns the tasks that would have to be executed next.
     * @param job           The parent job
     * @param currentTask   The task that has been executed
     */
    List<ExecutionTask> chooseNext(T job, ExecutionTask currentTask);

    /**
     * Given a task, it returns the Data Source tasks that initiated the current execution branch.
     * @param job           The parent job
     * @param currentTask   The task that is about to be executed
     */
    List<DataSourceExecutionTask> findDataSourceTasks(T job, ExecutionTask currentTask);
}
