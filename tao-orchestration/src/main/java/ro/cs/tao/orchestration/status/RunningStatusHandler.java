package ro.cs.tao.orchestration.status;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.PersistenceException;

/**
 * Handles the transition of the execution status for tasks that are moving
 * into the RUNNING state.
 *
 * This class is responsible for processing tasks whose associated jobs are in
 * the QUEUED_ACTIVE or UNDETERMINED execution statuses and updating the job
 * status to RUNNING if applicable. It overrides the task handling behavior
 * defined in the {@link TaskStatusHandler} base class.
 *
 * The class works in conjunction with the {@link ExecutionJobProvider} and
 * {@link ExecutionTaskProvider} for persisting changes to jobs and tasks, as
 * well as managing their execution lifecycle.
 *
 * Constructor:
 * - The constructor initializes the handler with references to the
 *   {@link ExecutionJobProvider} and {@link ExecutionTaskProvider}, which are
 *   used to manage job and task persistence and updates.
 *
 * Method:
 * - The {@link #handleTask(ExecutionTask, String)} method processes the provided
 *   task and updates the associated job's status to RUNNING if the job is in
 *   QUEUED_ACTIVE or UNDETERMINED state.
 *
 * Exceptions:
 * - A {@link PersistenceException} may be thrown during task or job
 *   persistence operations.
 */
public class RunningStatusHandler extends TaskStatusHandler {
    protected RunningStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        super(jobProvider, taskProvider);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        ExecutionStatus jobStatus = job.getExecutionStatus();
        if (jobStatus == ExecutionStatus.QUEUED_ACTIVE || jobStatus == ExecutionStatus.UNDETERMINED) {
            job.setExecutionStatus(ExecutionStatus.RUNNING);
            jobProvider.update(job);
        }
    }
}
