package ro.cs.tao.persistence;

import ro.cs.tao.topology.VolatileInstance;

import java.util.List;

public interface VolatileInstanceProvider extends EntityProvider<VolatileInstance, Long> {

    List<VolatileInstance> getUserInstances(String userId);

    List<VolatileInstance> getFlavorInstances(String flavorId);

    void deleteByNode(String nodeId) throws PersistenceException;

    void deleteByUser(String userId) throws PersistenceException;
}
