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
package ro.cs.tao.orchestration.commands;

import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.ExecutionsManager;
import ro.cs.tao.execution.model.ExecutionGroup;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;

import java.util.EnumSet;
import java.util.Set;

/**
 * Class encapsulating the desired status of an execution task and the action to get there.
 *
 * @author Cosmin Cara
 */
public abstract class TaskCommand {
    public static final TaskCommand START = new TaskStart();
    public static final TaskCommand STOP = new TaskStop();
    public static final TaskCommand SUSPEND = new TaskSuspend();
    public static final TaskCommand RESUME = new TaskResume();

    private final Set<ExecutionStatus> allowedStates;

    /**
     * Sets the persistence manager for all subclasses
     */
    /*public static void setPersistenceManager(PersistenceManager persister) {
        persistenceManager = persister;
    }*/

    private TaskCommand() {
        this.allowedStates = getAllowedStates();
    }

    /**
     * Returns the set of states a task should be into when applying this command.
     */
    public abstract Set<ExecutionStatus> getAllowedStates();

    /**
     * The action to be performed in order to transition to the desired state.
     *
     * @param task      The task on which to apply the action
     */
    protected abstract void doAction(ExecutionTask task);

    /**
     * Applies the internal action of this command to the given task.
     *
     * @param task  The task on which to apply the action
     * @throws ExecutionException   If anything goes wrong
     */
    public void applyTo(ExecutionTask task) throws ExecutionException {
        if (task != null) {
            ExecutionStatus currentStatus = task.getExecutionStatus();
            if (!this.allowedStates.contains(currentStatus)) {
                throw new ExecutionException("Invalid task state");
            }
            try {
                doAction(task);
            } catch (Exception ex) {
                task.setExecutionStatus(ExecutionStatus.FAILED);
                throw new ExecutionException(ex);
            }

        }
    }

    /**
     * Encapsulates a START command for a task
     */
    private static class TaskStart extends TaskCommand {
        private TaskStart() {
            super();
        }
        @Override
        public Set<ExecutionStatus> getAllowedStates() {
            return EnumSet.of(ExecutionStatus.UNDETERMINED);
        }
        @Override
        protected void doAction(ExecutionTask task) {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            if (task instanceof ExecutionGroup) {
                ExecutionTask first = ((ExecutionGroup) task).getTasks().get(0);
                first.setContext(task.getContext());
                executionsManager.execute(first);
            } else {
                executionsManager.execute(task);
            }
        }
    }
    /**
     * Encapsulates a STOP command for a task
     */
    private static class TaskStop extends TaskCommand {
        private TaskStop() {
            super();
        }
        @Override
        public Set<ExecutionStatus> getAllowedStates() {
            return EnumSet.of(ExecutionStatus.UNDETERMINED, ExecutionStatus.QUEUED_ACTIVE, ExecutionStatus.RUNNING);
        }
        @Override
        protected void doAction(ExecutionTask task) {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            if (task instanceof ExecutionGroup) {
                ((ExecutionGroup) task).getTasks().forEach(executionsManager::stop);
            } else {
                executionsManager.stop(task);
            }
        }
    }
    /**
     * Encapsulates a SUSPEND command for a task
     */
    private static class TaskSuspend extends TaskCommand {
        private TaskSuspend() {
            super();
        }
        @Override
        public Set<ExecutionStatus> getAllowedStates() {
            return EnumSet.of(ExecutionStatus.QUEUED_ACTIVE, ExecutionStatus.RUNNING);
        }
        @Override
        protected void doAction(ExecutionTask task) {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            executionsManager.suspend(task);
        }
    }
    /**
     * Encapsulates a RESUME command for a task
     */
    private static class TaskResume extends TaskCommand {
        private TaskResume() {
            super();
        }
        @Override
        public Set<ExecutionStatus> getAllowedStates() {
            return EnumSet.of(ExecutionStatus.SUSPENDED);
        }
        @Override
        protected void doAction(ExecutionTask task) {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            executionsManager.resume(task);
        }
    }
}
