package ro.cs.tao.persistence;

import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;

import java.util.List;

/**
 * Provides methods for managing and retrieving {@link Container} entities based on various criteria.
 * This interface extends {@link EntityProvider} to inherit generic CRUD operations and adds specific
 * functionalities for containers.
 */
public interface ContainerProvider extends EntityProvider<Container, String> {

    /**
     * Retrieves a {@link Container} entity by its unique name.
     *
     * @param name the unique name of the container to be retrieved
     * @return the {@link Container} entity that matches the specified name,
     *         or null if no container with the given name exists
     */
    Container getByName(String name);

    /**
     * Retrieves a list of {@link Container} entities filtered by the specified container type.
     *
     * @param type the {@link ContainerType} to filter the containers
     * @return a {@link List} of {@link Container} entities that match the specified type
     */
    List<Container> getByType(ContainerType type);

    /**
     * Retrieves a list of {@link Container} entities that match the specified type and visibility criteria.
     *
     * @param type the {@link ContainerType} used to filter containers by their type
     * @param visibility the {@link ContainerVisibility} used to filter containers by their visibility level
     * @return a {@link List} of {@link Container} entities that match the specified type and visibility,
     *         or an empty list if no containers meet the criteria
     */
    List<Container> getByTypeAndVisibility(ContainerType type, ContainerVisibility visibility);

    /**
     * Retrieves a list of containers that are visible to the specified user.
     *
     * @param userId the unique identifier of the user for whom visible containers are to be retrieved
     * @return a list of {@link Container} entities that are visible to the specified user,
     *         or an empty list if no containers are visible to the user
     */
    List<Container> listContainersVisibleToUser(String userId);

    /**
     * Retrieves a list of {@link Container} entities owned by a specific user.
     *
     * @param userId the unique identifier of the user whose containers are to be retrieved
     * @return a list of {@link Container} entities owned by the specified user,
     *         or an empty list if the user does not own any containers
     */
    List<Container> listUserContainers(String userId);

    /**
     * Cleans up references to a specific container identified by the provided container ID.
     * This operation ensures that all associated references to the container are removed
     * from the system, preventing dangling links or orphaned relationships.
     *
     * @param containerId the unique identifier of the container whose references are to be cleaned
     * @return the number of references that were successfully removed
     */
    int cleanContainerReferences(String containerId);
}
