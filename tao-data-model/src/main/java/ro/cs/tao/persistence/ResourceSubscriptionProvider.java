package ro.cs.tao.persistence;

import ro.cs.tao.subscription.ResourceSubscription;

import java.util.List;

/**
 * Provides subscription management functionalities specific to the {@link ResourceSubscription} entity.
 * This interface extends {@link EntityProvider}, inheriting common methods for managing entities.
 * It defines additional methods tailored to handle user-specific and status-based subscription operations.
 *
 * @see ResourceSubscription
 * @see EntityProvider
 */
public interface ResourceSubscriptionProvider extends EntityProvider<ResourceSubscription, Long> {

    /**
     * Retrieves the active (open) subscription associated with the specified user.
     * An open subscription is one that has not been ended or terminated.
     *
     * @param userId the unique identifier of the user whose open subscription is requested
     * @return the user's active open subscription, or null if no such subscription exists or the user has no active subscriptions
     */
    ResourceSubscription getUserOpenSubscription(String userId);

    /**
     * Retrieves the list of subscriptions that have ended for a specified user.
     *
     * @param userId the unique identifier of the user whose ended subscriptions are to be retrieved
     * @return a list of {@code ResourceSubscription} objects representing the ended subscriptions for the user,
     *         or an empty list if the user has no ended subscriptions
     */
    List<ResourceSubscription> getEndedSubscriptionsByUser(String userId);

}
