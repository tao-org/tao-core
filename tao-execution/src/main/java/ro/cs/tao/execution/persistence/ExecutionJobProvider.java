package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTaskOutput;
import ro.cs.tao.persistence.EntityProvider;

import java.util.List;
import java.util.Set;

/**
 * Provides methods for managing and interacting with {@link ExecutionJob} entities.
 * It extends the {@link EntityProvider} interface to provide basic CRUD operations
 * and adds specific operations related to execution jobs and their workflow associations.
 */
public interface ExecutionJobProvider extends EntityProvider<ExecutionJob, Long> {

    /**
     * Retrieves the list of execution jobs associated with a given workflow for a specific user.
     *
     * @param workflowId the unique identifier of the workflow for which the execution jobs are to be retrieved
     * @param userId    the user identifier
     * @return a list of execution jobs associated with the specified workflow
     */
    List<ExecutionJob> listByWorkflow(long workflowId, String userId);

    /**
     * Retrieves the list of execution jobs associated with a given workflow.
     *
     * @param workflowId the unique identifier of the workflow for which the execution jobs are to be retrieved
     * @return a list of execution jobs associated with the specified workflow
     */
    List<ExecutionJob> listByWorkflow(long workflowId);

    /**
     * Retrieves a list of output keys associated with the specified workflow for a specific user.
     *
     * @param workflowId the unique identifier of the workflow whose output keys are to be retrieved
     * @param userId    the user identifier
     * @return a list of strings representing the output keys for the specified workflow
     */
    List<String> getWorkflowOutputKeys(long workflowId, String userId);

    /**
     * Retrieves the list of workflow outputs associated with the specified workflow for a specific user.
     *
     * @param workflowId the unique identifier of the workflow whose outputs are to be retrieved
     * @param userId    the user identifier
     * @return a list of {@code ExecutionTaskOutput} objects representing the outputs of the specified workflow
     */
    List<ExecutionTaskOutput> getWorkflowOutputs(long workflowId, String userId);

    /**
     * Retrieves a list of output keys associated with the specified execution job.
     *
     * @param jobId the unique identifier of the execution job whose output keys are to be retrieved
     * @return a list of strings representing the output keys for the specified execution job
     */
    List<String> getOutputKeys(long jobId);

    /**
     * Retrieves a list of execution jobs that match the specified execution status.
     *
     * @param status the execution status to filter the jobs by
     * @return a list of {@code ExecutionJob} instances that have the specified status
     */
    List<ExecutionJob> list(ExecutionStatus status);

    /**
     * Counts the number of execution jobs that match the specified execution status.
     *
     * @param status the execution status to filter the jobs by
     * @return the number of execution jobs with the specified status
     */
    int count(ExecutionStatus status);

    /**
     * Counts the number of execution jobs associated with a specific user and matching the given execution status.
     *
     * @param userId the unique identifier of the user for whom the execution jobs are to be counted
     * @param status the execution status to filter the jobs by
     * @return the number of execution jobs associated with the specified user and matching the provided status
     */
    int count(String userId, ExecutionStatus status);

    /**
     * Retrieves a list of execution jobs that match any of the specified execution statuses.
     *
     * @param statuses a set of {@code ExecutionStatus} instances used to filter the execution jobs
     * @return a list of {@code ExecutionJob} objects that match one or more of the provided statuses
     */
    List<ExecutionJob> list(Set<ExecutionStatus> statuses);

    /**
     * Retrieves a list of execution jobs associated with the specified user and matching any of the given execution statuses.
     *
     * @param userId   the unique identifier of the user for whom the execution jobs are to be retrieved
     * @param statuses a set of {@code ExecutionStatus} instances used to filter the execution jobs
     * @return a list of {@code ExecutionJob} objects associated with the specified user and matching one or more of the provided statuses
     */
    List<ExecutionJob> list(String userId, Set<ExecutionStatus> statuses);

    /**
     * Retrieves a page of execution jobs associated with the specified user and matching any of the given execution statuses,
     * ordered from the most recent one.
     *
     * @param userId   the unique identifier of the user for whom the execution jobs are to be retrieved
     * @param statuses a set of {@code ExecutionStatus} instances used to filter the execution jobs
     * @param pageNo   the page number, starting from 1
     * @param pageSize the maximum number of jobs in a page
     * @return a list of {@code ExecutionJob} objects associated with the specified user and matching one or more of the provided statuses
     */
    List<ExecutionJob> list(String userId, Set<ExecutionStatus> statuses, int pageNo, int pageSize);

    /**
     * Determines whether a batch with the specified identifier is currently running.
     *
     * @param batchId the unique identifier of the batch to be checked
     * @return {@code true} if the batch is currently running; {@code false} otherwise
     */
    boolean isBatchRunning(String batchId);

    /**
     * Retrieves a list of execution jobs associated with the specified batch IDs.
     *
     * @param batchIds a list of batch identifiers for which the execution jobs need to be retrieved
     * @return a list of {@code ExecutionJob} instances corresponding to the provided batch IDs
     */
    List<ExecutionJob> list(List<String> batchIds);

    /**
     * Retrieves the list of data source execution tasks associated with the specified job.
     *
     * @param jobId the unique identifier of the job for which the data source execution tasks are to be retrieved
     * @return a list of {@code DataSourceExecutionTask} objects associated with the specified job
     */
    List<DataSourceExecutionTask> getDatasourceTasks(long jobId);
}
