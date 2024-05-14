package ro.cs.tao.persistence;

import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.topology.ServiceDescription;

import java.util.List;

public interface NodeDBProvider extends EntityProvider<NodeDescription, String> {

    List<NodeDescription> getByFlavor(NodeFlavor flavor);
    List<NodeDescription> getByUserWithFlavor(String userId, String flavorId);
    List<NodeDescription> list(boolean active);
    ServiceDescription getServiceDescription(String name, String version);
    ServiceDescription saveServiceDescription(ServiceDescription service) throws PersistenceException;
    int countUsableNodes(String userId);
}
