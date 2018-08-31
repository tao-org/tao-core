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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.repository.WorkflowNodeDescriptorRepository;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.List;

@Component("workflowNodeDescriptorManager")
public class WorkflowNodeDescriptorManager extends EntityManager<WorkflowNodeDescriptor, Long, WorkflowNodeDescriptorRepository> {

    @Transactional
    public List<WorkflowNodeDescriptor> getWorkflowNodesByComponentId(long workflowId, String componentId) {
        return repository.findByComponentId(workflowId, componentId);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return (existingEntity && entityId != null && entityId != 0) ||
                (!existingEntity && (entityId == null || entityId == 0));
    }

    @Override
    protected boolean checkEntity(WorkflowNodeDescriptor entity) {
        return entity.getComponentId() != null && !entity.getComponentId().isEmpty();
    }
}
