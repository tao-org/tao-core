package ro.cs.tao.persistence;

import ro.cs.tao.subscription.ExternalResourceSubscription;

public interface ExternalResourceSubscriptionProvider extends EntityProvider<ExternalResourceSubscription, Long> {

    ExternalResourceSubscription getSubscriptionByName(String name);


}
