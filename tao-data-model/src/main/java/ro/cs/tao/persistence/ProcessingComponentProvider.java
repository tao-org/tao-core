package ro.cs.tao.persistence;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ComponentCategory;

import java.util.List;
import java.util.Set;

/**
 * Provides an interface for managing instances of {@link ProcessingComponent}. This interface
 * extends {@code EntityProvider} to include additional operations specific to processing components,
 * such as retrieving components by label or container, listing user-specific components,
 * and managing component modification timestamps.
 */
public interface ProcessingComponentProvider extends EntityProvider<ProcessingComponent, String> {

    /**
     * Retrieves a {@link ProcessingComponent} by its unique identifier and the identifier of its associated container.
     *
     * @param id the unique identifier of the {@link ProcessingComponent} to retrieve
     * @param containerId the unique identifier of the container associated with the {@link ProcessingComponent}
     * @return the {@link ProcessingComponent} matching the provided identifiers, or {@code null} if no such component exists
     */
    ProcessingComponent get(String id, String containerId);
    /**
     * Retrieves a {@link ProcessingComponent} by its label and the identifier of
     * its associated container.
     *
     * @param label the label of the {@link ProcessingComponent} to retrieve
     * @param containerId the unique identifier of the container associated with the {@link ProcessingComponent}
     * @return the {@link ProcessingComponent} matching the provided label and container identifier,
     *         or {@code null} if no such component exists
     */
    ProcessingComponent getByLabel(String label, String containerId);
    /**
     * Retrieves a paginated and sorted list of {@link ProcessingComponent} instances.
     *
     * @param pageNumber the number of the page to retrieve, where the first page is 0
     * @param pageSize the number of {@link ProcessingComponent} instances per page
     * @param sort the sorting criteria for the list of {@link ProcessingComponent} instances
     * @return a list of {@link ProcessingComponent} instances corresponding to the specified page and sorting criteria
     */

    /**
     * Retrieves a paginated and sorted list of {@link ProcessingComponent} instances whose labels contain
     * the provided value.
     *
     * @param label the label fragment used to filter components
     * @param pageNumber the number of the page to retrieve
     * @param pageSize the number of {@link ProcessingComponent} instances per page
     * @param sort the sorting criteria for the list of {@link ProcessingComponent} instances
     * @return a list of {@link ProcessingComponent} instances matching the label filter and pagination criteria
     */

    /**
     * Retrieves a paginated and sorted list of {@link ProcessingComponent} instances having at least one
     * of the provided tags.
     *
     * @param tags the tags used to filter components
     * @param pageNumber the number of the page to retrieve
     * @param pageSize the number of {@link ProcessingComponent} instances per page
     * @param sort the sorting criteria for the list of {@link ProcessingComponent} instances
     * @return a list of {@link ProcessingComponent} instances matching at least one provided tag
     */

    /**
     * Retrieves a paginated and sorted list of {@link ProcessingComponent} instances whose labels contain
     * the provided value and that have at least one of the provided tags.
     *
     * @param label the label fragment used to filter components
     * @param tags the tags used to filter components
     * @param pageNumber the number of the page to retrieve
     * @param pageSize the number of {@link ProcessingComponent} instances per page
     * @param sort the sorting criteria for the list of {@link ProcessingComponent} instances
     * @return a list of {@link ProcessingComponent} instances matching the label and tag filters
     */

    /**
     * Counts all registered processing components.
     *
     * @return the total number of processing components
     */
    /**
     * Lists the processing components associated with a specific user.
     *
     * @param userName the name of the user whose processing components are to be listed
     * @return a list of {@link ProcessingComponent} instances associated with the specified user,
     *         or an empty list if no components are found
     */
    List<ProcessingComponent> listUserProcessingComponents(String userName);
    /**
     * Lists the script components associated with a specific user.
     *
     * @param userName the name of the user whose script components are to be listed
     * @return a list of {@link ProcessingComponent} instances associated with the specified user,
     *         or an empty list if no script components are found
     */
    List<ProcessingComponent> listUserScriptComponents(String userName);
    /**
     * Retrieves a list of {@link ProcessingComponent} instances whose identifiers are not included
     * in the specified set of IDs.
     *
     * @param ids a set of identifiers to be excluded from the result list
     * @return a list of {@link ProcessingComponent} instances that are not included in the provided set of IDs,
     *         or an empty list if no matching components are found
     */
    List<ProcessingComponent> listOtherComponents(Set<String> ids);
    /**
     * Retrieves a list of {@link ProcessingComponent} instances that are associated with a specific label.
     *
     * @param label the label of the {@link ProcessingComponent} instances to retrieve
     * @return a list of {@link ProcessingComponent} instances matching the specified label,
     *         or an empty list if no components with the given label are found
     */
    List<ProcessingComponent> listByLabel(String label);
    /**
     * Retrieves a list of {@link ProcessingComponent} instances associated with the specified container.
     *
     * @param containerId the unique identifier of the container whose associated {@link ProcessingComponent}
     *                    instances are to be retrieved
     * @return a list of {@link ProcessingComponent} instances associated with the specified container,
     *         or an empty list if no such components are found
     */
    List<ProcessingComponent> listByContainer(String containerId);

    List<ProcessingComponent> listByCategory(ComponentCategory category);
    /**
     * Finds a list of {@link ProcessingComponent} instances based on their similarity to a given text
     * and their association with a specific user.
     *
     * @param text the text used as a basis for determining similarity
     * @param userId the identifier of the user to filter the associated components
     * @return a list of {@link ProcessingComponent} instances that are similar to the provided text
     *         and associated with the specified user, or an empty list if no matching components are found
     */
    List<ProcessingComponent> findBySimilarity(String text, String userId);
    /**
     * Finds a list of labels based on their similarity to a given text
     * and their association with a specific user.
     *
     * @param text the text used as a basis for determining similarity
     * @param userId the identifier of the user to filter the associated labels
     * @return a list of labels that are similar to the provided text
     *         and associated with the specified user, or an empty list if no matching labels are found
     */
    List<String> findLabelsBySimilarity(String text, String userId);
    /**
     * Checks whether a copy component is associated with the specified container.
     *
     * @param containerId the unique identifier of the container to check for an associated copy component
     * @return {@code true} if a copy component exists for the specified container, {@code false} otherwise
     */
    boolean hasCopyComponent(String containerId);
    /**
     * Determines if a move component is associated with the specified container.
     *
     * @param containerId the unique identifier of the container to check for an associated move component
     * @return {@code true} if a move component is associated with the specified container, {@code false} otherwise
     */
    boolean hasMoveComponent(String containerId);
    /**
     * Checks whether a delete component is associated with the specified container.
     *
     * @param containerId the unique identifier of the container to check for an associated delete component
     * @return {@code true} if a delete component exists for the specified container, {@code false} otherwise
     */
    boolean hasDeleteComponent(String containerId);
    /**
     * Updates the modified date of a specified processing component.
     *
     * @param processingComponentId the unique identifier of the processing component whose modified date needs to be updated
     * @return the number of records affected by the update operation
     */
    int updateModifiedDate(String processingComponentId);
}
