package ro.cs.tao.topology;

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
    NodeDescription create(NodeDescription node) throws TopologyException;
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
    NodeDescription create(NodeFlavor flavor, String name, String description, String user, String pwd) throws TopologyException;
    /**
     * Removes a node from the TAO topology. Depending on the actual implementation, some providers may also delete
     * the physical node.
     *
     * @param nodeName      The node (host) name.
     */
    void remove(String nodeName) throws TopologyException;
    /**
     * Removes a node from the TAO topology. Depending on the actual implementation, some providers may also delete
     * the physical node.
     *
     * @param node  The node descriptor
     */
    default void remove(NodeDescription node)  throws TopologyException {
        if (node != null) {
            remove(node.getId());
        }
    }
    /**
     * Puts the node in an inactive state. Depending on the actual implementation, some providers may only mark
     * the node as inactive, while others may put the physical node in a suspended state.
     *
     * @param nodeName  The node (host) name
     */
    void suspend(String nodeName) throws TopologyException;
    /**
     * Puts the node in an inactive state. Depending on the actual implementation, some providers may only mark
     * the node as inactive, while others may put the physical node in a suspended state.
     *
     * @param node  The node descriptor
     */
    default void suspend(NodeDescription node)  throws TopologyException {
        if (node != null) {
            suspend(node.getId());
        }
    }
    /**
     * Puts the node in an active state. Depending on the actual implementation, some providers may only mark
     * the node as active, while others may bring back the physical node in a runnable state.
     *
     * @param nodeName  The node (host) name
     */
    void resume(String nodeName)  throws TopologyException;
    /**
     * Puts the node in an active state. Depending on the actual implementation, some providers may only mark
     * the node as active, while others may bring back the physical node in a runnable state.
     *
     * @param node  The node descriptor
     */
    default void resume(NodeDescription node)  throws TopologyException {
        if (node != null) {
            resume(node.getId());
        }
    }
    /**
     * Updates the node descriptor. Depending on the actual implementation, some providers may also alter the flavor
     * of the physical node.
     *
     * @param node  The node descriptor
     */
    NodeDescription update(NodeDescription node) throws TopologyException;

    default int countUsableNodes(String userId) { return 1; }
}
