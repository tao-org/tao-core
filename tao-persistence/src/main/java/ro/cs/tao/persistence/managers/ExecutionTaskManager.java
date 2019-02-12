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

package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.ExecutionTaskRepository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component("executionManager")
public class ExecutionTaskManager extends EntityManager<ExecutionTask, Long, ExecutionTaskRepository> {

    @Autowired
    private ExecutionJobManager executionJobManager;

    @Autowired
    private DataSource dataSource;

    public ExecutionTask updateStatus(ExecutionTask task, ExecutionStatus newStatus) throws PersistenceException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("UPDATE execution.task SET execution_status_id = ?, resource_id = ?, start_time = ? WHERE id = ?",
                            newStatus.value(), task.getResourceId(), task.getLastUpdated(), task.getId());
        return get(task.getId());
    }

    @Transactional
    public List<ExecutionTask> getRunningTasks() {
        return repository.getRunningTasks();
    }

    public List<ExecutionTask> getExecutingTasks() {
        return repository.getExecutingTasks();
    }

    @Transactional
    public List<ExecutionTaskSummary> getStatus(long jobId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
            PreparedStatement statement =
                    con.prepareStatement("SELECT t.id, w.name \"workflow\", CASE WHEN d.id IS NULL THEN CASE WHEN p.id IS NULL THEN 'group' ELSE p.id END ELSE d.id END \"componentName\", " +
                                                 "t.start_time, t.end_time, t.execution_node_host_name, t.execution_status_id FROM execution.task t " +
                                                 "INNER JOIN execution.job j ON j.id = t.job_id " +
                                                 "INNER JOIN workflow.graph w ON w.id = j.workflow_id " +
                                                 "LEFT OUTER JOIN component.data_source_component d ON d.id = t.component_id " +
                                                 "LEFT OUTER JOIN component.processing_component p ON p.id = t.component_id where job_id = ? " +
                                                 "ORDER BY t.start_time, t.id");
            statement.setLong(1, jobId);
            return statement;
        }, (rs, rowNum) -> {
            ExecutionTaskSummary result = new ExecutionTaskSummary();
            result.setTaskId(rs.getLong(1));
            result.setWorkflowName(rs.getString(2));
            result.setComponentName(rs.getString(3));
            Timestamp timestamp = rs.getTimestamp(4);
            if (timestamp != null) {
                result.setTaskStart(timestamp.toLocalDateTime());
            }
            timestamp = rs.getTimestamp(5);
            if (timestamp != null) {
                result.setTaskEnd(timestamp.toLocalDateTime());
            }
            result.setHost(rs.getString(6));
            result.setTaskStatus(EnumUtils.getEnumConstantByValue(ExecutionStatus.class, rs.getInt(7)));
            return result;
        });
    }

    @Transactional
    public ExecutionTask getTaskByJobAndNode(long jobId, long nodeId, int instanceId) {
        return repository.findByJobAndWorkflowNode(jobId, nodeId, instanceId);
    }

    @Transactional
    public ExecutionTask getTaskByGroupAndNode(long groupId, long nodeId, int instanceId) {
        return repository.findByGroupAndWorkflowNode(groupId, nodeId, instanceId);
    }

    @Transactional
    public ExecutionTask getTaskByResourceId(String id) throws PersistenceException {
        final ExecutionTask existingTask = repository.findByResourceId(id);
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
    public ExecutionTask save(ExecutionTask task, ExecutionJob job) throws PersistenceException {
        // check method parameters
        if (!checkExecutionTask(task, job, task.getId() != null)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution task !");
        }

        // check if there is already task with the same resource identifier
        if (task.getResourceId() != null) {
            final ExecutionTask taskWithSameResourceId = repository.findByResourceId(task.getResourceId());
            if (taskWithSameResourceId != null) {
                throw new PersistenceException("There is already another task with the resource identifier: " + task.getResourceId());
            }
        }

        if (task instanceof ProcessingExecutionTask || task instanceof DataSourceExecutionTask ||
                task instanceof ExecutionGroup) {

            // set the task parent job
            task.setJob(job);

            // save the new ExecutionTask entity
            final ExecutionTask savedExecutionTask =  repository.save(task);

            // add the task to job tasks collection
            List<ExecutionTask> jobTasks = job.orderedTasks();
            if (jobTasks.stream().noneMatch(t -> t.getId().equals(task.getId()))) {
                jobTasks.add(task);
                job.setTasks(jobTasks);
                executionJobManager.update(job);
            }

            return savedExecutionTask;
        }

        return null;
    }

    @Transactional
    public ExecutionTask saveExecutionGroupSubTask(ExecutionTask task, ExecutionGroup taskGroup) throws PersistenceException {

        // check method parameters
        if (!checkExecutionGroupTask(task, taskGroup, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution task within task group " + taskGroup.getId() + "!");
        }

        // check if there is already task with the same resource identifier
        if (task.getResourceId() != null) {
            final ExecutionTask taskWithSameResourceId = repository.findByResourceId(task.getResourceId());
            if (taskWithSameResourceId != null) {
                throw new PersistenceException("There is already another task with the resource identifier: " + task.getResourceId());
            }
        }

        if (task instanceof ProcessingExecutionTask || task instanceof DataSourceExecutionTask) {

            // set the task parent group
            task.setGroupTask(taskGroup);

            // save the new ExecutionTask entity
            final ExecutionTask savedExecutionTask =  repository.save(task);

            // add the task to job tasks collection
            List<ExecutionTask> groupTasks = taskGroup.getTasks();
            if (groupTasks == null){
                groupTasks = new ArrayList<>();
            }
            if (groupTasks.stream().noneMatch(t -> t.getId().equals(task.getId()))) {
                groupTasks.add(task);
                taskGroup.setTasks(groupTasks);
                repository.save(taskGroup);
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
        taskGroup = (ExecutionGroup) save(taskGroup, job);

        for (ExecutionTask subTask : subTasks){
            saveExecutionGroupSubTask(subTask, taskGroup);
        }

        return taskGroup;
    }
    //endregion


    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return (!existingEntity && (entityId == null || entityId == 0)) ||
                (existingEntity && entityId != null && entityId > 0);
    }

    @Override
    protected boolean checkEntity(ExecutionTask entity) {
        return !(entity.getId() != null && entity.getId() > 0 && (entity.getResourceId() == null || entity.getResourceId().isEmpty()));
    }

    private boolean checkExecutionJob(ExecutionJob job, boolean existingEntity) {
        return job != null && !(existingEntity && job.getId() == 0);
    }

    private boolean checkExecutionTask(ExecutionTask task, ExecutionJob job, boolean existingEntity) {
        // check first the job (that should already be persisted)
        return !(!checkExecutionJob(job, true) || !existsJob(job.getId())) &&
                checkEntity(task, existingEntity);
    }

    private boolean checkExecutionGroupTask(ExecutionTask task, ExecutionGroup taskGroup, boolean existingEntity) {
        // check first the task group (that should already be persisted)
        return !(!checkEntity(taskGroup, true) || !exists(taskGroup.getId())) &&
                checkEntity(task, existingEntity);
    }

    @Transactional
    private boolean existsJob(final Long jobId) {
        return jobId != null && jobId > 0 && executionJobManager.exists(jobId);
    }
}
