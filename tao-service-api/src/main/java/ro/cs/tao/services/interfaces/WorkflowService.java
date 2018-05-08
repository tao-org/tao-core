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
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

/**
 * @author Cosmin Cara
 */
public interface WorkflowService extends CRUDService<WorkflowDescriptor> {

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
    WorkflowDescriptor removeNode(long workflowId, WorkflowNodeDescriptor nodeDescriptor) throws PersistenceException;
    /**
     * Adds a link between nodes of a workflow.
     * @return  The updated workflow
     */
    WorkflowDescriptor addLink(long sourceNodeId, String sourceTargetId,
                               long targetNodeId, String targetSourceId) throws PersistenceException;
    /**
     * Removes a link between nodes of a workflow.
     * @param link    The link to remove
     * @return  The updated workflow
     */
    WorkflowDescriptor removeLink(long nodeId, ComponentLink link) throws PersistenceException;
    /**
     * Adds a group node to a workflow.
     * @param workflowId         The workflow identifier
     * @param groupDescriptor    The group node to add
     * @param nodes              The nodes to be grouped
     * @return  The updated workflow
     */
    WorkflowDescriptor addGroup(long workflowId, WorkflowNodeGroupDescriptor groupDescriptor,
                                long nodeBeforeId,
                                WorkflowNodeDescriptor[] nodes) throws PersistenceException;
    /**
     * Updates a group node of a workflow.
     * @param groupDescriptor    The group node to update
     * @return  The updated workflow
     */
    WorkflowDescriptor updateGroup(WorkflowNodeGroupDescriptor groupDescriptor) throws PersistenceException;
    /**
     * Removes a group node from a workflow.
     * @param groupDescriptor    The group node to remove
     * @return  The updated workflow
     */
    WorkflowDescriptor removeGroup(WorkflowNodeGroupDescriptor groupDescriptor) throws PersistenceException;
}
