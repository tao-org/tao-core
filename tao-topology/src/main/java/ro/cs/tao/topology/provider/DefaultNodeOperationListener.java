package ro.cs.tao.topology.provider;

import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.ServiceInstallStatus;

/**
 * Default simple implementation of a node operation listener
 *
 * @author Cosmin Cara
 */
public class DefaultNodeOperationListener implements NodeOperationListener {

    @Override
    public void onCompleted(NodeDescription node, ServiceInstallStatus status) {
        switch (status.getStatus()) {
            case INSTALLED:
                Messaging.send(SystemPrincipal.instance(), Topic.INFORMATION.value(),
                               this,
                               String.format("%s installation on %s completed",
                                             status.getServiceName(),
                                             node.getId()));
                break;
            case UNINSTALLED:
                Messaging.send(SystemPrincipal.instance(), Topic.INFORMATION.value(),
                               this,
                               String.format("%s uninstallation on %s completed",
                                             status.getServiceName(),
                                             node.getId()));
                break;
            case ERROR:
                Messaging.send(SystemPrincipal.instance(), Topic.WARNING.value(),
                               this,
                               String.format("%s installation on %s failed [reason: %s]",
                                             status.getServiceName(),
                                             node.getId(),
                                             status.getReason()));
                break;
        }
    }
}
