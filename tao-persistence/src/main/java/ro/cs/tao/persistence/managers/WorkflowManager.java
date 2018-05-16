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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.WorkflowDescriptorRepository;
import ro.cs.tao.persistence.repository.WorkflowNodeDescriptorRepository;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public List<WorkflowDescriptor> getAllWorkflows() {
        // retrieve workflows and filter them
        return ((List<WorkflowDescriptor>)
                workflowDescriptorRepository.findAll(new Sort(Sort.Direction.ASC,
                                                              Constants.WORKFLOW_IDENTIFIER_PROPERTY_NAME)))
                .stream()
                .filter(WorkflowDescriptor::isActive)
                .collect(Collectors.toList());
    }

    public WorkflowDescriptor getWorkflowDescriptor(long identifier) {
        return workflowDescriptorRepository.findById(identifier);
    }

    @Query(value = "SELECT * from tao.workflow_graph WHERE username = :user AND status_id = :statusId " +
            "ORDER BY created DESC", nativeQuery = true)
    public List<WorkflowDescriptor> getUserWorkflowsByStatus(String user, int statusId) {
        return workflowDescriptorRepository.getUserWorkflowsByStatus(user, statusId);
    }

    @Query(value = "SELECT * from tao.workflow_graph WHERE username = :user AND visibility_id = :visibilityId" +
            "ORDER BY created DESC", nativeQuery = true)
    public List<WorkflowDescriptor> getUserPublishedWorkflowsByVisibility(String user, int visibilityId) {
        return workflowDescriptorRepository.getUserPublishedWorkflowsByVisibility(user, visibilityId);
    }

    @Query(value = "SELECT * from tao.workflow_graph WHERE username != :user AND visibility_id = 1 " +
            "AND status_id = 3 ORDER BY created DESC", nativeQuery = true)
    public List<WorkflowDescriptor> getOtherPublicWorkflows(String user) {
        return workflowDescriptorRepository.getOtherPublicWorkflows(user);
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
    @Transactional
    public WorkflowNodeDescriptor getWorkflowNodeById(Long id) {
        return id != null ? workflowNodeDescriptorRepository.findById(id) : null;
    }

    @Transactional
    public List<WorkflowNodeDescriptor> getWorkflowNodesById(Long... ids) {
        if (ids != null && ids.length > 0) {
            Set<Long> nodeIds = new HashSet<>();
            Collections.addAll(nodeIds, ids);
            return workflowNodeDescriptorRepository.getWorkflowsById(nodeIds);
        }
        return null;
    }

    @Transactional
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

        node.setWorkflow(workflow);

        // save the new WorkflowNodeDescriptor entity
        final WorkflowNodeDescriptor savedWorkflowNodeDescriptor =  workflowNodeDescriptorRepository.save(node);

        // add the node to workflow nodes collection
        workflow.addNode(savedWorkflowNodeDescriptor);
        //workflow.addNode(node);
        workflowDescriptorRepository.save(workflow);
        return savedWorkflowNodeDescriptor;
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
        try {
            return workflowNodeDescriptorRepository.save(node);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public void delete(WorkflowNodeDescriptor nodeDescriptor) {
        workflowNodeDescriptorRepository.delete(nodeDescriptor);
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
        return workflow != null &&
                ((!existingEntity && workflow.getId() == null) || (existingEntity && workflow.getId() != null)) &&
                workflow.getName() != null && workflow.getStatus() != null && workflow.getVisibility() != null &&
                (workflow.getNodes() == null || checkWorkflowNodesDescriptors(workflow.getNodes(), existingEntity));

    }

    @Transactional
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
