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
package ro.cs.tao.services.interfaces;

import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.Status;
import ro.cs.tao.workflow.enums.Visibility;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface WorkflowService extends CRUDService<WorkflowDescriptor> {

    List<WorkflowDescriptor> getUserWorkflowsByStatus(String user, Status status);

    List<WorkflowDescriptor> getUserPublishedWorkflowsByVisibility(String user, Visibility visibility);

    List<WorkflowDescriptor> getOtherPublicWorkflows(String user);

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
                                long nodeBeforeId,
                                WorkflowNodeDescriptor[] nodes) throws PersistenceException;
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
     * Retrieve the execution history of a workflow
     * @param workflowId         The workflow identifier
     * @return  The list of workflow executions
     * @throws PersistenceException
     */
    List<ExecutionJob> getWorkflowExecutions(long workflowId) throws PersistenceException;

    /**
     * Retrieve workflow nodes execution details from a workflow execution
     * @param executionJobId         The workflow execution identifier
     * @return The list of nodes executions within a workflow execution
     * @throws PersistenceException
     */
    List<ExecutionTask> getWorkflowExecutionTasks(long executionJobId) throws PersistenceException;
}
