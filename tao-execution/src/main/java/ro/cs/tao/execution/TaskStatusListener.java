package ro.cs.tao.execution;

import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;

/**
 * The TaskStatusListener interface is used to listen for changes in the status
 * of execution tasks and react to task or task status updates.
 * Implementations of this interface provide custom handling for these events,
 * typically to update relevant data stores or manage resources associated
 * with the tasks.
 */
public interface TaskStatusListener {
    /**
     * Notifies that the status of an execution task has changed.
     *
     * @param task   The execution task whose status has changed.
     * @param status The new status of the execution task.
     * @param reason The reason for the status change, providing additional context.
     */
    void taskStatusChanged(ExecutionTask task, ExecutionStatus status, String reason);

    /**
     * Notifies that there has been a change related to an execution task.
     * This method acts as a generic handler for task changes, allowing implementations
     * to define custom responses to a task's modification or update events.
     *
     * @param task The execution task that has undergone a change.
     */
    void taskChanged(ExecutionTask task);
}
