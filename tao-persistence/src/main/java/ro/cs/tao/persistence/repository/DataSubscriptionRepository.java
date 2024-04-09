package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.subscription.DataSubscription;

import java.util.List;

@Repository
@Qualifier(value = "dataSubscriptionRepository")
@Transactional
public interface DataSubscriptionRepository extends PagingAndSortingRepository<DataSubscription, Long> {

    List<DataSubscription> findByUserId(String userId);

    List<DataSubscription> findByUserIdAndRepositoryId(String userId, String repositoryId);

    @Query(value = "SELECT * FROM subscription.dataset WHERE user_id = :userId AND repository_id = :repositoryId and data_root_path = :path",
            nativeQuery = true)
    DataSubscription get(@Param("userId") String userId, @Param("repositoryId") String repositoryId, @Param("path") String path);

    @Query(value = "SELECT * FROM subscription.dataset WHERE user_id = :userId AND name = :name", nativeQuery = true)
    DataSubscription get(@Param("userId") String userId, @Param("name") String name);

    @Query(value = "SELECT * FROM subscription.dataset WHERE checksum = :checkSum", nativeQuery = true)
    DataSubscription get(@Param("checkSum") String checkSum);

    @Query(value = "SELECT * FROM subscription.dataset WHERE data_root_path LIKE CONCAT('%',CAST(:userId AS varchar),'%') ORDER by created ASC",
           nativeQuery = true)
    List<DataSubscription> getSubscribersFor(@Param("userId") String userId);

    @Query(value = "SELECT * FROM subscription.dataset WHERE checksum IS NULL", nativeQuery = true)
    List<DataSubscription> getUnchecked();
}
