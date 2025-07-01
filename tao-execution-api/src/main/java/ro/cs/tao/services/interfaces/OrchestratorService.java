/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.services.interfaces;

import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.beans.Parameter;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.callback.EndpointDescriptor;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.persistence.PersistenceException;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Interface for orchestration
 *
 * @author Cosmin Cara
 */
public interface OrchestratorService extends TAOService {

    /**
     * Returns all the settable parameters of the components of a workflow.
     * The parameters are grouped by the component identifier
     * @param workflowId    The workflow identifier
     */
    Map<String, List<Parameter>> getWorkflowParameters(long workflowId) throws PersistenceException;

    /**
     * Returns the outputs of the executions of a workflow for a specific user.
     * @param workflowId    The workflow identifier
     * @param userId        The user identifier
     */
    List<ExecutionTaskOutput> getWorkflowOutputs(long workflowId, String userId) throws PersistenceException;
    /**
     * Returns the output descriptors of the executions of a workflow.
     * @param workflowId    The workflow identifier
     */
    List<TargetDescriptor> getWorkflowOutputDescriptors(long workflowId) throws PersistenceException;
    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param request   The execution request, containing the workflow identifier, input parameters and
     *                  a name/label for the execution instance
     *
     * @throws ExecutionException   In case anything goes wrong or a job for this workflow was already created
     */
    long startWorkflow(ExecutionRequest request) throws ExecutionException;

    /**
     * Creates a job for a workflow definition without starting its execution.
     *
     * @param request       The execution request, containing:
     *  - The workflow identifier
     *  - A friendly name for this execution
     *  - The format in which the workflow will be exported
     *  - The overridden parameter values for components
     *
     * @throws ExecutionException   In case anything goes wrong or a job for this workflow was already created
     */
    //ExecutionJob createJobExternal(long workflowId, String jobName, Map<String, Map<String, String>> inputs) throws ExecutionException;
    ExecutionJob scriptJob(ExecutionRequest request) throws ExecutionException;

    /**
     * Stops the execution of the given job.
     *
     * @param jobId    The job identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job
     */
    void stopJob(long jobId) throws ExecutionException;
    /**
     * Pauses (suspends) the execution of the given job.
     *
     * @param jobId    The job identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job
     */
    void pauseJob(long jobId) throws ExecutionException;
    /**
     * Resumes the execution of the given job.
     *
     * @param jobId    The job identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job
     */
    void resumeJob(long jobId) throws ExecutionException;
    /**
     * Removes all pending jobs for a user.
     * @param userId  The user for which to remove the jobs
     * @return      The number of purged jobs
     */
    int purgeJobs(String userId) throws ExecutionException;

    /**
     * Returns (from the database) the list of tasks that are marked as being executed.
     */
    List<ExecutionTaskSummary> getRunningTasks(String userId);
    /**
     * Returns (from the database) the status of the tasks of the given job.
     */
    List<ExecutionTaskSummary> getTasksStatus(long jobId);

    /**
     * Retrieves a list of all currently running jobs for a specific user.
     *
     * @param userId The identifier of the user for whom the running jobs are to be retrieved.
     * @return A list of ExecutionJobSummary objects representing the running jobs for the specified user.
     */
    List<ExecutionJobSummary> getRunningJobs(String userId);

    /**
     * Retrieves detailed information about a specific job identified by the given job ID.
     *
     * @param jobId The identifier of the job whose details are to be retrieved.
     * @return An ExecutionJobSummary object containing the job details, such as status,
     *         name, workflow name, start and end times, and associated task summaries.
     */
    ExecutionJobSummary getJobInfo(long jobId);

    /**
     * Retrieves a list of all completed jobs for a specific user.
     *
     * @param userId The identifier of the user for whom the completed jobs are to be retrieved.
     * @return A list of ExecutionJobSummary objects representing the completed jobs for the specified user.
     */
    List<ExecutionJobSummary> getCompletedJobs(String userId);

    /**
     * Retrieves a queue of all currently queued execution jobs.
     *
     * @return a queue containing {@code ExecutionJobSummary} objects that represent the queued jobs.
     */
    Queue<ExecutionJobSummary> getQueuedJobs();

    /**
     * Retrieves a queue of all currently queued execution jobs for a specific user.
     *
     * @param userId The identifier of the user for whom the queued jobs are to be retrieved.
     * @return A queue containing {@code ExecutionJobSummary} objects that represent the queued jobs for the specified user.
     */
    Queue<ExecutionJobSummary> getQueuedJobs(String userId);

    /**
     * Retrieves a mapping of user identifiers to their respective queues of currently queued execution jobs.
     *
     * @return A map where each key is a user identifier (as a String) and the corresponding value
     *         is a queue of {@code ExecutionJobSummary} objects representing the queued jobs for that user.
     */
    Map<String, Queue<ExecutionJobSummary>> getQueuedUserJobs();

    /**
     * Moves the specified job up in the execution queue for the user.
     *
     * @param jobId The identifier of the job to be moved up in the queue.
     * @param userId The identifier of the user associated with the job.
     */
    void moveJobUp(long jobId, String userId);

    /**
     * Moves the specified job down in the execution queue for the user.
     *
     * @param jobId The identifier of the job to be moved down in the queue.
     * @param userId The identifier of the user associated with the job.
     */
    void moveJobDown(long jobId, String userId);

    /**
     * Removes a specific job from the execution queue for the given user.
     *
     * @param userId The identifier of the user associated with the job to be removed.
     * @param jobId  The identifier of the job to be removed from the queue.
     * @throws PersistenceException If there is an issue accessing or modifying the persistence layer.
     */
    void deleteJobFromQueue(String userId, long jobId) throws PersistenceException;

    /**
     * Clears the job history for a specified user.
     *
     * @param userId The identifier of the user whose job history will be cleared.
     * @throws PersistenceException If there is an issue accessing or modifying the persistence layer.
     */
    void clearJobHistory(String userId) throws PersistenceException;

    /**
     * Initiates the execution of an external workflow using the provided parameters.
     *
     * @param jsonGraph The JSON representation of the workflow graph to be executed.
     * @param container The identifier of the container where the workflow will be executed.
     * @param callback The endpoint descriptor for receiving callbacks or notifications related to the workflow execution.
     * @return The unique identifier of the newly started workflow instance.
     * @throws ExecutionException If an error occurs during the execution of the workflow.
     */
    long startExternalWorkflow(String jsonGraph, String container, EndpointDescriptor callback) throws ExecutionException;

    /**
     * Invokes a WPS (Web Processing Service) operation identified by the given identifier and using
     * the specified input parameters.
     *
     * @param identifier The unique identifier of the WPS operation to be invoked.
     * @param inputs A map containing the input parameters for the WPS operation. The map's keys are
     *               parameter names, and the values are nested maps where each key-value pair
     *               represents specific parameter attributes or details.
     * @return The unique identifier of the execution result for the invoked WPS operation.
     * @throws ExecutionException If an error occurs during the invocation of the WPS operation.
     */
    long invokeWPS(String identifier, Map<String, Map<String, String>> inputs) throws ExecutionException;

    /**
     * Invokes a WMS (Web Map Service) operation identified by the given identifier and using
     * the specified input parameters.
     *
     * @param identifier The unique identifier of the WMS operation to be invoked.
     * @param inputs A map containing the input parameters for the WMS operation. The map's keys are
     *               parameter names, and the values are their corresponding input values.
     * @return The unique identifier of the execution result for the invoked WMS operation.
     * @throws ExecutionException If an error occurs during the invocation of the WMS operation.
     */
    long invokeWMS(String identifier, Map<String, String> inputs) throws ExecutionException;
}
