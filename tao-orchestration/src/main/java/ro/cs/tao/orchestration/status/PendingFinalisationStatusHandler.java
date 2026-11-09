package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.PersistenceException;

/**
 * Handles tasks in the PENDING_FINALISATION state and transitions the associated job to
 * the RUNNING state, if specific conditions are met. This handler operates under the assumption
 * that the handling of tasks in this state indicates the preparation for or continuation of execution.
 *
 * It ensures that if the associated job is not already in a RUNNING state and none of its ordered tasks
 * are in a FAILED state, the job's execution status is transitioned to RUNNING and persisted.
 *
 * This handler is registered for the PENDING_FINALISATION status via the {@code TaskStatusHandler}
 * registry mechanism.
 */
public class PendingFinalisationStatusHandler extends TaskStatusHandler {
    protected PendingFinalisationStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        super(jobProvider, taskProvider);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        if (job.getExecutionStatus() != ExecutionStatus.RUNNING &&
                job.orderedTasks().stream().noneMatch(t -> t.getExecutionStatus() == ExecutionStatus.FAILED)) {
            job.setExecutionStatus(ExecutionStatus.RUNNING);
            jobProvider.update(job);
        }
    }
}