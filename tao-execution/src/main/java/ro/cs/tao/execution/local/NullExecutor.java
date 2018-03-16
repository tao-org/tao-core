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

package ro.cs.tao.execution.local;

import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NullExecutor extends Executor {
    private static final int MAX = 3;
    private Map<ExecutionTask, Integer> counters = new HashMap<>();
    private List<ExecutionTask> tasks = new ArrayList<>();

    @Override
    public void execute(ExecutionTask task) throws ExecutionException {
        tasks.add(task);
        counters.put(task, 0);
        task.changeStatus(ExecutionStatus.QUEUED_ACTIVE);
    }

    @Override
    public void stop(ExecutionTask task) throws ExecutionException {
        tasks.remove(task);
        counters.remove(task);
        markTaskFinished(task, ExecutionStatus.CANCELLED);
    }

    @Override
    public void suspend(ExecutionTask task) throws ExecutionException {
        tasks.remove(task);
        counters.remove(task);
        markTaskFinished(task, ExecutionStatus.SUSPENDED);
    }

    @Override
    public void resume(ExecutionTask task) throws ExecutionException {
        if (!tasks.contains(task)) {
            tasks.add(task);
            counters.put(task, 0);
        }
        task.changeStatus(ExecutionStatus.QUEUED_ACTIVE);
    }

    @Override
    public void monitorExecutions() throws ExecutionException {
        if (tasks != null) {
            tasks.stream().filter(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING)
                    .forEach(t -> counters.put(t, counters.get(t) + 1));
            tasks.stream()
                    .filter(t -> t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE)
                    .findFirst().ifPresent(task -> task.changeStatus(ExecutionStatus.RUNNING));
            List<Map.Entry<ExecutionTask, Integer>> toTerminate =
                    counters.entrySet().stream().filter(e -> e.getValue() == MAX).collect(Collectors.toList());
            for (Map.Entry<ExecutionTask, Integer> entry : toTerminate) {
                counters.remove(entry.getKey());
                tasks.remove(entry.getKey());
                entry.getKey().changeStatus(ExecutionStatus.DONE);
            }
        }
    }

    @Override
    public String defaultName() { return "NullExecutor"; }
}
