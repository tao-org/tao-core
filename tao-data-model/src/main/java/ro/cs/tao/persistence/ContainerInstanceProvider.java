package ro.cs.tao.persistence;

import ro.cs.tao.docker.ContainerInstance;

import java.util.List;

/**
 * Provides an interface for managing and accessing {@link ContainerInstance} entities.
 * This interface extends {@link EntityProvider} and introduces methods specific
 * to querying container instances based on container IDs, user IDs, and port allocations.
 */
public interface ContainerInstanceProvider extends EntityProvider<ContainerInstance, String> {

    /**
     * Retrieves a list of {@link ContainerInstance} objects associated with the specified container ID.
     *
     * @param containerId the identifier of the container for which instances are to be retrieved
     * @return a list of {@link ContainerInstance} objects associated with the specified container ID
     */
    List<ContainerInstance> getByContainerId(String containerId);
    /**
     * Retrieves a list of {@link ContainerInstance} objects associated with the specified user ID.
     *
     * @param userId the identifier of the user whose container instances are to be retrieved
     * @return a list of {@link ContainerInstance} objects associated with the specified user ID
     */
    List<ContainerInstance> getByUserId(String userId);
    /**
     * Retrieves a {@link ContainerInstance} associated with the given container ID and user ID.
     *
     * @param containerId the identifier of the container
     * @param userId the identifier of the user
     * @return the {@link ContainerInstance} associated with the specified container ID and user ID,
     *         or null if no matching instance exists
     */
    ContainerInstance getByContainerIdAndUserId(String containerId, String userId);
    /**
     * Retrieves a list of currently allocated port numbers.
     *
     * @return a list of integers representing the port numbers that are currently allocated
     */
    List<Integer> getAllocatedPorts();
}
