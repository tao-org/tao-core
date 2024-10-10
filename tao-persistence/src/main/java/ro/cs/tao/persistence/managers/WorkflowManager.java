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
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.WorkflowProvider;
import ro.cs.tao.persistence.repository.WorkflowDescriptorRepository;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.workflow.*;
import ro.cs.tao.workflow.enums.ComponentType;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
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
                                if (component != null) {
                                    Query query = queryManager.get(finalWorkflow.getUserId(),
                                                                   component.getSensorName(),
                                                                   component.getDataSourceName(),
                                                                   n.getId());
                                    if (query != null) {
                                        n.setCustomValues(query.getValues().entrySet().stream()
                                                               .map(e -> new ParameterValue(e.getKey(), e.getValue()))
                                                               .collect(Collectors.toList()));
                                    }
                                } else {
                                    Logger.getLogger(WorkflowManager.class.getName()).warning(String.format("Component [%s] referred by node [%s] does not exist",
                                                                                                            n.getComponentId(), n.getName()));
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
    public List<WorkflowDescriptor> listUserWorkflowsByStatus(String userId, int statusId) {
        return repository.getUserWorkflowsByStatus(userId, statusId);
    }

    @Override
    public List<WorkflowDescriptor> listUserPublishedWorkflowsByVisibility(String userId, int visibilityId) {
        return repository.getUserPublishedWorkflowsByVisibility(userId, visibilityId);
    }

    @Override
    public List<WorkflowDescriptor> listOtherPublicWorkflows(String userId) {
        return repository.getOtherPublicWorkflows(userId);
    }

    @Override
    public List<WorkflowDescriptor> listPublic() {
        return repository.getPublicWorkflows();
    }

    @Override
    public List<WorkflowDescriptor> listUserVisible(String userId) {
        final List<WorkflowDescriptor> workflows = repository.getUserWorkflows(userId);
        final String value = ConfigurationManager.getInstance().getValue("other.workflows.visibility");
        if (value != null && Enum.valueOf(Visibility.class, value.toUpperCase()).equals(Visibility.SUBSCRIPTION)) {
            // if the visibility mode is SUBSCRIPTION,
            // only workflows published by admins and those subscribed to are returned
            //workflows.addAll(repository.getPublicWorkflows());
            workflows.addAll(repository.getSubscribedWorkflows(userId));
        } else {
            workflows.addAll(repository.getOtherPublicWorkflows(userId));
        }
        workflows.removeIf(WorkflowDescriptor::isTemporary);
        return workflows;
    }

    @Override
    public WorkflowDescriptor save(WorkflowDescriptor workflow) throws PersistenceException {
        // check method parameters
        /*if(!checkEntity(workflow, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new workflow !");
        }*/
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
            final List<Query> list = queryManager.list(entity.getUserId(), node.getId());
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
        if (workflow.isEmpty()) {
            throw new PersistenceException("There is no workflow with the specified identifier: " + workflowId);
        }

        final WorkflowDescriptor workflowEnt = workflow.get();
        // deactivate the workflow
        workflowEnt.setActive(false);
        // save it
        repository.save(workflowEnt);
    }

    @Override
    public Map<Long, String> getWorkflowImages(Set<Long> ids) {
        final List<Object[]> list = repository.getWorkflowImages(ids);
        return list != null && !list.isEmpty()
               ? list.stream().collect(Collectors.toMap(p -> p[0] instanceof BigInteger ? ((BigInteger) p[0]).longValue() : (Long) p[0], p -> (String) p[1]))
               : null;
    }

    @Override
    public String getWorkflowImage(long id) {
        return repository.getWorkflowImage(id);
    }

    @Override
    public void addWorkflowImage(long id, String image) {
        repository.insertImage(id, image);
    }

    @Override
    public void updateWorkflowImage(long id, String newImage) {
        repository.updateImage(id, newImage);
    }

    @Override
    public void deleteWorkflowImage(long id) {
        repository.deleteImage(id);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkEntity(WorkflowDescriptor entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getName()) &&
                !StringUtilities.isNullOrEmpty(entity.getUserId()) &&
                entity.getVisibility() != null && entity.getStatus() != null;
    }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return existingEntity
               ? entityId != null && (get(entityId) != null)
               : entityId == null;
    }
}
