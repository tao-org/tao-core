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
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.ExecutionTaskRepository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component("executionManager")
public class ExecutionTaskManager extends EntityManager<ExecutionTask, Long, ExecutionTaskRepository>
                                  implements ExecutionTaskProvider {

    @Autowired
    private ExecutionJobManager executionJobManager;
    @Autowired
    private DataSource dataSource;
    private final Logger logger = Logger.getLogger(ExecutionTaskManager.class.getName());

    private final ReentrantLock lock = new ReentrantLock();

    public ExecutionTask updateStatus(ExecutionTask task, ExecutionStatus newStatus, String reason) throws PersistenceException {
        lock.lock();
        try {
            boolean failed = newStatus == ExecutionStatus.FAILED;
            task.setLastUpdated(LocalDateTime.now());
            final ExecutionStatus oldStatus = task.getExecutionStatus();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update("UPDATE execution.task SET execution_status_id = ?, resource_id = ?, execution_log = ?, last_updated = ? WHERE id = ?",
                                newStatus.value(), task.getResourceId(), failed ? reason : null, task.getLastUpdated(), task.getId());
            logger.log(failed
                       ? Level.SEVERE
                       : newStatus == ExecutionStatus.CANCELLED
                         ? Level.WARNING
                         : Level.FINE,
                       String.format("Task %s status change [%s -> %s].", task.getId(), oldStatus, newStatus)
                               + (reason != null ? " Reason: " + reason : ""));
            task.setExecutionStatus(newStatus);
            task.setLog(reason);
            return task;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ExecutionTask> listRunning() {
        lock.lock();
        try {
            return repository.getRunningTasks();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ExecutionTask> listExecuting(String applicationId) {
        lock.lock();
        try {
            return repository.getExecutingTasks(applicationId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ExecutionTask> listWPSExecuting() { return repository.getWPSExecutingTasks(); }

    @Override
    public List<ExecutionTask> listWMSExecuting() { return repository.getWMSExecutingTasks(); }

    @Override
    public List<ExecutionTask> listByHost(String hostName) { return repository.getTasksByHost(hostName); }

    @Override
    public int countByHostSince(String hostName, LocalDateTime since) { return repository.getRunningTaskCountByHostSince(hostName, since); }

    @Override
    public LocalDateTime getLastRunTask(String hostName) {
        return repository.getLastRun(hostName);
    }

    @Override
    public List<ExecutionTask> getDataSourceTasks(long jobId) {
        return repository.getDataSourceTasks(jobId);
    }

    @Override
    public List<ExecutionTaskSummary> getTasksStatus(long jobId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
            PreparedStatement statement =
                    con.prepareStatement("SELECT t.id, w.name \"workflow\", CASE WHEN d.id IS NULL THEN CASE WHEN p.id IS NULL THEN 'group' ELSE n.\"name\" END ELSE d.Label END \"componentName\", " +
                                                 "t.start_time, t.end_time, t.execution_node_host_name, t.execution_status_id, t.last_updated, " +
                                                 "CASE WHEN ct.average_duration_seconds IS NULL OR ct.average_duration_seconds = 0 THEN -1 ELSE " +
                                                 "EXTRACT(EPOCH FROM (COALESCE(t.last_updated, t.start_time) - t.start_time)) /  ct.average_duration_seconds * 100.0 END as progress, t.execution_log, " +
                                                 "COALESCE(t.used_cpu, -1), COALESCE(t.used_ram, -1), t.command, j.name, j.user_id " +
                                                 "FROM execution.task t " +
                                                 "INNER JOIN execution.job j ON j.id = t.job_id " +
                                                 "LEFT OUTER JOIN workflow.graph w ON w.id = j.workflow_id " +
                                                 "LEFT OUTER JOIN workflow.graph_node n ON n.id = t.graph_node_id " +
                                                 "LEFT OUTER JOIN component.data_source_component d ON d.id = t.component_id " +
                                                 "LEFT OUTER JOIN component.processing_component p ON p.id = t.component_id " +
                                                 "LEFT OUTER JOIN execution.component_time ct ON t.component_id = ct.component_id " +
                                                 "WHERE job_id = ? " +
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
            timestamp = rs.getTimestamp(8);
            if (timestamp != null) {
                result.setLastUpdated(timestamp.toLocalDateTime());
            }
            double progress = rs.getDouble(9);
            // If the progress comes > 100.0, it means the task takes longer than the current average,
            // hence we set it to 99 not to overflow
            if (Double.compare(progress, 100.0) == 1) {
                progress = 99;
            }
            result.setPercentComplete(progress);
            result.setOutput(rs.getString(10));
            if (result.getOutput() == null) {
                result.setOutput("");
            }
            result.setUsedCPU(rs.getInt(11));
            result.setUsedRAM(rs.getInt(12));
            result.setCommand(rs.getString(13));
            result.setJobName(rs.getString(14));
            result.setUserId(rs.getString(15));
            return result;
        });
    }

    @Override
    public ExecutionTaskSummary getTaskStatus(long taskId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
            PreparedStatement statement =
                    con.prepareStatement("SELECT t.id, w.name \"workflow\", CASE WHEN d.id IS NULL THEN CASE WHEN p.id IS NULL THEN 'group' ELSE n.\"name\" END ELSE d.Label END \"componentName\", " +
                                                 "t.start_time, t.end_time, t.execution_node_host_name, t.execution_status_id, t.last_updated, " +
                                                 "CASE WHEN ct.average_duration_seconds IS NULL OR ct.average_duration_seconds = 0 THEN -1 ELSE " +
                                                 "EXTRACT(EPOCH FROM (COALESCE(t.last_updated, t.start_time) - t.start_time)) /  ct.average_duration_seconds * 100.0 END as progress, t.execution_log, " +
                                                 "COALESCE(t.used_cpu, -1), COALESCE(t.used_ram, -1), t.command, j.name, j.user_id " +
                                                 "FROM execution.task t " +
                                                 "INNER JOIN execution.job j ON j.id = t.job_id " +
                                                 "LEFT OUTER JOIN workflow.graph w ON w.id = j.workflow_id " +
                                                 "LEFT OUTER JOIN workflow.graph_node n ON n.id = t.graph_node_id " +
                                                 "LEFT OUTER JOIN component.data_source_component d ON d.id = t.component_id " +
                                                 "LEFT OUTER JOIN component.processing_component p ON p.id = t.component_id " +
                                                 "LEFT OUTER JOIN execution.component_time ct ON t.component_id = ct.component_id " +
                                                 "WHERE t.id = ?");
            statement.setLong(1, taskId);
            return statement;
        }, rs -> {
            ExecutionTaskSummary result = null;
            if (rs.next()) {
                result = new ExecutionTaskSummary();
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
                timestamp = rs.getTimestamp(8);
                if (timestamp != null) {
                    result.setLastUpdated(timestamp.toLocalDateTime());
                }
                double progress = rs.getDouble(9);
                // If the progress comes > 100.0, it means the task takes longer than the current average,
                // hence we set it to 99 not to overflow
                if (Double.compare(progress, 100.0) == 1) {
                    progress = 99;
                }
                result.setPercentComplete(progress);
                result.setOutput(rs.getString(10));
                if (result.getOutput() == null) {
                    result.setOutput("");
                }
                result.setUsedCPU(rs.getInt(11));
                result.setUsedRAM(rs.getInt(12));
                result.setCommand(rs.getString(13));
                result.setJobName(rs.getString(14));
                result.setUserId(rs.getString(15));
                result.setComponentType("exec");
            }
            return result;
        });
    }

        @Override
    public ExecutionTask getByJobAndNode(long jobId, long nodeId, int instanceId) {
        return repository.findByJobAndWorkflowNode(jobId, nodeId, instanceId);
    }

    @Override
    public ExecutionTask getByGroupAndNode(long groupId, long nodeId, int instanceId) {
        return repository.findByGroupAndWorkflowNode(groupId, nodeId, instanceId);
    }

    @Override
    public ExecutionTask getByResourceId(String id) {
        return repository.findByResourceId(id);
    }

    @Override
    public int getCPUsForUser(String userId) {
        return repository.getCPUsForUser(userId);
    }    

    @Override
    public int getMemoryForUser(String userId) {
        return repository.getMemoryForUser(userId);
    }

    @Override
    public int getRunningParents(long jobId, long taskId) {
        return repository.getRunningParents(jobId, taskId);
    }

    @Override
    public void updateComponentTime(String id, int duration) {
        try {
            repository.updateComponentTime(id, duration);
        } catch (Throwable t) {
            logger.severe(t.getMessage());
        }
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
        lock.lock();
        try {
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
                    task instanceof ExecutionGroup || task instanceof WPSExecutionTask || task instanceof WMSExecutionTask) {

                // set the task parent job
                task.setJob(job);

                // save the new ExecutionTask entity
                final ExecutionTask savedExecutionTask = repository.save(task);
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
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ExecutionTask save(ExecutionTask task, ExecutionGroup taskGroup) throws PersistenceException {
        lock.lock();
        try {
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
                final ExecutionTask savedExecutionTask = repository.save(task);
                // add the task to job tasks collection
                List<ExecutionTask> groupTasks = taskGroup.getTasks();
                if (groupTasks == null) {
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
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ExecutionTask saveWithSubTasks(ExecutionGroup taskGroup, ExecutionJob job) throws PersistenceException {

        // check method parameters
        if (!checkExecutionTask(taskGroup, job, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution group with sub-tasks !");
        }

        List<ExecutionTask> subTasks = taskGroup.getTasks() != null ? taskGroup.getTasks() : new ArrayList<>();

        taskGroup.setTasks(null);
        taskGroup = (ExecutionGroup) save(taskGroup, job);

        for (ExecutionTask subTask : subTasks){
            save(subTask, taskGroup);
        }

        return taskGroup;
    }

    @Transactional
    @Override
    public int countRunableTasks(long jobId) {
        return repository.countRunableTasks(jobId);
    }

    @Override
    public boolean isTerminalTask(long taskId) {
        return repository.isTerminalTask(taskId);
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
        return entity.getExecutionStatus() != null;// && entity.getWorkflowNodeId() != null;
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
