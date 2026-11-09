package ro.cs.tao.orchestration;

import ro.cs.tao.drmaa.Environment;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.JobType;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.workflow.WorkflowDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Interface for creating and managing execution jobs and tasks.
 * Defines methods for creating jobs of various types, splitting jobs, cloning jobs,
 * and managing job components.
 */
public interface JobFactory {

    /**
     * Creates an execution job for the given workflow, with the given input parameter values
     *
     * @param context   The current user session context
     * @param appId     The orchestrator identifier
     * @param jobName   The name of the new job
     * @param workflow  The workflow
     * @param inputs    The (possibly overridden) input parameter values
     * @param jobType   The output job format
     */
    ExecutionJob createJob(SessionContext context, String appId, String jobName, WorkflowDescriptor workflow,
                           Map<String, Map<String, String>> inputs, JobType jobType, Environment environment)
            throws PersistenceException;

    /**
     * Creates an execution job with a single task for a WPS remote service invocation, with the given input parameter values
     *
     * @param context   The invocation context
     * @param appId     The identifier of the Orchestrator instance
     * @param jobName   The name of the new job
     * @param inputs    The (possibly overridden) input parameter values
     */
    ExecutionJob createWPSJob(SessionContext context, String appId, String jobName, Map<String, Map<String, String>> inputs)
            throws PersistenceException;

    /**
     * Creates an execution job with a single task for a WMS remote service invocation, with the given input parameter values
     *
     * @param context   The invocation context
     * @param appId     The identifier of the Orchestrator instance
     * @param jobName   The name of the new job
     * @param inputs    The (possibly overridden) input parameter values
     */
    ExecutionJob createWMSJob(SessionContext context, String appId, String jobName, Map<String, String> inputs)
            throws PersistenceException;

    /**
     * Creates a list of execution jobs from a job based on the list of values of the entry data source task.
     * The first value is discarded, assuming it is taken by the original job.
     *
     * @param job       The job to be split
     *
     */
    List<ExecutionJob> splitJob(ExecutionJob job) throws PersistenceException, CloneNotSupportedException;

    /**
     * Creates a list of execution jobs from a job based on the list of values of the entry data source task.
     * The first value is discarded, assuming it is taken by the original job.
     *
     * @param job       The job to be split
     *
     */
    List<ExecutionJob> splitJobBeforeExecution(ExecutionJob job) throws PersistenceException, CloneNotSupportedException;

    /**
     * Remove volatile components.
     *
     * @param job the finished job
     * @throws PersistenceException if the removal fails
     */
    void removeVolatileComponents(ExecutionJob job) throws PersistenceException;

    /**
     * Creates a clone of the given execution job while assigning unique identifiers for each clone.
     *
     * @param original    The original execution job to be cloned.
     * @param cloneNumber The unique number identifying this clone.
     * @param total       The total number of clones that will be created, including this one.
     * @return A new {@code ExecutionJob} instance that is a clone of the original job.
     * @throws CloneNotSupportedException If the cloning operation is not supported for the given job.
     * @throws PersistenceException       If an error occurs during the persistence operation while cloning the job.
     */
    ExecutionJob cloneJob(ExecutionJob original, int cloneNumber, int total) throws CloneNotSupportedException, PersistenceException;

    /**
     * Creates a clone of the given execution task, assigning it to the specified cloned job.
     *
     * @param clonedJob The {@code ExecutionJob} instance to which the cloned task will belong.
     * @param original  The original {@code ExecutionTask} to be cloned.
     * @return A new {@code ExecutionTask} instance that is a clone of the original task.
     * @throws CloneNotSupportedException If the cloning operation is not supported for the given task.
     * @throws PersistenceException       If an error occurs during the persistence operation while cloning the task.
     */
    ExecutionTask cloneTask(ExecutionJob clonedJob, ExecutionTask original) throws CloneNotSupportedException, PersistenceException;
}
