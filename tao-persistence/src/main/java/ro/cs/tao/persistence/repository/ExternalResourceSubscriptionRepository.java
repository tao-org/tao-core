package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.subscription.ExternalResourceSubscription;

@Repository
@Qualifier(value = "externalResourceSubscriptionRepository")
@Transactional
public interface ExternalResourceSubscriptionRepository extends PagingAndSortingRepository<ExternalResourceSubscription, Long> {

    @Query(value = "SELECT * FROM subscription.external_resource_subscription WHERE name = :name ", nativeQuery = true)
    ExternalResourceSubscription getSubscriptionByName(@Param("name") String name);

//    /**
//     * The delete* methods are overridden because subscriptions cannot be deleted
//     */
//
//    @Override
//    default void deleteById(Long aLong) { }
//
//    @Override
//    default void delete(ExternalResourceSubscription entity) { }
//
//    @Override
//    default void deleteAllById(Iterable<? extends Long> longs) { }
//
//    @Override
//    default void deleteAll(Iterable<? extends ExternalResourceSubscription> entities) { }
//
//    @Override
//    default void deleteAll() { }
}
