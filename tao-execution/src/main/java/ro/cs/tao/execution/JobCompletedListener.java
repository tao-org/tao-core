package ro.cs.tao.execution;

import ro.cs.tao.execution.model.ExecutionJob;

/**
 * Interface for listening to the completion of an execution job.
 *
 * Classes implementing this interface receive notifications when an
 * execution job has been completed. This can be used for performing
 * post-completion tasks such as cleanup, logging, or managing next actions.
 */
public interface JobCompletedListener {
    /**
     * Invoked when an execution job has been completed.
     *
     * This method is responsible for handling any operations that need
     * to occur once the provided {@link ExecutionJob} has reached its
     * finished state. Implementations may include tasks such as logging,
     * resource cleanup, or initiating subsequent steps in a workflow.
     *
     * @param job the completed execution job containing details about
     *            the execution, its status, and other associated metadata.
     */
    void onCompleted(ExecutionJob job);
}
