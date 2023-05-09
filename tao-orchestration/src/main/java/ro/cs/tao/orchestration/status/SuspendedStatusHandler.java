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

public class SuspendedStatusHandler extends TaskStatusHandler {

    protected SuspendedStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
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
