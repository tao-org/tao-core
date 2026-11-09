package ro.cs.tao.persistence;

import ro.cs.tao.subscription.ExternalResourceSubscription;

/**
 * Provides a contract for managing and retrieving {@link ExternalResourceSubscription} entities.
 * This interface extends {@link EntityProvider} to leverage generic CRUD operations while adding
 * a specialized method to retrieve subscriptions by name.
 */
public interface ExternalResourceSubscriptionProvider extends EntityProvider<ExternalResourceSubscription, Long> {

    /**
     * Retrieves an {@link ExternalResourceSubscription} entity by its name.
     *
     * @param name the name of the subscription to retrieve
     * @return the {@link ExternalResourceSubscription} instance matching the specified name,
     *         or null if no subscription with the given name exists
     */
    ExternalResourceSubscription getSubscriptionByName(String name);


}
