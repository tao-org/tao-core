package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.persistence.PersistenceException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class TaskStatusHandler {

    private static Map<ExecutionStatus, TaskStatusHandler> handlers;
    protected final Logger logger;
    protected final ExecutionJobProvider jobProvider;
    protected final ExecutionTaskProvider taskProvider;

    public static void registerHandlers(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        if (handlers == null) {
            handlers = new HashMap<>();
        }
        handlers.put(ExecutionStatus.UNDETERMINED, new IgnoredStatusHandler(jobProvider, taskProvider));
        handlers.put(ExecutionStatus.QUEUED_ACTIVE, new IgnoredStatusHandler(jobProvider, taskProvider));
        handlers.put(ExecutionStatus.RUNNING, new RunningStatusHandler(jobProvider, taskProvider));
        handlers.put(ExecutionStatus.PENDING_FINALISATION, new PendingFinalisationStatusHandler(jobProvider, taskProvider));
        handlers.put(ExecutionStatus.DONE, new DoneStatusHandler(jobProvider, taskProvider));
        handlers.put(ExecutionStatus.FAILED, new FailedStatusHandler(jobProvider, taskProvider));
        handlers.put(ExecutionStatus.SUSPENDED, new SuspendedStatusHandler(jobProvider, taskProvider));
        handlers.put(ExecutionStatus.CANCELLED, new CancelledStatusHandler(jobProvider, taskProvider));
    }

    protected TaskStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        this.jobProvider = jobProvider;
        this.taskProvider = taskProvider;
        this.logger = Logger.getLogger(getClass().getName());
    }

    public static void handle(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionStatus status = task.getExecutionStatus();
        TaskStatusHandler handler = handlers.get(status);
        if (handler == null) {
            throw new ExecutionException(String.format("No handler registered for state %s", status.name()));
        }
        handler.handleTask(task, reason);
    }

    protected abstract void handleTask(ExecutionTask task, String reason) throws PersistenceException;

    protected void handleTask(ExecutionTask task) throws PersistenceException {
        handleTask(task, null);
    }

    protected void bulkSetStatus(Collection<ExecutionTask> tasks, ExecutionStatus status) {
        if (tasks == null) {
            return;
        }
        tasks.forEach(t -> {
            t.setExecutionStatus(status);
            try {
                taskProvider.updateStatus(t, status, "Bulk set");
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
                logger.severe(ex.getMessage());
            }
        }
    }
}
