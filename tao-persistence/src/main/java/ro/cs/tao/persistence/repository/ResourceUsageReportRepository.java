package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.ResourceUsageReport;

@org.springframework.stereotype.Repository
@Qualifier(value = "resourceUsageReportRepository")
@Transactional
public interface ResourceUsageReportRepository extends PagingAndSortingRepository<ResourceUsageReport, Long> {

    @Query(value = "SELECT * FROM execution.resource_usage_report WHERE user_id = :userId", nativeQuery = true)
    ResourceUsageReport getByUserId(@Param("userId") String userId);

}
