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

package ro.cs.tao.orchestration;

import ro.cs.tao.component.execution.ExecutionJob;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.component.execution.JobVisitor;

import java.util.List;

public class DefaultJobVisitor implements JobVisitor {
    @Override
    public ExecutionTask visit(ExecutionJob job) {
        ExecutionTask next = null;
        List<ExecutionTask> tasks = job.getTasks();
        if (tasks != null && tasks.size() > 0) {
            switch (job.getExecutionStatus()) {
                case UNDETERMINED:
                case QUEUED_ACTIVE:
                    next = tasks.get(0);
                    break;
                case SUSPENDED:
                    next = tasks.stream()
                            .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                            .findFirst().orElse(null);
                    break;
                case RUNNING:
                    for (ExecutionTask task : tasks) {
                        if (task.getExecutionStatus() != ExecutionStatus.RUNNING) {
                            next = task;
                            break;
                        }
                    }
                    break;
                case DONE:
                case FAILED:
                case CANCELLED:
                default:
                    break;
            }
        }
        return next;
    }
}
