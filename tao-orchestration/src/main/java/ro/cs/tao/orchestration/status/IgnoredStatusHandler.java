package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.PersistenceException;

/**
 * A concrete implementation of the {@link TaskStatusHandler} that represents a
 * no-operation handler for specific task statuses. This handler is designed to
 * ignore tasks assigned to it and perform no actions when processing such tasks.
 *
 * It is typically used for task statuses where no specific handling logic is required.
 */
public class IgnoredStatusHandler extends TaskStatusHandler {
    protected IgnoredStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        super(jobProvider, taskProvider);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        // do nothing
    }

}
