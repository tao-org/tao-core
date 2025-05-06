package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.persistence.PersistenceException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles tasks with a cancelled execution status by performing operations
 * specific to stopped and cancelled tasks. This includes stopping all
 * eligible tasks within the associated job, updating their statuses in bulk,
 * and ensuring the job's execution status reflects the "cancelled" state.
 * This is part of the task status handler hierarchy and manages the
 * propagation of the "cancelled" status through all relevant tasks of a job.
 */
public class CancelledStatusHandler extends TaskStatusHandler {

    protected CancelledStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        super(jobProvider, taskProvider);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        ExecutionStatus taskStatus = task.getExecutionStatus();
        List<ExecutionTask> tasks = job.orderedTasks().stream()
                .filter(t -> TaskCommand.STOP.getAllowedStates().contains(t.getExecutionStatus()))
                .collect(Collectors.toList());
        stopTasks(tasks);
        bulkSetStatus(tasks, taskStatus);
        job.setExecutionStatus(taskStatus);
        jobProvider.update(job);
    }
}
