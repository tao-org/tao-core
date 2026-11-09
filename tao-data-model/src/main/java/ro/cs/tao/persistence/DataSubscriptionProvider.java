package ro.cs.tao.persistence;

import ro.cs.tao.subscription.DataSubscription;

import java.util.List;

/**
 * Interface for managing dataset subscriptions in a persistence layer.
 * Provides methods for retrieving, creating, and manipulating {@link DataSubscription} entities,
 * with specific query capabilities based on user, workspace, or subscription attributes.
 */
public interface DataSubscriptionProvider extends EntityProvider<DataSubscription, Long> {

    /**
     * Retrieves a list of {@link DataSubscription} entities associated with the specified user.
     *
     * @param userId the unique identifier of the user whose data subscriptions are to be retrieved
     * @return a list of {@link DataSubscription} instances associated with the given userId,
     *         or an empty list if no subscriptions are found
     */
    List<DataSubscription> getByUser(String userId);

    /**
     * Retrieves a list of {@link DataSubscription} entities associated with the specified user
     * and workspace repository.
     *
     * @param userId the unique identifier of the user whose data subscriptions are to be retrieved
     * @param repositoryId the unique identifier of the workspace repository within which to search for data subscriptions
     * @return a list of {@link DataSubscription} instances associated with the given userId and repositoryId,
     *         or an empty list if no subscriptions are found
     */
    List<DataSubscription> getByUserAndWorkspace(String userId, String repositoryId);

    /**
     * Retrieves the {@link DataSubscription} associated with the specified user, repository,
     * and data root path.
     *
     * @param userId the unique identifier of the user
     * @param repositoryId the unique identifier of the repository
     * @param path the path of the data root associated with the subscription
     * @return the {@link DataSubscription} that matches the specified userId, repositoryId, and path,
     *         or null if no matching subscription is found
     */
    DataSubscription get(String userId, String repositoryId, String path);

    /**
     * Retrieves the {@link DataSubscription} associated with the specified user and subscription name.
     *
     * @param userId the unique identifier of the user
     * @param name the name of the subscription
     * @return the {@link DataSubscription} that matches the specified userId and name,
     *         or null if no matching subscription is found
     */
    DataSubscription get(String userId, String name);

    /**
     * Retrieves the {@link DataSubscription} entity associated with the specified checksum.
     *
     * @param checkSum the checksum used to uniquely identify a {@link DataSubscription}
     * @return the {@link DataSubscription} entity that matches the given checksum,
     *         or null if no matching subscription is found
     */
    DataSubscription get(String checkSum);

    /**
     * Retrieves a list of {@link DataSubscription} entities that are subscribers of the specified user.
     *
     * @param userId the unique identifier of the user whose subscribers are to be retrieved
     * @return a list of {@link DataSubscription} instances representing the subscribers of the given userId,
     *         or an empty list if no subscribers are found
     */
    List<DataSubscription> getSubscribersFor(String userId);

    /**
     * Retrieves a list of {@link DataSubscription} entities that are unchecked.
     *
     * @return a list of {@link DataSubscription} instances that have not been checked,
     *         or an empty list if no such subscriptions are found
     */
    List<DataSubscription> getUnckecked();
}
