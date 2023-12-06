package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import ro.cs.tao.eodata.DataSubscription;

import java.util.List;

public interface DataSubscriptionRepository extends PagingAndSortingRepository<DataSubscription, Long> {

    List<DataSubscription> findByUserId(String userId);

    @Query(value = "SELECT * FROM subscription.dataset WHERE data_root_path LIKE CONCAT('%',CAST(:userId AS varchar),'%') ORDER by created ASC")
    List<DataSubscription> getSubscribersFor(@Param("userId") String userId);
}
