package ro.cs.tao.topology.provider;

import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.topology.TopologyException;

import java.util.List;

/**
 * A node provider is responsible with providing nodes (computing machines) for the TAO topology.
 *
 * @author Cosmin Cara
 */
public interface NodeProvider {
    /**
     * Performs any authentication necessary by the underlying provider.
     */
    void authenticate();
    /**
     * Lists the flavors supported by the underlying provider.
     */
    List<NodeFlavor> listFlavors() throws TopologyException;
    /**
     * Lists the active nodes registered with TAO from the underlying provider.
     */
    List<NodeDescription> listNodes() throws TopologyException;
    /**
     * Gets the details of the given node
     *
     * @param nodeName  The node (host) name
     */
    NodeDescription getNode(String nodeName) throws TopologyException;
    /**
     * Adds a node to the TAO topology. Depending on the actual implementation, some providers may also create
     * the physical node.
     *
     * @param node  The node information
     */
    NodeDescription addNode(NodeDescription node) throws TopologyException;
    /**
     * Adds a node to the TAO topology. Depending on the actual implementation, some providers may also create
     * the physical node.
     *
     * @param flavor    The node flavor (CPU, memory, disk)
     * @param name      The node name (identifier)
     * @param description   The node description
     * @param user      The user that has (or will have) administrative privileges on the node
     * @param pwd       The user password
     */
    NodeDescription addNode(NodeFlavor flavor, String name, String description, String user, String pwd) throws TopologyException;
    /**
     * Removes a node from the TAO topology. Depending on the actual implementation, some providers may also delete
     * the physical node.
     *
     * @param nodeName      The node (host) name.
     */
    void removeNode(String nodeName) throws TopologyException;
    /**
     * Removes a node from the TAO topology. Depending on the actual implementation, some providers may also delete
     * the physical node.
     *
     * @param node  The node descriptor
     */
    default void removeNode(NodeDescription node)  throws TopologyException {
        if (node != null) {
            removeNode(node.getId());
        }
    }
    /**
     * Puts the node in an inactive state. Depending on the actual implementation, some providers may only mark
     * the node as inactive, while others may put the physical node in a suspended state.
     *
     * @param nodeName  The node (host) name
     */
    void suspendNode(String nodeName) throws TopologyException;
    /**
     * Puts the node in an inactive state. Depending on the actual implementation, some providers may only mark
     * the node as inactive, while others may put the physical node in a suspended state.
     *
     * @param node  The node descriptor
     */
    default void suspendNode(NodeDescription node)  throws TopologyException {
        if (node != null) {
            suspendNode(node.getId());
        }
    }
    /**
     * Puts the node in an active state. Depending on the actual implementation, some providers may only mark
     * the node as active, while others may bring back the physical node in a runnable state.
     *
     * @param nodeName  The node (host) name
     */
    void resumeNode(String nodeName)  throws TopologyException;
    /**
     * Puts the node in an active state. Depending on the actual implementation, some providers may only mark
     * the node as active, while others may bring back the physical node in a runnable state.
     *
     * @param node  The node descriptor
     */
    default void resumeNode(NodeDescription node)  throws TopologyException {
        if (node != null) {
            resumeNode(node.getId());
        }
    }
    /**
     * Updates the node descriptor. Depending on the actual implementation, some providers may also alter the flavor
     * of the physical node.
     *
     * @param node  The node descriptor
     */
    NodeDescription updateNode(NodeDescription node) throws TopologyException;
}
