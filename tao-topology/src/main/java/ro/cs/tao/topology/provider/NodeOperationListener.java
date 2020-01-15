package ro.cs.tao.topology.provider;

import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.ServiceInstallStatus;

/**
 * Listener for completion of node service installation operations
 *
 * @author Cosmin Cara
 */
public interface NodeOperationListener {

    /**
     * Method called when a node or tool installation operation completes (either successful or with error)
     *
     * @param node      The node on which the event occured
     * @param status    The status of the installation operation
     */
    void onCompleted(NodeDescription node, ServiceInstallStatus status);

}
