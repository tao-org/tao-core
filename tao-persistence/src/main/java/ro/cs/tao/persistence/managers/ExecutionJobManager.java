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

import org.springframework.stereotype.Component;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.persistence.repository.ExecutionJobRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("executionJobManager")
public class ExecutionJobManager extends EntityManager<ExecutionJob, Long, ExecutionJobRepository> {

    public List<ExecutionJob> list(long workflowId) {
        return repository.findByWorkflowId(workflowId);
    }

    public List<ExecutionJob> list(ExecutionStatus status) {
        return repository.findByExecutionStatus(status);
    }

    public List<ExecutionJob> list(String userName, Set<ExecutionStatus> statuses) {
        Set<Integer> statusIds;
        if (statuses == null) {
            statusIds = Arrays.stream(ExecutionStatus.values()).map(ExecutionStatus::value).collect(Collectors.toSet());
        } else {
            statusIds = statuses.stream().map(ExecutionStatus::value).collect(Collectors.toSet());
        }
        return repository.findByStatusAndUser(statusIds, userName);
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
        return entity.getUserName() != null && entity.getExecutionStatus() != null;
    }
}
