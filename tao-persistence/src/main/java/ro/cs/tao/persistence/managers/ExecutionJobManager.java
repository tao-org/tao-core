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
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTaskOutput;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.persistence.repository.ExecutionJobRepository;
import ro.cs.tao.utils.executors.MemoryUnit;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Configuration
@EnableTransactionManagement
@Component("executionJobManager")
public class ExecutionJobManager extends EntityManager<ExecutionJob, Long, ExecutionJobRepository>
                                 implements ExecutionJobProvider {

    @Autowired
    private DataSource dataSource;

    @Override
    public List<ExecutionJob> listByWorkflow(long workflowId, String userId) {
        return repository.findByWorkflowIdAndUserId(workflowId, userId);
    }

    @Override
    public List<ExecutionJob> listByWorkflow(long workflowId) {
        return repository.findByWorkflowId(workflowId);
    }

    @Override
    public List<String> getWorkflowOutputKeys(long workflowId, String userId) {
        return repository.getWorkflowJobsOutputs(workflowId, userId);
    }

    @Override
    public List<ExecutionTaskOutput> getWorkflowOutputs(long workflowId, String userId) {
        //return repository.getWorkflowJobsOutputs(workflowId);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
            PreparedStatement statement =
                    con.prepareStatement("SELECT t.job_id, t.id, t.end_time," +
                                        "REGEXP_REPLACE(t.actual_target, '.*/', '') AS filename, t.actual_target, COALESCE(d.data_format_id, 7) as data_format_id," +
                                        "CASE WHEN d.data_format_id = 1 THEN rdp.approximate_size ELSE -1 END AS \"size\" " +
                                        "FROM execution.task t " +
                                        "JOIN execution.job e ON e.id = t.job_id AND e.execution_status_id = 4 " +
                                        "JOIN workflow.graph_node n ON n.id = t.graph_node_id AND n.component_type_id = 2 " +
                                        "JOIN component.processing_component_targets ct ON ct.processing_component_id = t.component_id " +
                                        "JOIN component.target_descriptor d ON d.id = ct.target_descriptor_id " +
                                        "LEFT JOIN product.raster_data_product rdp ON rdp.entry_point = REGEXP_REPLACE(t.actual_target, '.*/', '') " +
                                        "WHERE e.workflow_id = ? AND e.user_id = ? ORDER BY t.job_id, t.end_time DESC");
            statement.setLong(1, workflowId);
            statement.setString(2, userId);
            return statement;
        }, (rs, rowNum) -> {
            ExecutionTaskOutput result = new ExecutionTaskOutput();
            result.setJobId(rs.getLong(1));
            result.setTaskId(rs.getLong(2));
            Timestamp timestamp = rs.getTimestamp(3);
            if (timestamp != null) {
                result.setEndTime(timestamp.toLocalDateTime());
            }
            result.setName(rs.getString(4));
            result.setPath(rs.getString(5));
            result.setFormat(EnumUtils.getEnumConstantByValue(DataFormat.class, rs.getInt(6)));
            final long value = rs.getLong(7);
            result.setSize(value > 0 ? value / MemoryUnit.MB.value() : -1);
            result.setUnit(value > 0 ? MemoryUnit.MB : null);
            return result;
        });
    }

    @Override
    public List<String> getOutputKeys(long jobId) {
        return repository.getJobOutputs(jobId);
    }

    @Override
    public List<ExecutionJob> list(ExecutionStatus status) {
        return repository.findByExecutionStatus(status);
    }

    @Override
    public int count(ExecutionStatus status) {
        return repository.countByExecutionStatus(status.value());
    }

    @Override
    public int count(String userId, ExecutionStatus status) {
        return repository.countByExecutionStatus(userId, status.value());
    }

    @Override
    public List<ExecutionJob> list(Set<ExecutionStatus> statuses) {
        return repository.findByExecutionStatuses(statuses.stream().map(ExecutionStatus::value).collect(Collectors.toSet()));
    }

    @Override
    public List<ExecutionJob> list(String userId, Set<ExecutionStatus> statuses) {
        Set<Integer> statusIds;
        if (statuses == null) {
            statusIds = Arrays.stream(ExecutionStatus.values()).map(ExecutionStatus::value).collect(Collectors.toSet());
        } else {
            statusIds = statuses.stream().map(ExecutionStatus::value).collect(Collectors.toSet());
        }
        return userId != null ?
                repository.findByStatusAndUser(statusIds, userId) :
                repository.findByStatus(statusIds);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return (!existingEntity && (entityId == null || entityId == 0)) ||
                (existingEntity && entityId != null && entityId > 0);
    }

    @Override
    protected boolean checkEntity(ExecutionJob entity) {
        return entity.getUserId() != null && entity.getExecutionStatus() != null;
    }

	@Override
	public boolean isBatchRunning(String batchId) {
        final Set<ExecutionStatus> statuses = new HashSet<>() {{
            add(ExecutionStatus.QUEUED_ACTIVE);
            add(ExecutionStatus.RUNNING);
            add(ExecutionStatus.PENDING_FINALISATION);
            add(ExecutionStatus.SUSPENDED);
            add(ExecutionStatus.UNDETERMINED);
        }};
		return repository.countJobsByBatchAndStatus(batchId, statuses.stream().map(ExecutionStatus::value).collect(Collectors.toSet())) > 0;
	}

	@Override
	public List<ExecutionJob> list(List<String> batchIds) {
		return repository.finbByBatches(batchIds);
	}

    @Override
    public List<DataSourceExecutionTask> getDatasourceTasks(long jobId) {
        return repository.getDatasourceTasks(jobId);
    }
}
