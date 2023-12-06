package ro.cs.tao.persistence;

import ro.cs.tao.eodata.DataSubscription;

import java.util.List;

public interface DataSubscriptionProvider extends EntityProvider<DataSubscription, Long> {

    List<DataSubscription> getByUser(String userId);

    List<DataSubscription> getSubscribersFor(String userId);
}
