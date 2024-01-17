package ro.cs.tao.persistence;

import ro.cs.tao.subscription.ResourceSubscription;

import java.util.List;

public interface ResourceSubscriptionProvider extends EntityProvider<ResourceSubscription, Long> {

    List<ResourceSubscription> getByUser(String userId);

    List<ResourceSubscription> getEndedSubscriptionsByUser(String userId);

}
