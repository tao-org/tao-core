package ro.cs.tao.execution.model;

/**
 * Callback interface for implementors that wish to be notified about execution task updates.
 *
 * @author  Cosmin Cara
 * @since   1.1.0
 */
public interface TaskListener {
    /**
     * Checks if this listener can call back using the given protocol.
     * There can be only one listener per protocol.
     */
    boolean supportsProtocol(String protocol);
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
