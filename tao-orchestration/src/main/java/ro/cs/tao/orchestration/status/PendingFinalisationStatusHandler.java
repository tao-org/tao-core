package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.PersistenceException;

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