package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.time.LocalDateTime;

public class DoneStatusHandler extends TaskStatusHandler {
    protected DoneStatusHandler(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        if (job.orderedTasks().stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
            job.setExecutionStatus(ExecutionStatus.DONE);
            job.setEndTime(LocalDateTime.now());
            persistenceManager.updateExecutionJob(job);
        }
    }
}
