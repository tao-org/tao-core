package ro.cs.tao.execution.model;

import ro.cs.tao.execution.callback.EndpointDescriptor;

import java.util.logging.Logger;

/**
 * Abstract base class for implementing {@link TaskListener} to handle execution task updates.
 * This class provides common functionality and utilities for working with execution tasks
 * and callback descriptors.
 */
public abstract class AbstractTaskListener implements TaskListener {

    protected final Logger logger;

    public AbstractTaskListener() {
        this.logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Retrieves the callback {@link EndpointDescriptor} associated with the provided {@link ProcessingExecutionTask}.
     * This method looks up the callback descriptor from the execution job contained within the provided task.
     *
     * @param task the {@link ProcessingExecutionTask} for which the callback descriptor is to be retrieved
     * @return the {@link EndpointDescriptor} associated with the callback of the given task
     * @throws RuntimeException if the job associated with the task does not have a callback descriptor
     */
    protected EndpointDescriptor getCallbackFor(ProcessingExecutionTask task) {
        final ExecutionJob job = task.getJob();
        final EndpointDescriptor callbackDescriptor = job.getCallbackDescriptor();
        if (callbackDescriptor == null) {
            throw new RuntimeException(String.format("Job %d doesn't contain a callback descriptor", job.getId()));
        }
        return callbackDescriptor;
    }
}
