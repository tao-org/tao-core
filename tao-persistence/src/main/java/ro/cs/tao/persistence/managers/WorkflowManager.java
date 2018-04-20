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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.WorkflowDescriptorRepository;
import ro.cs.tao.persistence.repository.WorkflowNodeDescriptorRepository;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("workflowManager")
public class WorkflowManager {

    private Logger logger = Logger.getLogger(WorkflowManager.class.getName());

    /** CRUD Repository for WorkflowDescriptor entities */
    @Autowired
    private WorkflowDescriptorRepository workflowDescriptorRepository;

    /** CRUD Repository for WorkflowDescriptor entities */
    @Autowired
    private WorkflowNodeDescriptorRepository workflowNodeDescriptorRepository;

    //region WorkflowDescriptor
    @Transactional(readOnly = true)
    public List<WorkflowDescriptor> getAllWorkflows() {
        // retrieve workflows and filter them
        return ((List<WorkflowDescriptor>)
                workflowDescriptorRepository.findAll(new Sort(Sort.Direction.ASC,
                                                              Constants.WORKFLOW_IDENTIFIER_PROPERTY_NAME)))
                .stream()
                .filter(WorkflowDescriptor::isActive)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowDescriptor getWorkflowDescriptor(long identifier) {
        return workflowDescriptorRepository.findById(identifier);
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
        final WorkflowDescriptor workflowEnt = workflowDescriptorRepository.findById(workflowId);
        if (workflowEnt == null) {
            throw new PersistenceException("There is no workflow with the specified identifier: " + workflowId);
        }

        // deactivate the workflow
        workflowEnt.setActive(false);

        // save it
        return workflowDescriptorRepository.save(workflowEnt);
    }
    //endregion

    //region WorkflowNodeDescriptor
    @Transactional(readOnly = true)
    public WorkflowNodeDescriptor getWorkflowNodeById(Long id) {
        return id != null ? workflowNodeDescriptorRepository.findById(id) : null;
    }

    @Transactional(readOnly = true)
    public List<WorkflowNodeDescriptor> getWorkflowNodesByComponentId(long workflowId, String componentId) {
        return workflowNodeDescriptorRepository.findByComponentId(workflowId, componentId);
    }

    @Transactional
    public WorkflowNodeDescriptor saveWorkflowNodeDescriptor(WorkflowNodeDescriptor node,
                                                             WorkflowDescriptor workflow) throws PersistenceException {
        // check method parameters
        if(!checkWorkflowNodeDescriptor(node, workflow, false)) {
            throw new PersistenceException("Invalid parameters were provided for adding new workflow node !");
        }

        //node.setWorkflow(workflow);

        // save the new WorkflowNodeDescriptor entity
        //final WorkflowNodeDescriptor savedWorkflowNodeDescriptor =  workflowNodeDescriptorRepository.save(node);

        // add the node to workflow nodes collection
        //workflow.addNode(savedWorkflowNodeDescriptor);
        workflow.addNode(node);
        workflowDescriptorRepository.save(workflow);

        //return savedWorkflowNodeDescriptor;
        return node;
    }

    @Transactional
    public WorkflowNodeDescriptor updateWorkflowNodeDescriptor(WorkflowNodeDescriptor node) throws PersistenceException {
        // check method parameters
        if(!checkWorkflowNodeDescriptor(node, true)) {
            throw new PersistenceException("Invalid parameters were provided for updating the workflow node "
                                                   + (node != null && node.getId() != 0 ? "(identifier " + node.getId() + ")" : "") + "!");
        }

        // check if there is such node (to update) with the given identifier
        /*final WorkflowNodeDescriptor existingNode = workflowNodeDescriptorRepository.findById(node.getId());
        if (existingNode == null) {
            throw new PersistenceException("There is no workflow node with the given identifier: " + node.getId());
        }*/

        // save the updated entity
        return workflowNodeDescriptorRepository.save(node);
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
        return nodeDescriptor != null && !(existingEntity && nodeDescriptor.getId() == null) &&
                !(!existingEntity && nodeDescriptor.getId() != null) &&
                !(existingEntity && (nodeDescriptor.getComponentId() == null || nodeDescriptor.getComponentId().isEmpty()));
    }

    private boolean checkWorkflowNodesDescriptors(List<WorkflowNodeDescriptor> nodesDescriptors , boolean existingEntity) {
        return nodesDescriptors != null &&
                nodesDescriptors.stream().allMatch(n -> checkWorkflowNodeDescriptor(n, existingEntity));
    }

    private boolean checkWorkflowDescriptor(WorkflowDescriptor workflow, boolean existingEntity) {
        return workflow != null &&
                (existingEntity || workflow.getId() == null) &&
                (!existingEntity || workflow.getId() != null) &&
                workflow.getName() != null && workflow.getStatus() != null && workflow.getVisibility() != null &&
                (workflow.getNodes() == null || checkWorkflowNodesDescriptors(workflow.getNodes(), existingEntity));

    }

    @Transactional(readOnly = true)
    private boolean checkIfExistsWorkflowDescriptorById(final Long workflowId) {
        boolean result = false;
        if (workflowId != null && workflowId > 0) {
            // try to retrieve WorkflowDescriptor after its identifier
            final WorkflowDescriptor workflowEnt = workflowDescriptorRepository.findById(workflowId);
            if (workflowEnt != null) {
                result = true;
            }
        }

        return result;
    }
}
