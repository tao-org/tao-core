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

package ro.cs.tao.execution.local;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.persistence.WorkflowNodeProvider;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Debug implementation of an executor.
 *
 * @author Cosmin Cara
 */
public class NullExecutor extends Executor {
    private static final int MAX = 3;
    private static WorkflowNodeProvider workflowNodeProvider;
    private static ProcessingComponentProvider componentProvider;
    private final Map<ExecutionTask, Integer> counters = new HashMap<>();
    private final List<ExecutionTask> tasks = new ArrayList<>();

    public static void setWorkflowNodeProvider(WorkflowNodeProvider provider) { workflowNodeProvider = provider; }

    public static void setComponentProvider(ProcessingComponentProvider provider) { componentProvider = provider; }

    public NullExecutor() {
        super();
        if (workflowNodeProvider == null) {
            throw new ExecutionException("A WorkflowNodeProvider is required to use this executor");
        }
        if (componentProvider == null) {
            throw new ExecutionException("A ComponentProvider is required to use this executor");
        }
    }

    @Override
    public void execute(ExecutionTask task) throws ExecutionException {
        task.setResourceId(UUID.randomUUID().toString());
        synchronized (tasks) {
            tasks.add(task);
            counters.put(task, 0);
        }
        task.setStartTime(LocalDateTime.now());
        changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE);
    }

    @Override
    public void stop(ExecutionTask task) throws ExecutionException {
        synchronized (tasks) {
            tasks.remove(task);
            counters.remove(task);
        }
        markTaskFinished(task, ExecutionStatus.CANCELLED);
    }

    @Override
    public void suspend(ExecutionTask task) throws ExecutionException {
        synchronized (tasks) {
            tasks.remove(task);
            counters.remove(task);
        }
        markTaskFinished(task, ExecutionStatus.SUSPENDED);
    }

    @Override
    public void resume(ExecutionTask task) throws ExecutionException {
        if (!tasks.contains(task)) {
            tasks.add(task);
            counters.put(task, 0);
            task.setResourceId(UUID.randomUUID().toString());
        }
        changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE);
    }

    @Override
    public void monitorExecutions() throws ExecutionException {
        if (tasks != null) {
            synchronized (tasks) {
                tasks.stream().filter(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING)
                        .forEach(t -> counters.put(t, counters.get(t) + 1));
                tasks.stream()
                        .filter(t -> t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE)
                        .findFirst().ifPresent(task -> {
                            changeTaskStatus(task, ExecutionStatus.RUNNING);
                        });
                List<Map.Entry<ExecutionTask, Integer>> toTerminate =
                        counters.entrySet().stream().filter(e -> e.getValue() == MAX).collect(Collectors.toList());
                for (Map.Entry<ExecutionTask, Integer> entry : toTerminate) {
                    ExecutionTask task = entry.getKey();
                    counters.remove(task);
                    tasks.remove(task);
                    try {
                        WorkflowNodeDescriptor node = workflowNodeProvider.get(task.getWorkflowNodeId());
                        ProcessingComponent component = componentProvider.get(node.getComponentId());
                        component.getTargets().forEach(t -> task.setOutputParameterValue(t.getName(), t.getDataDescriptor().getLocation()));
                    } catch (Exception ex) {
                        logger.severe(ex.getMessage());
                        markTaskFinished(task, ExecutionStatus.FAILED);
                    }
                    markTaskFinished(task, ExecutionStatus.DONE);
                }
            }
        }
    }

    @Override
    public String defaultId() { return "NullExecutor"; }
}
