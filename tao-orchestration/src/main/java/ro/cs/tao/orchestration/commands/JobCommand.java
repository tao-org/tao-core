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
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class encapsulating the desired status of an execution job and the action to get there.
 *
 * @author Cosmin Cara
 */
public abstract class JobCommand {
    public static final JobCommand START = new JobStart();
    public static final JobCommand STOP = new JobStop();
    public static final JobCommand SUSPEND = new JobSuspend();
    public static final JobCommand RESUME = new JobResume();

    private static final PersistenceManager persistenceManager;

    private final ExecutionStatus requestedStatus;
    private final Set<ExecutionStatus> allowedStates;

    static {
        persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
    }

    private JobCommand(ExecutionStatus requestedStatus) {
        this.requestedStatus = requestedStatus;
        this.allowedStates = getAllowedStates();
    }

    /**
     * Returns the set of states a job should be into when applying this command.
     */
    protected abstract Set<ExecutionStatus> getAllowedStates();

    /**
     * The action to be performed in order to transition to the desired state.
     *
     * @param job      The job on which to apply the action
     */
    protected abstract void doAction(ExecutionJob job);

    /**
     * Applies the internal action of this command to the given job.
     *
     * @param job  The job on which to apply the action
     * @throws ExecutionException   If anything goes wrong
     */
    public void applyTo(ExecutionJob job) throws ExecutionException {
        if (job != null) {
            ExecutionStatus currentStatus = job.getExecutionStatus();
            if (!this.allowedStates.contains(currentStatus)) {
                throw new ExecutionException("Invalid job state");
            }
            try {
                job.setExecutionStatus(this.requestedStatus);
                if (job.getStartTime() == null) {
                    job.setStartTime(LocalDateTime.now());
                }
                job = persistenceManager.jobs().update(job);
                doAction(job);
            } catch (Exception ex) {
                job.setExecutionStatus(ExecutionStatus.FAILED);
                throw new ExecutionException(ex);
            }

        }
    }

    /**
     * Encapsulates a START command for a job.
     * After applying this command, the job and its first task should have
     * the <code>QUEUED_ACTIVE</code> status.
     */
    private static class JobStart extends JobCommand {
        private JobStart() {
            super(ExecutionStatus.QUEUED_ACTIVE);
        }
        @Override
        protected Set<ExecutionStatus> getAllowedStates() {
            return new HashSet<ExecutionStatus>() {{
                add(ExecutionStatus.UNDETERMINED);
            }};
        }
        @Override
        protected void doAction(ExecutionJob job) {
            List<ExecutionTask> tasks = job.orderedTasks();
            if (tasks == null || tasks.size() == 0) {
                throw new ExecutionException(String.format("Job %s doesn't contain any tasks", job.getId()));
            }
            //ExecutionTask firstTask = tasks.get(0);
            List<ExecutionTask> rootTasks = job.rootTasks();
            if (rootTasks.isEmpty()) {
                throw new ExecutionException(String.format("Job %s doesn't contain any first level tasks", job.getId()));
            }
            for (ExecutionTask firstTask : rootTasks) {
                firstTask.setContext(SessionStore.currentContext());
                TaskCommand.START.applyTo(firstTask);
            }
        }
    }

    /**
     * Encapsulates a STOP command for a job.
     * After applying this command the job should have the <code>CANCELLED</code> state.
     * All its tasks that were previously in one of the states:
     * <code>QUEUED_ACTIVE</code>,
     * <code>RUNNING</code>,
     * <code>UNDETERMINED</code>,
     * should also have the <code>CANCELLED</code> status.
     */
    private static class JobStop extends JobCommand {
        private JobStop() {
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
        protected void doAction(ExecutionJob job) {
            job.orderedTasks().stream()
                    .filter(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING ||
                            t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE ||
                            t.getExecutionStatus() == ExecutionStatus.UNDETERMINED)
                    .forEach(TaskCommand.STOP::applyTo);
        }
    }

    /**
     * Encapsulates a SUSPEND command for a job.
     * After applying this command the job should have the <code>SUSPENDED</code> state.
     * All its tasks that were previously in one of the states:
     * <code>QUEUED_ACTIVE</code>,
     * <code>RUNNING</code>,
     * should also have the <code>SUSPENDED</code> status.
     */
    private static class JobSuspend extends JobCommand {
        private JobSuspend() {
            super(ExecutionStatus.SUSPENDED);
        }
        @Override
        protected Set<ExecutionStatus> getAllowedStates() {
            return new HashSet<ExecutionStatus>() {{
                add(ExecutionStatus.RUNNING);
            }};
        }
        @Override
        protected void doAction(ExecutionJob job) {
            job.orderedTasks().stream()
                    .filter(t -> t.getExecutionStatus() == ExecutionStatus.RUNNING ||
                            t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE)
                    .forEach(TaskCommand.SUSPEND::applyTo);
        }
    }

    /**
     * Encapsulates a RESUME command for a job.
     * After applying this command the job should have the <code>QUEUED_ACTIVE</code> state.
     * All its tasks that were previously in the <code>SUSPENDED</code> state
     * should also have the <code>QUEUED_ACTIVE</code> status.
     */
    private static class JobResume extends JobCommand {
        private JobResume() {
            super(ExecutionStatus.QUEUED_ACTIVE);
        }
        @Override
        protected Set<ExecutionStatus> getAllowedStates() {
            return new HashSet<ExecutionStatus>() {{
                add(ExecutionStatus.SUSPENDED);
            }};
        }
        @Override
        protected void doAction(ExecutionJob job) {
            job.orderedTasks().stream()
                    .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                    .forEach(TaskCommand.RESUME::applyTo);
        }
    }
}
