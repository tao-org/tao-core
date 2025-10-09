package ro.cs.tao.persistence;

import ro.cs.tao.workflow.WorkflowDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WorkflowProvider is an interface that extends {@link EntityProvider}, providing additional
 * methods specifically for managing and interacting with {@link WorkflowDescriptor} entities.
 * This interface supports operations such as retrieving workflows by name or node ID,
 * listing workflows based on various criteria, and managing workflow images.
 */
public interface WorkflowProvider extends EntityProvider<WorkflowDescriptor, Long> {

    /**
     * Retrieves the {@link WorkflowDescriptor} associated with the specified name.
     *
     * @param name the unique name identifying the workflow
     * @return the {@link WorkflowDescriptor} matching the given name, or null if no workflow is found
     */
    WorkflowDescriptor getByName(String name);
    /**
     * Loads and retrieves the {@link WorkflowDescriptor} associated with the specified unique identifier.
     *
     * @param identifier the unique identifier of the workflow to be loaded
     * @return the {@link WorkflowDescriptor} corresponding to the given identifier, or null if no workflow is found
     */
    WorkflowDescriptor loadWorkflowDescriptor(long identifier);
    /**
     * Retrieves the {@link WorkflowDescriptor} associated with the specified workflow node ID.
     *
     * @param workflowNodeId the unique identifier of the workflow node
     * @return the {@link WorkflowDescriptor} corresponding to the given node ID,
     *         or null if no matching workflow is found
     */
    WorkflowDescriptor getByNodeId(long workflowNodeId);
    /**
     * Retrieves a list of all active {@link WorkflowDescriptor} instances.
     *
     * @return a list of WorkflowDescriptor objects that are currently active
     */
    List<WorkflowDescriptor> listActive();
    /**
     * Retrieves a list of public {@link WorkflowDescriptor} instances that are visible for general access.
     *
     * @return a list of WorkflowDescriptor objects marked as public
     */
    List<WorkflowDescriptor> listPublic();
    /**
     * Retrieves a list of {@link WorkflowDescriptor} objects associated with a specific user
     * and filtered by the given status identifier.
     *
     * @param user the username of the user whose workflows are to be retrieved
     * @param statusId the identifier of the workflow status to filter by
     * @return a list of {@link WorkflowDescriptor} objects that match the specified user and status
     */
    List<WorkflowDescriptor> listUserWorkflowsByStatus(String user, int statusId);
    /**
     * Retrieves a list of {@link WorkflowDescriptor} objects associated with a specific user
     * and filtered by a specific visibility identifier.
     *
     * @param user the username of the user whose published workflows are to be retrieved
     * @param visibilityId the identifier of the visibility level to filter the workflows by
     * @return a list of {@link WorkflowDescriptor} objects that match the specified user and visibility
     */
    List<WorkflowDescriptor> listUserPublishedWorkflowsByVisibility(String user, int visibilityId);
    /**
     * Retrieves a list of public workflows created by other users that are visible to the specified user.
     *
     * @param user the username of the user for whom the list of other public workflows is retrieved
     * @return a list of {@link WorkflowDescriptor} objects representing public workflows from other users
     */
    List<WorkflowDescriptor> listOtherPublicWorkflows(String user);
    /**
     * Retrieves a list of {@link WorkflowDescriptor} objects that are visible to the specified user.
     *
     * @param user the username of the user for whom the visible workflows are to be retrieved
     * @param active flag to retrieve either active or inactive visible workflows
     * @return a list of {@link WorkflowDescriptor} objects that the user can access
     */
    List<WorkflowDescriptor> listUserVisible(String user, boolean active);

    /**
     * Retrieves a map of workflow image IDs and their corresponding image representations
     * for the specified set of workflow IDs.
     *
     * @param ids a set of unique identifiers representing the workflows whose images
     *            need to be retrieved
     * @return a map where the keys are workflow IDs (as Long) and the values are
     *         the corresponding image representations (as String)
     */
    Map<Long, String> getWorkflowImages(Set<Long> ids);
    /**
     * Retrieves the image representation of a workflow identified by its unique identifier.
     *
     * @param id the unique identifier of the workflow whose image is to be retrieved
     * @return the string representation of the workflow image, or null if no image is found for the specified ID
     */
    String getWorkflowImage(long id);
    /**
     * Associates the specified image with the workflow identified by the given unique ID.
     *
     * @param id the unique identifier of the workflow to which the image will be added
     * @param image the string representation of the image to be associated with the workflow
     */
    void addWorkflowImage(long id, String image);
    /**
     * Updates the image associated with the specified workflow.
     *
     * @param id the unique identifier of the workflow whose image is to be updated
     * @param newImage the new string representation of the image to associate with the workflow
     */
    void updateWorkflowImage(long id, String newImage);
    /**
     * Deletes the image associated with the workflow identified by the given unique identifier.
     *
     * @param id the unique identifier of the workflow whose image is to be deleted
     */
    void deleteWorkflowImage(long id);
}
