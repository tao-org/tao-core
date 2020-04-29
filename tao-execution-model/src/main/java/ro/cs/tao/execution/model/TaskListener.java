package ro.cs.tao.execution.model;

import ro.cs.tao.execution.callback.EndpointDescriptor;

/**
 * Callback interface for implementors that wish to be notified about execution task updates.
 *
 * @author  Cosmin Cara
 * @since   1.1.0
 */
public interface TaskListener {
    /**
     * Sets the connection information descriptor for the listener.
     * If the descriptor is not set, subsequent calls of other methods of this listener will fail.
     *
     * @param descriptor    The descriptor encapsulating the connection information
     */
    void setDescriptor(EndpointDescriptor descriptor);

    /**
     * Returns the connection information descriptor associated with this listener.
     */
    EndpointDescriptor getDescriptor();

    /**
     * Called after an execution task has transited to RUNNING state.
     * @param task  The TAO execution task
     */
    void onStarted(ProcessingExecutionTask task);
    /**
     * Called when an execution task has changed progress.
     * @param task  The TAO execution task
     */
    void onUpdated(ProcessingExecutionTask task);
    /**
     * Called after an execution task has completed successfully.
     * @param task  The TAO execution task
     * @param processOutput The console output of the task
     */
    void onCompleted(ProcessingExecutionTask task, String processOutput);
    /**
     * Called after an execution task has failed.
     * @param task  The TAO execution task
     * @param reason The failure message
     * @param errorCode The exit code of the process executed by the task
     * @param processOutput The console output of the task
     */
    void onError(ProcessingExecutionTask task, String reason, int errorCode, String processOutput);
}
