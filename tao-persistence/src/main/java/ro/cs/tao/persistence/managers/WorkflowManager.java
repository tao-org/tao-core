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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.WorkflowDescriptorRepository;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("workflowManager")
public class WorkflowManager {

    private Logger logger = Logger.getLogger(WorkflowManager.class.getName());

    @PersistenceContext
    private EntityManager em;
    /** CRUD Repository for WorkflowDescriptor entities */
    @Autowired
    private WorkflowDescriptorRepository workflowDescriptorRepository;

    //region WorkflowDescriptor
    public List<WorkflowDescriptor> getAllWorkflows() {
        // retrieve workflows and filter them
        return ((List<WorkflowDescriptor>)
                workflowDescriptorRepository.findAll(new Sort(Sort.Direction.ASC,
                                                              Constants.WORKFLOW_IDENTIFIER_PROPERTY_NAME)))
                .stream()
                .filter(WorkflowDescriptor::isActive)
                .collect(Collectors.toList());
    }

    public List<WorkflowDescriptor> getWorkflows(Iterable<Long> ids) {
        return ((List<WorkflowDescriptor>) workflowDescriptorRepository.findAllById(ids))
                .stream().sorted(Comparator.comparingLong(LongIdentifiable::getId))
                .collect(Collectors.toList());
    }

    public WorkflowDescriptor getWorkflowDescriptor(Long identifier) {
        if (identifier != null){
            final Optional<WorkflowDescriptor> workflow = workflowDescriptorRepository.findById(identifier);
            if (workflow.isPresent()){
                return workflow.get();
            }
        }
        return null;
    }

    public WorkflowDescriptor getFullWorkflow(Long identifier) {
        WorkflowDescriptor workflow = workflowDescriptorRepository.getDetailById(identifier);
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
            }
        } else {
            workflow = getWorkflowDescriptor(identifier);
        }
        return workflow;
    }

    public WorkflowDescriptor getWorkflowByName(String name) {
        if (name != null) {
            return workflowDescriptorRepository.findByName(name).orElse(null);
        }
        return null;
    }

    public List<WorkflowDescriptor> getUserWorkflowsByStatus(String user, int statusId) {
        return workflowDescriptorRepository.getUserWorkflowsByStatus(user, statusId);
    }

    public List<WorkflowDescriptor> getUserPublishedWorkflowsByVisibility(String user, int visibilityId) {
        return workflowDescriptorRepository.getUserPublishedWorkflowsByVisibility(user, visibilityId);
    }

    public List<WorkflowDescriptor> getOtherPublicWorkflows(String user) {
        return workflowDescriptorRepository.getOtherPublicWorkflows(user);
    }

    public List<WorkflowDescriptor> getPublicWorkflows() {
        return workflowDescriptorRepository.getPublicWorkflows();
    }

    @Transactional
    public WorkflowDescriptor saveWorkflowDescriptor(WorkflowDescriptor workflow) throws PersistenceException {
        // check method parameters
        if(!checkWorkflowDescriptor(workflow, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new workflow !");
        }

        // by default a new workflow is active
        workflow.setActive(true);

        // save the new WorkflowDescriptor entity and return it
        return workflowDescriptorRepository.save(workflow);
    }

    @Transactional
    public WorkflowDescriptor updateWorkflowDescriptor(WorkflowDescriptor workflow) throws PersistenceException {
        // check method parameters
        if(!checkWorkflowDescriptor(workflow, true)) {
            throw new PersistenceException("Invalid parameters were provided for updating the workflow "
                                                   + (workflow != null && workflow.getId() != null ? "(identifier " + workflow.getId() + ")" : "") + "!");
        }

        // check if there is such workflow (to update) with the given identifier
        /*final WorkflowDescriptor existingWorkflow = workflowDescriptorRepository.findById(workflow.getId());
        if (existingWorkflow == null) {
            throw new PersistenceException("There is no workflow with the given identifier: " + workflow.getId());
        }*/

        // save the updated entity
        return workflowDescriptorRepository.save(workflow);
    }

    @Transactional
    public WorkflowDescriptor deleteWorkflowDescriptor(final Long workflowId) throws PersistenceException {
        // check method parameters
        if (workflowId == null) {
            throw new PersistenceException("Invalid identifier provided for deleting workflow");
        }

        // retrieve WorkflowDescriptor after its identifier
        final Optional<WorkflowDescriptor> workflow = workflowDescriptorRepository.findById(workflowId);
        if (!workflow.isPresent()) {
            throw new PersistenceException("There is no workflow with the specified identifier: " + workflowId);
        }

        final WorkflowDescriptor workflowEnt = workflow.get();
        // deactivate the workflow
        workflowEnt.setActive(false);
        // save it
        return workflowDescriptorRepository.save(workflowEnt);
    }
    //endregion


    private boolean checkWorkflowNodeDescriptor(WorkflowNodeDescriptor nodeDescriptor,
                                                WorkflowDescriptor workflowDescriptor, boolean existingEntity) {
        // check first the workflow (that should already be persisted)
        return !(!checkWorkflowDescriptor(workflowDescriptor, true) ||
                !checkIfExistsWorkflowDescriptorById(workflowDescriptor.getId())) &&
                checkWorkflowNodeDescriptor(nodeDescriptor, existingEntity);
    }

    private boolean checkWorkflowNodeDescriptor(WorkflowNodeDescriptor nodeDescriptor, boolean existingEntity) {
        return nodeDescriptor != null &&
                ((existingEntity && nodeDescriptor.getId() != null) || (!existingEntity && nodeDescriptor.getId() == null)) &&
                nodeDescriptor.getComponentId() != null && !nodeDescriptor.getComponentId().isEmpty();
    }

    private boolean checkWorkflowNodesDescriptors(List<WorkflowNodeDescriptor> nodesDescriptors , boolean existingEntity) {
        return nodesDescriptors != null &&
                nodesDescriptors.stream().allMatch(n -> checkWorkflowNodeDescriptor(n, n.getId() != null));
    }

    private boolean checkWorkflowDescriptor(WorkflowDescriptor workflow, boolean existingEntity) {
        return workflow != null && checkId(workflow, existingEntity) &&
                workflow.getName() != null && workflow.getStatus() != null && workflow.getVisibility() != null &&
                (workflow.getNodes() == null || checkWorkflowNodesDescriptors(workflow.getNodes(), existingEntity));

    }

    private boolean checkId(WorkflowDescriptor descriptor, boolean existingEntity) {
        return (!existingEntity && (descriptor.getId() == null || descriptor.getId() == 0)) ||
                (existingEntity && descriptor.getId() != null && descriptor.getId() > 0);
    }

    @Transactional
    private boolean checkIfExistsWorkflowDescriptorById(final Long workflowId) {
        if (workflowId != null && workflowId > 0) {
            // verify if such WorkflowDescriptor exists
            return workflowDescriptorRepository.existsById(workflowId);
        }
        return false;
    }
}
