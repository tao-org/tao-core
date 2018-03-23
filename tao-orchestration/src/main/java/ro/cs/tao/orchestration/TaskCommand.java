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

import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.ExecutionsManager;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;

import java.util.HashSet;
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

    private static PersistenceManager persistenceManager;

    private final ExecutionStatus requestedStatus;
    private final Set<ExecutionStatus> allowedStates;

    /**
     * Sets the persistence manager for all subclasses
     */
    public static void setPersistenceManager(PersistenceManager persister) {
        persistenceManager = persister;
    }

    private TaskCommand(ExecutionStatus requestedStatus) {
        this.requestedStatus = requestedStatus;
        this.allowedStates = getAllowedStates();
    }

    /**
     * Returns the set of states a task should be into when applying this command.
     */
    protected abstract Set<ExecutionStatus> getAllowedStates();

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
                task.changeStatus(this.requestedStatus);
                persistenceManager.updateExecutionTask(task);
            } catch (Exception ex) {
                task.changeStatus(ExecutionStatus.FAILED);
                throw new ExecutionException(ex);
            }

        }
    }

    /**
     * Encapsulates a START command for a task
     */
    private static class TaskStart extends TaskCommand {
        private TaskStart() {
            super(ExecutionStatus.QUEUED_ACTIVE);
        }
        @Override
        protected Set<ExecutionStatus> getAllowedStates() {
            return new HashSet<ExecutionStatus>() {{
                add(ExecutionStatus.UNDETERMINED);
            }};
        }
        @Override
        protected void doAction(ExecutionTask task) {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            executionsManager.execute(task);
        }
    }
    /**
     * Encapsulates a STOP command for a task
     */
    private static class TaskStop extends TaskCommand {
        private TaskStop() {
            super(ExecutionStatus.CANCELLED);
        }
        @Override
        protected Set<ExecutionStatus> getAllowedStates() {
            return new HashSet<ExecutionStatus>() {{
                add(ExecutionStatus.UNDETERMINED);
                add(ExecutionStatus.QUEUED_ACTIVE);
                add(ExecutionStatus.RUNNING);
            }};
        }
        @Override
        protected void doAction(ExecutionTask task) {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            executionsManager.stop(task);
        }
    }
    /**
     * Encapsulates a SUSPEND command for a task
     */
    private static class TaskSuspend extends TaskCommand {
        private TaskSuspend() {
            super(ExecutionStatus.SUSPENDED);
        }
        @Override
        protected Set<ExecutionStatus> getAllowedStates() {
            return new HashSet<ExecutionStatus>() {{
                add(ExecutionStatus.QUEUED_ACTIVE);
                add(ExecutionStatus.RUNNING);
            }};
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
            super(ExecutionStatus.QUEUED_ACTIVE);
        }
        @Override
        protected Set<ExecutionStatus> getAllowedStates() {
            return new HashSet<ExecutionStatus>() {{
                add(ExecutionStatus.SUSPENDED);
            }};
        }
        @Override
        protected void doAction(ExecutionTask task) {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            executionsManager.resume(task);
        }
    }
}
