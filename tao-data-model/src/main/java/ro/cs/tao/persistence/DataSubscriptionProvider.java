package ro.cs.tao.persistence;

import ro.cs.tao.subscription.DataSubscription;

import java.util.List;

public interface DataSubscriptionProvider extends EntityProvider<DataSubscription, Long> {

    List<DataSubscription> getByUser(String userId);

    List<DataSubscription> getSubscribersFor(String userId);
}
