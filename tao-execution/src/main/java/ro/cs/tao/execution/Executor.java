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
package ro.cs.tao.execution;

import ro.cs.tao.Tuple;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.security.SystemSessionContext;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Base class for executors.
 *
 * @author Cosmin Udroiu
 */
public abstract class Executor<T extends ExecutionTask> extends StringIdentifiable {
    private static final String TIMER_PERIOD = "5";
    /* Flag for trying to close the monitoring thread in an elegant manner */
    private static final Map<String, Tuple<Integer, Long>> resourcesUsage = new HashMap<>();
    protected Boolean isInitialized = false;
    protected final PersistenceManager persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
    protected final Logger logger = Logger.getLogger(getClass().getName());
    private final Timer executionsCheckTimer = new Timer("exec-monitor");
    private final Map<Long, SessionContext> contextMap = new HashMap<>();

    public static Tuple<Integer, Long> getResourcesInUse(String userName) {
        Tuple<Integer, Long> tuple = resourcesUsage.get(userName);
        if (tuple == null) {
            tuple = new Tuple<>(0, 0L);
        }
        return tuple;
    }

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
        int pollingInterval = 1000 *
                Integer.parseInt(ConfigurationManager.getInstance().getValue("tao.drmaa.polling.interval", TIMER_PERIOD));
        initialize(pollingInterval);
    }

    public void initialize(int pollingInterval) throws ExecutionException {
        synchronized (isInitialized) {
            if (isInitialized)
                return;
            // mark the executor as initialized
            isInitialized = true;
        }
        // once the session was created, start the timer
        executionsCheckTimer.schedule(new ExecutionsCheckTimer(this), 0, pollingInterval);
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
    public abstract void execute(T task) throws ExecutionException;

    /**
     * Tries to stop the given task.
     * @param task  The task to be stopped
     *
     * @throws ExecutionException
     */
    public abstract void stop(T task) throws ExecutionException;

    /**
     * Tries to suspend the execution of a task.
     * @param task  The task to be suspended
     *
     * @throws ExecutionException
     */
    public abstract void suspend(T task) throws ExecutionException;

    /**
     * Tries to resume the execution of a task.
     * @param task  The task to be resumed
     *
     * @throws ExecutionException
     */
    public abstract void resume(T task) throws ExecutionException;

    /**
     * Polls for the execution status of the tasks that were started.
     *
     * @throws ExecutionException
     */
    public abstract void monitorExecutions() throws ExecutionException;

    protected void markTaskFinished(ExecutionTask task, ExecutionStatus status) {
        task.setEndTime(LocalDateTime.now());
        changeTaskStatus(task, status);
        this.contextMap.remove(task.getId());
    }

    protected void changeTaskStatus(ExecutionTask task, ExecutionStatus status) {
        changeTaskStatus(task, status, false);
    }

    protected void changeTaskStatus(ExecutionTask task, ExecutionStatus status, boolean firstTime) {
        if(status != task.getExecutionStatus()) {
            try {
                if (!this.contextMap.containsKey(task.getId())) {
                    if (task.getContext() == null) {
                        logger.warning(String.format("Context for task %s is null!", task.getId()));
                    }
                    this.contextMap.put(task.getId(), task.getContext());
                }
                final String userName = task.getContext().getPrincipal().getName();
                final int cpu = task.getUsedCPU();
                final long memory = task.getUsedRAM();
                if (firstTime) {
                    task.setExecutionStatus(status);
                    task.setLastUpdated(LocalDateTime.now());
                    persistenceManager.updateExecutionTask(task);
                    increment(userName, cpu, memory);
                } else {
                    switch (status) {
                        case DONE:
                            task.setExecutionStatus(status);
                            task.setLastUpdated(LocalDateTime.now());
                            persistenceManager.updateExecutionTask(task);
                            decrement(userName, cpu, memory);
                            break;
                        case CANCELLED:
                        case SUSPENDED:
                        case FAILED:
                            persistenceManager.updateTaskStatus(task, status);
                            decrement(userName, cpu, memory);
                            break;
                        case RUNNING:
                            persistenceManager.updateTaskStatus(task, status);
                            break;
                    }
                }
            } catch (PersistenceException e) {
                logger.severe(e.getMessage());
            }
            SessionContext context = this.contextMap.get(task.getId());
            if (context == null) {
                context = SystemSessionContext.instance();
            }
            Messaging.send(context.getPrincipal(), Topic.EXECUTION.value(), task.getId(), status.name());
        }
    }

    private void decrement(String userName, int cpu, long memory) {
        synchronized (resourcesUsage) {
            final Tuple<Integer, Long> tuple = resourcesUsage.get(userName);
            if (tuple != null) {
                resourcesUsage.put(userName,
                                   new Tuple<>(Math.max(0, tuple.getKeyOne() - cpu),
                                               Math.max(0, tuple.getKeyTwo() - memory)));
            }
        }
    }

    private void increment(String userName, int cpu, long memory) {
        synchronized (resourcesUsage) {
            final Tuple<Integer, Long> tuple = resourcesUsage.get(userName);
            if (tuple == null) {
                resourcesUsage.put(userName, new Tuple<>(cpu, memory));
            } else {
                resourcesUsage.put(userName,
                                   new Tuple<>(Math.max(0, tuple.getKeyOne() + cpu),
                                               Math.max(0, tuple.getKeyTwo() + memory)));
            }
        }
    }

    protected class ExecutionsCheckTimer extends TimerTask {
        private final Executor executor;
        private boolean inProgress;
        ExecutionsCheckTimer(Executor executor) {
            this.executor = executor;
            this.inProgress = false;
        }
        @Override
        public void run() {
            try {
                if (!this.inProgress) {
                    this.inProgress = true;
                    executor.monitorExecutions();
                }
            } catch (ExecutionException e) {
                logger.severe("Error during monitoring executions: " + e.getMessage());
            } finally {
                this.inProgress = false;
            }

        }
    }
}
