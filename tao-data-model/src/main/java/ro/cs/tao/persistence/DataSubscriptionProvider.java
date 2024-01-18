package ro.cs.tao.persistence;

import ro.cs.tao.subscription.DataSubscription;

import java.util.List;

public interface DataSubscriptionProvider extends EntityProvider<DataSubscription, Long> {

    List<DataSubscription> getByUser(String userId);

    List<DataSubscription> getByUserAndWorkspace(String userId, String repositoryId);

    DataSubscription get(String userId, String repositoryId, String path);

    DataSubscription get(String userId, String name);

    List<DataSubscription> getSubscribersFor(String userId);
}
