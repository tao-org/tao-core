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
 * Handles tasks and job state transitions when their status is set to "SUSPENDED".
 * This class implements the behavior required to manage and persist changes
 * for suspended tasks and their associated job.
 */
public class SuspendedStatusHandler extends TaskStatusHandler {

    protected SuspendedStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        super(jobProvider, taskProvider);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        ExecutionStatus taskStatus = task.getExecutionStatus();
        List<ExecutionTask> tasks = job.orderedTasks().stream()
                .filter(t -> TaskCommand.STOP.allowedStates().contains(t.getExecutionStatus()))
                .collect(Collectors.toList());
        stopTasks(tasks);
        bulkSetStatus(tasks, taskStatus);
        job.setExecutionStatus(taskStatus);
        jobProvider.update(job);
    }
}
