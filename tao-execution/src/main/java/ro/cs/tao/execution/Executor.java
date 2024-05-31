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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.security.SystemSessionContext;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.utils.Tuple;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final List<TaskStatusListener> statusListeners = new ArrayList<>();
    protected static ExecutionTaskProvider taskProvider;
    protected static ExecutionJobProvider jobProvider;
    protected final AtomicBoolean isInitialized = new AtomicBoolean(false);
    protected final Logger logger = Logger.getLogger(getClass().getName());
    private final Timer executionsCheckTimer = new Timer("exec-monitor");
    private final Map<Long, SessionContext> contextMap = new HashMap<>();

    public static Tuple<Integer, Long> getResourcesInUse(String userId) {
        Tuple<Integer, Long> tuple = resourcesUsage.get(userId);
        if (tuple == null) {
            tuple = new Tuple<>(0, 0L);
        }
        return tuple;
    }

    public static void addStatusListener(TaskStatusListener statusListener) {
        Executor.statusListeners.add(statusListener);
    }

    public static void removeStatusListener(TaskStatusListener listener) {
        Executor.statusListeners.remove(listener);
    }

    public static void setTaskProvider(ExecutionTaskProvider provider) { taskProvider = provider; }

    public static void setJobProvider(ExecutionJobProvider provider) { jobProvider = provider; }

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
            if (isInitialized.get())
                return;
            // mark the executor as initialized
            isInitialized.set(true);
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
            if (!isInitialized.get())
                return;
            isInitialized.set(false);
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
        changeTaskStatus(task, status, status.friendlyName());
        this.contextMap.remove(task.getId());
    }

    protected void markTaskFinished(ExecutionTask task, ExecutionStatus status, String reason) {
        task.setEndTime(LocalDateTime.now());
        changeTaskStatus(task, status, reason);
        this.contextMap.remove(task.getId());
    }

    protected void changeTaskStatus(ExecutionTask task, ExecutionStatus status) {
        changeTaskStatus(task, status, false, status.friendlyName());
    }

    protected void changeTaskStatus(ExecutionTask task, ExecutionStatus status, String reason) {
        changeTaskStatus(task, status, false, reason);
    }

    protected void changeTaskStatus(ExecutionTask task, ExecutionStatus status, boolean firstTime, String reason) {
        if (status != task.getExecutionStatus()) {
            task.setLastUpdated(LocalDateTime.now());
            final ExecutionJob job = task.getJob();
            if ((status == ExecutionStatus.RUNNING || status == ExecutionStatus.DONE) && job.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE) {
                job.setExecutionStatus(ExecutionStatus.RUNNING);
                try {
                    jobProvider.update(job);
                } catch (PersistenceException e) {
                    logger.severe("Cannot set job status: " + e.getMessage());
                }
            }
            final String name = TaskUtilities.getTaskDescriptiveName(task);
            try {
                if (!this.contextMap.containsKey(task.getId())) {
                    if (task.getContext() == null) {
                        logger.warning(String.format("Context for task %s is null!", name));
                    }
                    this.contextMap.put(task.getId(), task.getContext());
                }
                final String userId = task.getContext().getPrincipal().getName();
                final int cpu = task.getUsedCPU();
                final long memory = task.getUsedRAM();
                if (firstTime) {
                    task.setExecutionStatus(status);
                    notifyStatusListener(task);
                    increment(userId, cpu, memory);
                } else {
                    switch (status) {
                        case PENDING_FINALISATION:
                        case DONE:
                            task.setEndTime(LocalDateTime.now());
                            task.setExecutionStatus(status);
                            notifyStatusListener(task);
                            decrement(userId, cpu, memory);
                            break;
                        case CANCELLED:
                        case SUSPENDED:
                        case FAILED:
                            task.setEndTime(LocalDateTime.now());
                            notifyStatusListener(task, status, reason);
                            decrement(userId, cpu, memory);
                            break;
                        case RUNNING:
                            if (task.getStartTime() == null) {
                                task.setStartTime(LocalDateTime.now());
                            }
                            if (task.getExecutionStatus() != status) {
                                notifyStatusListener(task, status, null);
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
            SessionContext context = this.contextMap.get(task.getId());
            if (context == null) {
                context = SystemSessionContext.instance();
            }
            //Messaging.send(context.getPrincipal(), Topic.EXECUTION.value(), task.getId(), status.name());
            final String message;
            if (task instanceof ProcessingExecutionTask) {
                message = "Task " + name
                        + (StringUtils.isNotEmpty(task.getExecutionNodeHostName()) ? " on node " + task.getExecutionNodeHostName() : "")
                        + " changed to " + status.friendlyName();
            } else if (task instanceof DataSourceExecutionTask) {
                message = "Query on " + ((DataSourceExecutionTask) task).getComponent().getDataSourceName()
                        + " for " + ((DataSourceExecutionTask) task).getComponent().getSensorName()
                        + " changed to " + status.friendlyName();
            } else {
                message = "Task " + task.getId() + (StringUtils.isNotEmpty(task.getExecutionNodeHostName()) ? " on node " + task.getExecutionNodeHostName() : "")
                        + " changed to " + status.friendlyName();
            }
            final Message msg = Message.create(job.getUserId(), task.getId(), message, status.name(), true);
            msg.setTopic(Topic.EXECUTION.value());
            msg.addItem("host", task.getExecutionNodeHostName());
            Messaging.send(msg);
        }
    }

    protected synchronized void notifyStatusListener(ExecutionTask task, ExecutionStatus status, String reason) {
        for (TaskStatusListener listener : statusListeners) {
            listener.taskStatusChanged(task, status, reason);
        }
    }

    protected synchronized void notifyStatusListener(ExecutionTask task) {
        for (TaskStatusListener listener : statusListeners) {
            listener.taskChanged(task);
        }
    }

    protected void sendMessage(ExecutionTask task, String message) {
        final String userId = task.getJob().getUserId();
        final Message msg = Message.create(userId,
                                           String.valueOf(task.getId()),
                                           message,
                                           false);
        Messaging.send(new UserPrincipal(userId), Topic.INFORMATION.value(), getClass().getSimpleName(), msg, false);
    }

    protected void sendProgressMessage(ExecutionTask task) {
        final String userId = task.getJob().getUserId();
        final Message message = Message.create(userId,
                                               String.valueOf(task.getId()),
                                               ExecutionStatus.RUNNING.name(),
                                               false);
        try {
            message.setData(JsonMapper.instance().writeValueAsString(taskProvider.getTaskStatus(task.getId())));
            Messaging.send(new UserPrincipal(userId), Topic.EXECUTION.value(), getClass().getSimpleName(), message, false);
        } catch (JsonProcessingException e) {
            logger.warning("Cannot send progress message. Reason: " + e.getMessage());
        }
    }

    protected void sendProgressMessage(ExecutionTask task, double percent) {
        final String userId = task.getJob().getUserId();
        final Message message = Message.create(userId,
                                               String.valueOf(task.getId()),
                                               ExecutionStatus.RUNNING.name(),
                                               false);
        try {
            final ExecutionTaskSummary taskStatus = taskProvider.getTaskStatus(task.getId());
            taskStatus.setPercentComplete(percent);
            message.setData(JsonMapper.instance().writeValueAsString(taskStatus));
            Messaging.send(new UserPrincipal(userId), Topic.EXECUTION.value(), getClass().getSimpleName(), message, false);
        } catch (JsonProcessingException e) {
            logger.warning("Cannot send progress message. Reason: " + e.getMessage());
        }
    }

    private void decrement(String userId, int cpu, long memory) {
        synchronized (resourcesUsage) {
            final Tuple<Integer, Long> tuple = resourcesUsage.get(userId);
            if (tuple != null) {
                resourcesUsage.put(userId,
                                   new Tuple<>(Math.max(0, tuple.getKeyOne() - cpu),
                                               Math.max(0, tuple.getKeyTwo() - memory)));
            }
        }
    }

    private void increment(String userId, int cpu, long memory) {
        synchronized (resourcesUsage) {
            final Tuple<Integer, Long> tuple = resourcesUsage.get(userId);
            if (tuple == null) {
                resourcesUsage.put(userId, new Tuple<>(cpu, memory));
            } else {
                resourcesUsage.put(userId,
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
