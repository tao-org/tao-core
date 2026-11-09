package ro.cs.tao.persistence;

import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WorkflowNodeProvider is an interface that defines operations for managing
 * WorkflowNodeDescriptor entities. It extends the EntityProvider interface
 * and includes methods for listing nodes by component ID, retrieving group
 * nodes, saving nodes, updating positions, and finding cloned nodes.
 */
public interface WorkflowNodeProvider extends EntityProvider<WorkflowNodeDescriptor, Long> {

    /**
     * Retrieves a list of workflow node descriptors associated with the specified component ID
     * within a given workflow.
     *
     * @param workflowId the identifier of the workflow containing the nodes
     * @param componentId the identifier of the component for which nodes are to be listed
     * @return a list of {@link WorkflowNodeDescriptor} objects matching the specified criteria
     */
    List<WorkflowNodeDescriptor> listByComponentId(long workflowId, String componentId);
    /**
     * Retrieves a workflow node group descriptor that contains the specified child node.
     *
     * @param childNodeId the identifier of the child node for which the group node is to be retrieved
     * @return a {@link WorkflowNodeGroupDescriptor} that contains the specified child node, or null if no group is found
     */
    WorkflowNodeGroupDescriptor getGroupNode(long childNodeId);
    /**
     * Saves the specified workflow node descriptor within the context of the given workflow.
     *
     * @param node the {@link WorkflowNodeDescriptor} to be saved
     * @param workflow the {@link WorkflowDescriptor} within which the node is being saved
     * @return the saved {@link WorkflowNodeDescriptor} object with potential updates/revisions
     * @throws PersistenceException if any error occurs during the saving process
     */
    WorkflowNodeDescriptor save(WorkflowNodeDescriptor node, WorkflowDescriptor workflow) throws PersistenceException;
    /**
     * Updates the position of a workflow node identified by its unique ID.
     *
     * @param id the unique identifier of the workflow node to be updated
     * @param coordinates an array of float values representing the new position of the node
     * @throws PersistenceException if an error occurs during the update operation
     */
    void updatePosition(Long id, float[] coordinates) throws PersistenceException;
    /**
     * Finds and returns the cloned node within a specified workflow corresponding
     * to a given original node ID.
     *
     * @param workflowId the identifier of the workflow containing the cloned node
     * @param originalNodeId the identifier of the original node for which the cloned version is to be retrieved
     * @return the {@link WorkflowNodeDescriptor} representing the cloned node, or null if no cloned node is found
     */
    WorkflowNodeDescriptor findClonedNode(long workflowId, long originalNodeId);

    ComponentLink getLink(long fromNodeId, String fromOutputId, long toNodeId, String toInputId);

    Map<Long, String> getNodeNames(Set<Long> nodeIds);
}
