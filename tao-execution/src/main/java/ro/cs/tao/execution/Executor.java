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
package ro.cs.tao.execution;

import ro.cs.tao.component.Identifiable;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * @author Cosmin Udroi
 */
public abstract class Executor extends Identifiable {
    private static final int TIMER_PERIOD = 5000;
    /* Flag for trying to close the monitoring thread in an elegant manner */
    protected Boolean isInitialized = false;
    protected final PersistenceManager persistenceManager = SpringContextBridge.services().getPersistenceManager();
    protected final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final Timer executionsCheckTimer = new Timer();

    public Executor() {
        super();
    }

    public Executor(String id) {
        super(id);
    }

    /**
     * Initializes this executor.
     * Inheritors should override this method and call the base implementation
     * after their initialization sequence.
     *
     * @throws ExecutionException
     */
    public void initialize() throws ExecutionException {
        synchronized (isInitialized) {
            if (isInitialized)
                return;
            // mark the executor as initialized
            isInitialized = true;
        }
        // once the session was created, start the timer
        executionsCheckTimer.schedule(new ExecutionsCheckTimer(this), 0, TIMER_PERIOD);
    }

    /**
     * Closes this executor.
     * Inheritors should override this method and call the base implementation
     * before their close sequence.
     *
     * @throws ExecutionException
     */
    public void close() throws ExecutionException {
        // stop the monitoring thread
        synchronized (isInitialized) {
            if (!isInitialized)
                return;
            isInitialized = false;
        }
        executionsCheckTimer.cancel();
    }

    /**
     * Checks if this executor can execute the given component.
     *
     * @param component     The component to execute
     */
    public boolean supports(TaoComponent component) { return (component instanceof ProcessingComponent); }

    /**
     * Starts the execution of a task.
     * @param task  The task to be executed
     *
     * @throws ExecutionException
     */
    public abstract void execute(ExecutionTask task) throws ExecutionException;

    /**
     * Tries to stop the given task.
     * @param task  The task to be stopped
     *
     * @throws ExecutionException
     */
    public abstract void stop(ExecutionTask task) throws ExecutionException;

    /**
     * Tries to suspend the execution of a task.
     * @param task  The task to be suspended
     *
     * @throws ExecutionException
     */
    public abstract void suspend(ExecutionTask task) throws ExecutionException;

    /**
     * Tries to resume the execution of a task.
     * @param task  The task to be resumed
     *
     * @throws ExecutionException
     */
    public abstract void resume(ExecutionTask task) throws ExecutionException;

    /**
     * Polls for the execution status of the tasks that were started.
     *
     * @throws ExecutionException
     */
    public abstract void monitorExecutions() throws ExecutionException;

    protected void markTaskFinished(ExecutionTask task, ExecutionStatus status) throws PersistenceException {
        task.setEndTime(LocalDateTime.now());
        changeTaskStatus(task, status);
    }

    protected void changeTaskStatus(ExecutionTask task, ExecutionStatus status) throws PersistenceException {
        if(status != task.getExecutionStatus()) {
            task.setExecutionStatus(status);
            persistenceManager.updateExecutionTask(task);
        }
    }

    protected class ExecutionsCheckTimer extends TimerTask {
        private final Executor executor;
        ExecutionsCheckTimer(Executor executor) {
            this.executor = executor;
        }
        @Override
        public void run() {
            try {
                executor.monitorExecutions();
            } catch (ExecutionException e) {
                logger.severe("Error during monitoring executions: " + e.getMessage());
            }

        }
    }
}
