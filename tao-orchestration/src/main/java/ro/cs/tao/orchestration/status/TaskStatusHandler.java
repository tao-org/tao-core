package ro.cs.tao.orchestration.status;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class TaskStatusHandler {

    private static Map<ExecutionStatus, TaskStatusHandler> handlers;
    protected final Logger logger;
    protected final PersistenceManager persistenceManager;

    public static void registerHandlers(PersistenceManager persistenceManager) {
        if (handlers == null) {
            handlers = new HashMap<>();
        }
        handlers.put(ExecutionStatus.UNDETERMINED, new IgnoredStatusHandler(persistenceManager));
        handlers.put(ExecutionStatus.QUEUED_ACTIVE, new IgnoredStatusHandler(persistenceManager));
        handlers.put(ExecutionStatus.RUNNING, new RunningStatusHandler(persistenceManager));
        handlers.put(ExecutionStatus.DONE, new DoneStatusHandler(persistenceManager));
        handlers.put(ExecutionStatus.FAILED, new FailedStatusHandler(persistenceManager));
        handlers.put(ExecutionStatus.SUSPENDED, new SuspendedStatusHandler(persistenceManager));
        handlers.put(ExecutionStatus.CANCELLED, new CancelledStatusHandler(persistenceManager));
    }

    protected TaskStatusHandler(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.logger = Logger.getLogger(getClass().getName());
    }

    public static void handle(ExecutionTask task) throws PersistenceException {
        ExecutionStatus status = task.getExecutionStatus();
        TaskStatusHandler handler = handlers.get(status);
        if (handler == null) {
            throw new ExecutionException(String.format("No handler registered for state %s", status.name()));
        }
        handler.handleTask(task);
    }

    protected abstract void handleTask(ExecutionTask task) throws PersistenceException;

    protected void bulkSetStatus(Collection<ExecutionTask> tasks, ExecutionStatus status) {
        if (tasks == null) {
            return;
        }
        tasks.forEach(t -> {
            t.setExecutionStatus(status);
            try {
                persistenceManager.updateTaskStatus(t, status);
                logger.fine(String.format("Task %s was put into status %s", t.getId(), status));
            } catch (PersistenceException e) {
                logger.severe(e.getMessage());
            }
        });
    }

    protected void stopTasks(List<ExecutionTask> tasks) {
        for (ExecutionTask task : tasks) {
            try {
                TaskCommand.STOP.applyTo(task);
            } catch (ExecutionException ex) {
                logger.severe(ExceptionUtils.getStackTrace(ex));
            }
        }
    }
}
