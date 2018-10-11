package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;

public class RunningStatusHandler extends TaskStatusHandler {
    protected RunningStatusHandler(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        ExecutionStatus jobStatus = job.getExecutionStatus();
        if (jobStatus == ExecutionStatus.QUEUED_ACTIVE || jobStatus == ExecutionStatus.UNDETERMINED) {
            job.setExecutionStatus(ExecutionStatus.RUNNING);
            persistenceManager.updateExecutionJob(job);
        }
    }
}
