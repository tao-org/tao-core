package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.orchestration.util.TaskUtilities;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.util.List;
import java.util.stream.Collectors;

public class FailedStatusHandler extends TaskStatusHandler {
    protected FailedStatusHandler(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        if (TransitionBehavior.FAIL_ON_ERROR.equals(TaskUtilities.getTransitionBehavior(task))) {
            ExecutionJob job = task.getJob();
            ExecutionStatus taskStatus = task.getExecutionStatus();
            List<ExecutionTask> tasks = job.orderedTasks().stream()
                    .filter(t -> TaskCommand.STOP.getAllowedStates().contains(t.getExecutionStatus()))
                    .collect(Collectors.toList());
            stopTasks(tasks);
            bulkSetStatus(tasks, taskStatus);
            job.setExecutionStatus(taskStatus);
            persistenceManager.updateExecutionJob(job);
        }
    }
}
