package ro.cs.tao.persistence;

import ro.cs.tao.subscription.WorkflowSubscription;

import java.util.List;

/**
 * Provides an interface for managing {@link WorkflowSubscription} entities uniquely identified
 * by a {@link Long} identifier. This interface extends the {@link EntityProvider} to
 * include workflow subscription-specific methods for retrieval based on user, workflow,
 * or a combination of both.
 *
 * The {@link WorkflowSubscriptionProvider} is designed to abstract the persistence
 * and retrieval logic, enabling various operations on workflow subscriptions while
 * maintaining compliance with the underlying domain model.
 *
 * @see EntityProvider
 * @see WorkflowSubscription
 */
public interface WorkflowSubscriptionProvider extends EntityProvider<WorkflowSubscription, Long> {

    /**
     * Retrieves a list of workflow subscriptions associated with the specified user.
     *
     * @param userId the unique identifier of the user whose workflow subscriptions are to be retrieved
     * @return a list of WorkflowSubscription objects associated with the specified user
     */
    List<WorkflowSubscription> getByUser(String userId);

    /**
     * Retrieves a list of workflow subscriptions associated with the specified workflow ID.
     *
     * @param workflowId the unique identifier of the workflow whose subscriptions are to be retrieved
     * @return a list of WorkflowSubscription objects associated with the specified workflow ID
     */
    List<WorkflowSubscription> getByWorkflow(long workflowId);

    /**
     * Retrieves a workflow subscription based on the specified user ID and workflow ID.
     *
     * @param userId the unique identifier of the user associated with the workflow subscription
     * @param workflowId the unique identifier of the workflow associated with the subscription
     * @return the WorkflowSubscription object corresponding to the given user ID and workflow ID,
     *         or null if no matching subscription is found
     */
    WorkflowSubscription getByUserAndWorkflow(String userId, long workflowId);
}
