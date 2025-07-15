package ro.cs.tao.persistence;

import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.topology.ServiceDescription;

import java.util.List;

/**
 * The NodeDBProvider interface defines methods for managing nodes and service descriptions
 * in a persistence layer. It extends the {@link EntityProvider} interface to include additional
 * operations specific to nodes and services, providing functionality for querying, saving,
 * and updating these entities.
 *
 * This interface is designed to handle entities of type {@link NodeDescription} with String identifiers,
 * including functionality for filtering nodes by flavors or users, counting usable nodes, and managing
 * service-related data.
 */
public interface NodeDBProvider extends EntityProvider<NodeDescription, String> {

    /**
     * Retrieves a list of node descriptions filtered by the specified node flavor.
     *
     * @param flavor the node flavor used to filter the nodes; it specifies resources
     *               such as CPU, memory, and disk capacity.
     * @return a list of {@code NodeDescription} objects that match the specified flavor.
     */
    List<NodeDescription> getByFlavor(NodeFlavor flavor);
    /**
     * Retrieves a list of node descriptions for a specific user and node flavor.
     *
     * @param userId the identifier of the user whose nodes are to be retrieved
     * @param flavorId the identifier of the node flavor used to filter the nodes
     * @return a list of NodeDescription objects that match the specified user and flavor criteria
     */
    List<NodeDescription> getByUserWithFlavor(String userId, String flavorId);
    /**
     * Retrieves a list of node descriptions, with an option to filter by their active status.
     *
     * @param active a boolean flag indicating whether to filter nodes by their active status.
     *               If true, only active nodes will be included; if false, only inactive nodes will be included.
     * @return a list of NodeDescription objects that match the specified active status.
     */
    List<NodeDescription> list(boolean active);
    /**
     * Retrieves a service description based on the provided service name and version.
     *
     * @param name the name of the service to retrieve
     * @param version the version of the service to retrieve
     * @return the {@code ServiceDescription} object matching the specified name and version,
     *         or {@code null} if no such service exists
     */
    ServiceDescription getServiceDescription(String name, String version);
    /**
     * Saves the provided service description in the persistence layer.
     *
     * @param service the {@code ServiceDescription} object containing the details of the service to be saved.
     *                It must include attributes such as name, version, and description.
     * @return the saved {@code ServiceDescription} object, potentially including additional
     *         information such as the generated ID from the persistence layer.
     * @throws PersistenceException if an error occurs while trying to save the service description.
     */
    ServiceDescription saveServiceDescription(ServiceDescription service) throws PersistenceException;
    /**
     * Counts the number of usable nodes for a given user.
     *
     * @param userId the identifier of the user for whom usable nodes are to be counted
     * @return the number of usable nodes associated with the specified user
     */
    int countUsableNodes(String userId);
    /**
     * Updates the modified date of the node identified by the given node ID.
     *
     * @param nodeId the identifier of the node whose modified date needs to be updated
     * @return an integer indicating the outcome of the update operation
     */
    int updateModifiedDate(String nodeId);
}
