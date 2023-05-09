package ro.cs.tao.execution.model;

import ro.cs.tao.execution.callback.EndpointDescriptor;

import java.util.logging.Logger;

public abstract class AbstractTaskListener implements TaskListener {

    protected final Logger logger;

    public AbstractTaskListener() {
        this.logger = Logger.getLogger(getClass().getName());
    }

    protected EndpointDescriptor getCallbackFor(ProcessingExecutionTask task) {
        final ExecutionJob job = task.getJob();
        final EndpointDescriptor callbackDescriptor = job.getCallbackDescriptor();
        if (callbackDescriptor == null) {
            throw new RuntimeException(String.format("Job %d doesn't contain a callback descriptor", job.getId()));
        }
        return callbackDescriptor;
    }
}
