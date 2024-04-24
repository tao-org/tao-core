package ro.cs.tao.persistence;

import ro.cs.tao.subscription.ResourceSubscription;

import java.util.List;

public interface ResourceSubscriptionProvider extends EntityProvider<ResourceSubscription, Long> {

    ResourceSubscription getUserOpenSubscription(String userId);

    List<ResourceSubscription> getEndedSubscriptionsByUser(String userId);

}
