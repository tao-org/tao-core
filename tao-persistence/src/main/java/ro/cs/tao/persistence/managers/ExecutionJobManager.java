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

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.persistence.repository.ExecutionJobRepository;

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

    @Override
    public List<ExecutionJob> listByWorkflow(long workflowId) {
        return repository.findByWorkflowId(workflowId);
    }

    @Override
    public List<String> getWorkflowOutputKeys(long workflowId) {
        return repository.getWorkflowJobsOutputs(workflowId);
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
