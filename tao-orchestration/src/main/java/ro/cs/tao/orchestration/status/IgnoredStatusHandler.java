package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;

public class IgnoredStatusHandler extends TaskStatusHandler {
    protected IgnoredStatusHandler(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    @Override
    protected void handleTask(ExecutionTask task) throws PersistenceException {
        // do nothing
    }

}
