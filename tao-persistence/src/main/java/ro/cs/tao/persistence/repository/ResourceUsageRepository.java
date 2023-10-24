package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.ResourceUsage;

import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Repository
@Qualifier(value = "resourceUsageRepository")
@Transactional
public interface ResourceUsageRepository extends PagingAndSortingRepository<ResourceUsage, Long> {

    @Query(value = "SELECT r.* FROM execution.resource_usage r JOIN execution.task t ON t.id = r.task_id " +
            "JOIN execution.job j ON j.id = t.job_id WHERE j.user_id = :userId", nativeQuery = true)
    List<ResourceUsage> getByUserId(@Param("userId") String userId);

    @Query(value = "SELECT r.* FROM execution.resource_usage r JOIN execution.task t ON t.id = r.task_id " +
            "JOIN execution.job j ON j.id = t.job_id WHERE j.user_id = :userId " +
            "AND r.start_time >= :startDate AND r.end_time <= :endDate", nativeQuery = true)
    List<ResourceUsage> getByUserIdAndInterval(@Param("user") String user,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT * FROM execution.resource_usage WHERE task_id = :taskId", nativeQuery = true)
    ResourceUsage getByTask(@Param("taskId") long taskId);
}
