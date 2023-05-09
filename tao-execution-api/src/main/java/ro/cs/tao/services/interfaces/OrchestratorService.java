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
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionJobSummary;
import ro.cs.tao.execution.model.ExecutionRequest;
import ro.cs.tao.execution.model.ExecutionTaskSummary;
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
     * Returns the outputs (i.e. the target descriptors of terminal nodes) of a workflow.
     * @param workflowId    The workflow identifier
     */
    List<TargetDescriptor> getWorkflowOutputs(long workflowId) throws PersistenceException;
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
     * @param user  The user for which to remove the jobs
     * @return      The number of purged jobs
     */
    int purgeJobs(String user) throws ExecutionException;

    /**
     * Returns (from the database) the list of tasks that are marked as being executed.
     */
    List<ExecutionTaskSummary> getRunningTasks(String userName);
    /**
     * Returns (from the database) the status of the tasks of the given job.
     */
    List<ExecutionTaskSummary> getTasksStatus(long jobId);

    List<ExecutionJobSummary> getRunningJobs(String userName);

    List<ExecutionJobSummary> getCompletedJobs(String userName);

    Queue<ExecutionJobSummary> getQueuedJobs();

    Queue<ExecutionJobSummary> getQueuedJobs(String userName);

    Map<String, Queue<ExecutionJobSummary>> getQueuedUserJobs();

    void moveJobUp(long jobId, String userName);

    void moveJobDown(long jobId, String userName);

    void deleteJobFromQueue(String userName, long jobId) throws PersistenceException;

    void clearJobHistory(String userName) throws PersistenceException;

    long startExternalWorkflow(String jsonGraph, String container, EndpointDescriptor callback) throws ExecutionException;

    long invokeWPS(String identifier, Map<String, Map<String, String>> inputs) throws ExecutionException;
}
