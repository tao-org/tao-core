package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.subscription.ResourceSubscription;

import java.util.List;

@Repository
@Qualifier(value = "resourceSubscriptionRepository")
@Transactional
public interface ResourceSubscriptionRepository extends PagingAndSortingRepository<ResourceSubscription, Long> {

    List<ResourceSubscription> findByUserId(String userId);

}
