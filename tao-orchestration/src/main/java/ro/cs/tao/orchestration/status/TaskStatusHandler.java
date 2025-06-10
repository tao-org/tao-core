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

/**
 * Abstract base class for handling task status transitions and associated operations.
 * This class provides a central mechanism to manage different execution statuses of tasks
 * and defines the contract for handling specific statuses via subclass implementations.
 */
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

    /**
     * Handles the execution of a task based on its current status by delegating the task
     * to the appropriate handler. If no handler is registered for the task's status,
     * an {@link ExecutionException} is thrown.
     *
     * @param task The {@link ExecutionTask} to be handled. It contains the execution status
     *             and additional information required for processing.
     * @param reason The reason for handling the task, providing context for the operation.
     * @throws PersistenceException If an error occurs during persistence operations while
     *                              handling the task.
     * @throws ExecutionException If no handler is registered for the task's current execution status.
     */
    public static void handle(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionStatus status = task.getExecutionStatus();
        TaskStatusHandler handler = handlers.get(status);
        if (handler == null) {
            throw new ExecutionException(String.format("No handler registered for state %s", status.name()));
        }
        handler.handleTask(task, reason);
    }

    /**
     * Processes the provided execution task based on the specific requirements
     * of the implementing handler.
     *
     * @param task The {@link ExecutionTask} instance to be processed. It contains
     *             all necessary details about the task, including its execution
     *             status and associated metadata.
     * @param reason A string specifying the reason for handling the task. This
     *               provides additional context or justification for the task
     *               processing operation.
     * @throws PersistenceException If an error occurs during any persistence
     *                              operation while processing the task.
     */
    protected abstract void handleTask(ExecutionTask task, String reason) throws PersistenceException;

    /**
     * Handles the execution of a task based on its current state. This method delegates
     * the execution task to a more specific handler by internally invoking another method
     * with additional parameters, if required.
     *
     * @param task The {@link ExecutionTask} instance representing the task to be processed.
     *             It contains the current execution status and relevant metadata for processing.
     * @throws PersistenceException If an error occurs during persistence operations while
     *                              handling the task.
     */
    protected void handleTask(ExecutionTask task) throws PersistenceException {
        handleTask(task, null);
    }

    /**
     * Updates the execution status of a collection of tasks to the specified status
     * and persists the change using the task provider. If any exception occurs
     * during the persistence operation, it is logged.
     *
     * @param tasks The collection of {@link ExecutionTask} instances whose status
     *              needs to be updated. If null, the method does nothing.
     * @param status The {@link ExecutionStatus} to set for each task in the collection.
     */
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

    /**
     * Stops the execution of a list of tasks by applying the STOP command to each task.
     * If an {@link ExecutionException} is encountered during the process, it logs the exception's message.
     *
     * @param tasks The list of {@link ExecutionTask} instances to be stopped. Each task in the list
     *              will have the STOP command applied to halt its execution.
     */
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
