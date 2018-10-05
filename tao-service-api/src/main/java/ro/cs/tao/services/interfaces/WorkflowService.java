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
package ro.cs.tao.services.interfaces;

import ro.cs.tao.Tag;
import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.beans.Parameter;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.model.execution.ExecutionJobInfo;
import ro.cs.tao.services.model.execution.ExecutionTaskInfo;
import ro.cs.tao.services.model.workflow.WorkflowInfo;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.Status;

import java.util.List;
import java.util.Map;

/**
 * Service for Workflow entity manipulation.
 *
 * @author Cosmin Cara
 */
public interface WorkflowService extends CRUDService<WorkflowDescriptor, Long> {

    /**
     * Returns the workflows of a given user that have a specific status.
     * @param user      The user name (login)
     * @param status    The desired workflow status
     */
    List<WorkflowInfo> getUserWorkflowsByStatus(String user, Status status);
    /**
     * Returns the published (i.e. final) workflows of an user by their visibility.
     * @param user          The user name (login)
     * @param visibility    The workflow visibility
     */
    List<WorkflowInfo> getUserPublishedWorkflowsByVisibility(String user, Visibility visibility);
    /**
     * Returns the public workflows that are not belonging to a specific user.
     * @param user      The user name (login)
     */
    List<WorkflowInfo> getOtherPublicWorkflows(String user);

    /**
     * Returns all the public workflows.
     */
    List<WorkflowInfo> getPublicWorkflows();
    /**
     * Adds a node to a workflow.
     * @param nodeDescriptor    The node to add
     * @return  The updated workflow
     */
    WorkflowNodeDescriptor addNode(long workflowId, WorkflowNodeDescriptor nodeDescriptor) throws PersistenceException;
    /**
     * Updates a node to a workflow.
     * @param nodeDescriptor    The node to update
     * @return  The updated workflow
     */
    WorkflowNodeDescriptor updateNode(long workflowId, WorkflowNodeDescriptor nodeDescriptor) throws PersistenceException;
    /**
     * Removes a node from a workflow.
     * @param nodeDescriptor    The node to remove
     * @return  The updated workflow
     */
    void removeNode(long workflowId, WorkflowNodeDescriptor nodeDescriptor) throws PersistenceException;
    /**
     * Adds a link between nodes of a workflow.
     * @return  The updated target node
     */
    WorkflowNodeDescriptor addLink(long sourceNodeId, String sourceTargetId,
                                   long targetNodeId, String targetSourceId) throws PersistenceException;
    /**
     * Removes a link between nodes of a workflow.
     * @param link    The link to remove
     * @return  The updated target node
     */
    WorkflowNodeDescriptor removeLink(long nodeId, ComponentLink link) throws PersistenceException;
    /**
     * Adds a group node to a workflow.
     * @param workflowId         The workflow identifier
     * @param groupDescriptor    The group node to add
     * @param nodes              The nodes to be grouped
     * @return  The updated workflow
     */
    WorkflowNodeDescriptor addGroup(long workflowId, WorkflowNodeGroupDescriptor groupDescriptor,
                                    long nodeBeforeId, WorkflowNodeDescriptor[] nodes) throws PersistenceException;
    /**
     * Updates a group node of a workflow.
     * @param groupDescriptor    The group node to update
     * @return  The updated workflow
     */
    WorkflowNodeDescriptor updateGroup(WorkflowNodeGroupDescriptor groupDescriptor) throws PersistenceException;
    /**
     * Removes a group node from a workflow.
     * @param groupDescriptor    The group node to remove
     * @return  The updated workflow
     */
    void removeGroup(WorkflowNodeGroupDescriptor groupDescriptor, boolean removeChildren) throws PersistenceException;
    /**
     * Creates a duplicate of the given workflow
     *
     * @param workflow  The workflow to clone
     */
    WorkflowDescriptor clone(WorkflowDescriptor workflow) throws PersistenceException;

    /**
     * Imports the nodes of a workflow into the given workflow
     * @param master        The workflow into which the nodes shall be imported
     * @param subWorkflow   The workflow whose nodes shall be imported
     * @param keepDataSources Flag indicating if to keep (true) or exclude (false) DataSource nodes from the subWorkflow
     * @throws PersistenceException
     */
    WorkflowDescriptor importWorkflowNodes(WorkflowDescriptor master,
                                           WorkflowDescriptor subWorkflow,
                                           boolean keepDataSources) throws PersistenceException;
    /**
     * Retrieve the execution history of a workflow
     * @param workflowId         The workflow identifier
     * @return  The list of workflow executions
     * @throws PersistenceException
     */
    List<ExecutionJobInfo> getWorkflowExecutions(long workflowId) throws PersistenceException;
    /**
     * Retrieve workflow nodes execution details from a workflow execution
     * @param executionJobId         The workflow execution identifier
     * @return The list of nodes executions within a workflow execution
     * @throws PersistenceException
     */
    List<ExecutionTaskInfo> getWorkflowExecutionTasks(long executionJobId) throws PersistenceException;

    /**
     * Returns all the settable parameters of the components of a workflow.
     * The parameters are grouped by the component identifier
     * @param workflowId    The workflow identifier
     */
    Map<String, List<Parameter>> getWorkflowParameters(long workflowId);

    /**
     * Returns the output descriptors (i.e. the ones of the terminal node) of a workflow.
     * @param workflowId    The workflow identifier
     */
    List<TargetDescriptor> getWorkflowOutputs(long workflowId);

    /**
     * Returns all tags that are associated with workflows.
     */
    List<Tag> getWorkflowTags();
}
