package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.PersistenceException;

import java.time.LocalDateTime;

public class DoneStatusHandler extends TaskStatusHandler {
    protected DoneStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        super(jobProvider, taskProvider);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        if (job.orderedTasks().stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
            job.setExecutionStatus(ExecutionStatus.DONE);
            job.setEndTime(LocalDateTime.now());
            jobProvider.update(job);
        }
    }
}
