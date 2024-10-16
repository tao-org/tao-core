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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.WorkflowNodeProvider;
import ro.cs.tao.persistence.repository.WorkflowDescriptorRepository;
import ro.cs.tao.persistence.repository.WorkflowNodeDescriptorRepository;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.util.List;
@Configuration
@EnableTransactionManagement
@Component("workflowNodeDescriptorManager")
public class WorkflowNodeDescriptorManager extends EntityManager<WorkflowNodeDescriptor, Long, WorkflowNodeDescriptorRepository>
                                            implements WorkflowNodeProvider {

    @Autowired
    private WorkflowDescriptorRepository workflowDescriptorRepository;

    @Override
    public List<WorkflowNodeDescriptor> listByComponentId(long workflowId, String componentId) {
        return repository.findByComponentId(workflowId, componentId);
    }

    @Override
    public WorkflowNodeGroupDescriptor getGroupNode(long nodeId) {
        return repository.getGroupNode(nodeId);
    }

    @Override
    public WorkflowNodeDescriptor save(WorkflowNodeDescriptor node, WorkflowDescriptor workflow) throws PersistenceException {
        if (workflow == null) {
            throw new PersistenceException("[workflow] null");
        }
        if (node == null) {
            throw new PersistenceException("[node] null");
        }
        node.setWorkflow(workflow);
        node = repository.save(node);
        workflow.addNode(node);
        workflowDescriptorRepository.save(workflow);
        return node;
    }

    @Override
    public WorkflowNodeDescriptor findClonedNode(long workflowId, long originalNodeId) {
        return repository.findClonedNode(workflowId, originalNodeId);
    }

    @Override
    public void updatePosition(Long id, float[] coordinates) throws PersistenceException {
        this.repository.updatePosition(id, coordinates[0], coordinates[1]);
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
