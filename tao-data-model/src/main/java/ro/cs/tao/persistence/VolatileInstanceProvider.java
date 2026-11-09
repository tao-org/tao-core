package ro.cs.tao.persistence;

import ro.cs.tao.topology.VolatileInstance;

import java.util.List;

/**
 * Provides an interface for managing volatile instances with specific methods
 * to retrieve and delete instances based on user or node identifiers, as well
 * as retrieve instances filtered by a user or flavor.
 *
 * As a specialization of {@link EntityProvider}, this interface extends the
 * functionality to support domain-specific operations related to {@link VolatileInstance}.
 */
public interface VolatileInstanceProvider extends EntityProvider<VolatileInstance, Long> {

    /**
     * Retrieves a list of VolatileInstance objects associated with the specified user.
     *
     * @param userId the unique identifier of the user whose instances are to be retrieved
     * @return a list of VolatileInstance objects corresponding to the specified user
     */
    List<VolatileInstance> getUserInstances(String userId);

    /**
     * Retrieves a list of VolatileInstance objects associated with the specified flavor.
     *
     * @param flavorId the unique identifier of the flavor whose associated instances are to be retrieved
     * @return a list of VolatileInstance objects corresponding to the specified flavor
     */
    List<VolatileInstance> getFlavorInstances(String flavorId);

    /**
     * Deletes the volatile instance(s) associated with the specified node identifier.
     *
     * @param nodeId the unique identifier of the node whose associated volatile instance(s)
     *               are to be deleted
     * @throws PersistenceException if an error occurs during the deletion process
     */
    void deleteByNode(String nodeId) throws PersistenceException;

    /**
     * Deletes the volatile instance(s) associated with the specified user identifier.
     *
     * @param userId the unique identifier of the user whose associated volatile instance(s)
     *               are to be deleted
     * @throws PersistenceException if an error occurs during the deletion process
     */
    void deleteByUser(String userId) throws PersistenceException;
}
