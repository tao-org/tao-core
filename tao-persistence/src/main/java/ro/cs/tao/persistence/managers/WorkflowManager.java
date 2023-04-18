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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.WorkflowProvider;
import ro.cs.tao.persistence.repository.WorkflowDescriptorRepository;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.workflow.*;
import ro.cs.tao.workflow.enums.ComponentType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("workflowManager")
public class WorkflowManager extends EntityManager<WorkflowDescriptor, Long, WorkflowDescriptorRepository>
                             implements WorkflowProvider {

//    @PersistenceContext
//    private javax.persistence.EntityManager em;
    @Autowired
    private QueryManager queryManager;
    @Autowired
    private DataSourceComponentManager dataSourceComponentManager;

    @Override
    public List<WorkflowDescriptor> listActive() {
        // retrieve workflows and filter them
        return list().stream().filter(WorkflowDescriptor::isActive).collect(Collectors.toList());
    }

    @Override
    public WorkflowDescriptor loadWorkflowDescriptor(long identifier) {
        WorkflowDescriptor workflow = repository.getDetailById(identifier);
        if (workflow != null) {
            List<WorkflowNodeDescriptor> nodes = workflow.getNodes();
            if (nodes != null) {
                workflow.setNodes(workflow.getNodes().stream().distinct().collect(Collectors.toList()));
                workflow.getNodes().stream()
                        .filter(n -> n instanceof WorkflowNodeGroupDescriptor)
                        .map(n -> (WorkflowNodeGroupDescriptor) n)
                        .forEach(g -> {
                            if (g.getNodes() != null) {
                                g.setNodes(g.getNodes().stream().distinct().collect(Collectors.toList()));
                            }
                        });
                WorkflowDescriptor finalWorkflow = workflow;
                workflow.getNodes().stream()
                        .filter(n -> n.getComponentType() == ComponentType.DATASOURCE)
                        .forEach(n -> {
                            List<ParameterValue> customValues = n.getCustomValues();
                            if (customValues == null || customValues.isEmpty()) {
                                DataSourceComponent component = dataSourceComponentManager.get(n.getComponentId());
                                Query query = queryManager.get(finalWorkflow.getUserName(),
                                                               component.getSensorName(),
                                                               component.getDataSourceName(),
                                                               n.getId());
                                if (query != null) {
                                    n.setCustomValues(query.getValues().entrySet().stream()
                                                           .map(e -> new ParameterValue(e.getKey(), e.getValue()))
                                                           .collect(Collectors.toList()));
                                }
                            }
                        });
                workflow.getNodes().sort(Comparator.comparingInt(WorkflowNodeDescriptor::getLevel)
                                                   .thenComparing(GraphObject::getName));
            }
        } else {
            workflow = get(identifier);
        }
        return workflow;
    }

    @Override
    public WorkflowDescriptor getByName(String name) {
        if (name != null) {
            return repository.findByName(name).orElse(null);
        }
        return null;
    }

    @Override
    public WorkflowDescriptor getByNodeId(long workflowNodeId) {
        return repository.getByNodeId(workflowNodeId);
    }

    @Override
    public List<WorkflowDescriptor> listUserWorkflowsByStatus(String user, int statusId) {
        return repository.getUserWorkflowsByStatus(user, statusId);
    }

    @Override
    public List<WorkflowDescriptor> listUserPublishedWorkflowsByVisibility(String user, int visibilityId) {
        return repository.getUserPublishedWorkflowsByVisibility(user, visibilityId);
    }

    @Override
    public List<WorkflowDescriptor> listOtherPublicWorkflows(String user) {
        return repository.getOtherPublicWorkflows(user);
    }

    @Override
    public List<WorkflowDescriptor> listPublic() {
        return repository.getPublicWorkflows();
    }

    @Override
    public List<WorkflowDescriptor> listUserVisible(String user) {
        return repository.getUserVisibleWorkflows(user);
    }

    @Override
    public WorkflowDescriptor save(WorkflowDescriptor workflow) throws PersistenceException {
        // check method parameters
        if(!checkEntity(workflow, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new workflow !");
        }
        // by default a new workflow is active
        workflow.setActive(true);
        // save the new WorkflowDescriptor entity and return it
        return repository.save(workflow);
    }

    @Override
    public void delete(WorkflowDescriptor entity) throws PersistenceException {
        // check method parameters
        if (entity == null) {
            throw new PersistenceException("Invalid entity provided for deleting workflow");
        }
        // retrieve WorkflowDescriptor after its identifier
        final Optional<WorkflowDescriptor> workflow = repository.findById(entity.getId());
        if (workflow.isEmpty()) {
            throw new PersistenceException("There is no workflow with the specified identifier: " + entity.getId());
        }
        final List<WorkflowNodeDescriptor> nodes = workflow.get().getOrderedNodes();
        for (WorkflowNodeDescriptor node : nodes) {
            final List<Query> list = queryManager.list(entity.getUserName(), node.getId());
            if (list != null) {
                for (Query query : list) {
                    queryManager.delete(query);
                }
            }
        }
        repository.delete(entity);
    }

    @Override
    public void delete(final Long workflowId) throws PersistenceException {
        // check method parameters
        if (workflowId == null) {
            throw new PersistenceException("Invalid identifier provided for deleting workflow");
        }

        // retrieve WorkflowDescriptor after its identifier
        final Optional<WorkflowDescriptor> workflow = repository.findById(workflowId);
        if (!workflow.isPresent()) {
            throw new PersistenceException("There is no workflow with the specified identifier: " + workflowId);
        }

        final WorkflowDescriptor workflowEnt = workflow.get();
        // deactivate the workflow
        workflowEnt.setActive(false);
        // save it
        repository.save(workflowEnt);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkEntity(WorkflowDescriptor entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getName()) &&
                !StringUtilities.isNullOrEmpty(entity.getUserName()) &&
                entity.getVisibility() != null && entity.getStatus() != null;
    }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return entityId != null && (existingEntity == (get(entityId) != null));
    }
}
