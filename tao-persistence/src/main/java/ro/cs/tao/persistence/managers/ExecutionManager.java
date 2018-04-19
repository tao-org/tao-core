/*
 * Copyright (C) 2017 CS ROMANIA
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

package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.ExecutionJobRepository;
import ro.cs.tao.persistence.repository.ExecutionTaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("executionManager")
public class ExecutionManager {

    private Logger logger = Logger.getLogger(ExecutionManager.class.getName());

    /** CRUD Repository for ExecutionJob entities */
    @Autowired
    private ExecutionJobRepository executionJobRepository;

    /** CRUD Repository for ExecutionTask entities */
    @Autowired
    private ExecutionTaskRepository executionTaskRepository;

    //region ExecutionJob
    @Transactional(readOnly = true)
    public List<ExecutionJob> getAllJobs() {
        // retrieve jobs and filter them
        return new ArrayList<>(((List<ExecutionJob>)
                executionJobRepository.findAll(new Sort(Sort.Direction.ASC, Constants.JOB_IDENTIFIER_PROPERTY_NAME))));
    }

    @Transactional(readOnly = true)
    public List<ExecutionJob> getJobs(long workflowId) {
        return executionJobRepository.findByWorkflowId(workflowId);
    }

    @Transactional(readOnly = true)
    public ExecutionJob getJobById(long jobId) {
        return executionJobRepository.findById(jobId);
    }

    @Transactional(readOnly = true)
    public List<ExecutionJob> getJobs(ExecutionStatus status) {
        return executionJobRepository.findByExecutionStatus(status);
    }

    @Transactional
    public ExecutionJob saveExecutionJob(ExecutionJob job) throws PersistenceException {
        // check method parameters
        if(!checkExecutionJob(job, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution job !");
        }

        // save the new ExecutionJob entity and return it
        return executionJobRepository.save(job);
    }

    @Transactional
    public ExecutionJob updateExecutionJob(ExecutionJob job) throws PersistenceException {
        // check method parameters
        if(!checkExecutionJob(job, true)) {
            throw new PersistenceException("Invalid parameters were provided for updating the execution job " + (job != null && job.getId() != 0 ? "(identifier " + job.getId() + ")" : "") + "!");
        }

        // check if there is such job (to update) with the given identifier
        final ExecutionJob existingJob = executionJobRepository.findById(job.getId());
        if (existingJob == null) {
            throw new PersistenceException("There is no execution job with the given identifier: " + job.getId());
        }

        // save the updated entity
        return executionJobRepository.save(job);
    }
    //endregion

    //region ExecutionTask
    @Transactional
    public List<ExecutionTask> getRunningTasks() {
        // retrieve tasks and filter them
        return ((List<ExecutionTask>)
                executionTaskRepository.findAll(new Sort(Sort.Direction.ASC,
                                                         Constants.TASK_IDENTIFIER_PROPERTY_NAME)))
                .stream()
                .filter(t -> (t.getExecutionStatus() == ExecutionStatus.RUNNING ||
                              t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExecutionTask getTaskById(Long id) throws PersistenceException {
        final ExecutionTask existingTask = executionTaskRepository.findById(id);
        if (existingTask == null) {
            throw new PersistenceException("There is no execution task with the given identifier: " + id);
        }
        return existingTask;
    }

    @Transactional(readOnly = true)
    public ExecutionTask getTaskByJobAndNode(long jobId, long nodeId) {
        return executionTaskRepository.findByJobAndWorkflowNode(jobId, nodeId);
    }

    @Transactional(readOnly = true)
    public ExecutionTask getTaskByGroupAndNode(long groupId, long nodeId) {
        return executionTaskRepository.findByGroupAndWorkflowNode(groupId, nodeId);
    }

    @Transactional(readOnly = true)
    public ExecutionTask getTaskByResourceId(String id) throws PersistenceException {
        final ExecutionTask existingTask = executionTaskRepository.findByResourceId(id);
        if (existingTask == null) {
            throw new PersistenceException("There is no execution task with the given resource identifier: " + id);
        }
        return existingTask;
    }

    /**
     * Saves a task directly attached to an existent job
     *
     * @param task - the task to save
     * @param job - the existent job
     * @return - the newly saved task
     * @throws PersistenceException
     */
    @Transactional
    public ExecutionTask saveExecutionTask(ExecutionTask task, ExecutionJob job) throws PersistenceException {

        logger.fine(String.format("saveExecutionTask() of type %s having resource id: %s",
                                   task.getClass().getCanonicalName(), task.getResourceId()));

        // check method parameters
        if (!checkExecutionTask(task, job, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution task !");
        }

        // check if there is already task with the same resource identifier
        if (task.getResourceId() != null) {
            final ExecutionTask taskWithSameResourceId = executionTaskRepository.findByResourceId(task.getResourceId());
            if (taskWithSameResourceId != null) {
                throw new PersistenceException("There is already another task with the resource identifier: " + task.getResourceId());
            }
        }

        if (task instanceof ProcessingExecutionTask || task instanceof DataSourceExecutionTask ||
                task instanceof ExecutionGroup) {

            // set the task parent job
            task.setJob(job);

            // save the new ExecutionTask entity
            final ExecutionTask savedExecutionTask =  executionTaskRepository.save(task);

            // add the task to job tasks collection
            List<ExecutionTask> jobTasks = job.getTasks();
            if (jobTasks.stream().noneMatch(t -> t.getId().equals(task.getId()))) {
                jobTasks.add(task);
                job.setTasks(jobTasks);
                executionJobRepository.save(job);
            }

            return savedExecutionTask;
        }

        return null;
    }

    @Transactional
    public ExecutionTask updateExecutionTask(ExecutionTask task) throws PersistenceException {
        // check method parameters
        if(!checkExecutionTask(task, true)) {
            throw new PersistenceException(String.format("Invalid parameters for updating the execution task %s!",
                                                         (task != null && task.getId() != 0 ? task.getId() : "")));
        }

        // check if there is such task (to update) with the given identifier
        final ExecutionTask existingTask = executionTaskRepository.findById(task.getId());
        if (existingTask == null) {
            throw new PersistenceException("There is no execution task with the given identifier: " + task.getId());
        }

        // save the updated entity
        return executionTaskRepository.save(task);
    }

    @Transactional
    public ExecutionTask saveExecutionGroupSubTask(ExecutionTask task, ExecutionGroup taskGroup) throws PersistenceException {

        logger.info("saveExecutionGroupSubTask() of type " + task.getClass().getCanonicalName() + " having resource id: " + task.getResourceId());

        // check method parameters
        if (!checkExecutionGroupTask(task, taskGroup, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution task within task group " + taskGroup.getId() + "!");
        }

        // check if there is already task with the same resource identifier
        if (task.getResourceId() != null) {
            final ExecutionTask taskWithSameResourceId = executionTaskRepository.findByResourceId(task.getResourceId());
            if (taskWithSameResourceId != null) {
                throw new PersistenceException("There is already another task with the resource identifier: " + task.getResourceId());
            }
        }

        if (task instanceof ProcessingExecutionTask || task instanceof DataSourceExecutionTask) {

            // set the task parent group
            task.setGroupTask(taskGroup);

            // save the new ExecutionTask entity
            final ExecutionTask savedExecutionTask =  executionTaskRepository.save(task);

            // add the task to job tasks collection
            List<ExecutionTask> groupTasks = taskGroup.getTasks();
            if (groupTasks == null){
                groupTasks = new ArrayList<>();
            }
            if (groupTasks.stream().noneMatch(t -> t.getId().equals(task.getId()))) {
                groupTasks.add(task);
                taskGroup.setTasks(groupTasks);
                executionTaskRepository.save(taskGroup);
            }

            return savedExecutionTask;
        }

        return null;
    }

    @Transactional
    public ExecutionTask saveExecutionGroupWithSubTasks(ExecutionGroup taskGroup, ExecutionJob job) throws PersistenceException {

        // check method parameters
        if (!checkExecutionTask(taskGroup, job, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution group with sub-tasks !");
        }

        List<ExecutionTask> subTasks = taskGroup.getTasks() != null ? taskGroup.getTasks() : new ArrayList<>();

        taskGroup.setTasks(null);
        taskGroup = (ExecutionGroup)saveExecutionTask(taskGroup, job);

        for (ExecutionTask subTask : subTasks){
            saveExecutionGroupSubTask(subTask, taskGroup);
        }

        return taskGroup;
    }
    //endregion

    private boolean checkExecutionJob(ExecutionJob job, boolean existingEntity) {
        return job != null && !(existingEntity && job.getId() == 0);
    }

    private boolean checkExecutionTask(ExecutionTask task, ExecutionJob job, boolean existingEntity) {
        // check first the job (that should already be persisted)
        return !(!checkExecutionJob(job, true) || !checkIfExistsExecutionJobById(job.getId())) &&
                checkExecutionTask(task, existingEntity);
    }

    private boolean checkExecutionGroupTask(ExecutionTask task, ExecutionGroup taskGroup, boolean existingEntity) {
        // check first the task group (that should already be persisted)
        return !(!checkExecutionTask(taskGroup, true) || !checkIfExistsExecutionTaskById(taskGroup.getId())) &&
                checkExecutionTask(task, existingEntity);
    }

    private boolean checkExecutionTask(ExecutionTask task, boolean existingEntity) {
        return task != null && !(existingEntity && task.getId() == null) &&
                !(!existingEntity && task.getId() != null) &&
                !(existingEntity && (task.getResourceId() == null || task.getResourceId().isEmpty()));
    }

    @Transactional(readOnly = true)
    private boolean checkIfExistsExecutionJobById(final Long jobId) {
        boolean result = false;

        if (jobId != null && jobId > 0) {
            // try to retrieve ExecutionJob after its identifier
            final ExecutionJob jobEnt = executionJobRepository.findById(jobId);
            if (jobEnt != null) {
                result = true;
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    private boolean checkIfExistsExecutionTaskById(final Long taskId) {
        boolean result = false;

        if (taskId != null && taskId > 0) {
            // try to retrieve ExecutionTask after its identifier
            final ExecutionTask taskEnt = executionTaskRepository.findById(taskId);
            if (taskEnt != null) {
                result = true;
            }
        }

        return result;
    }
}
