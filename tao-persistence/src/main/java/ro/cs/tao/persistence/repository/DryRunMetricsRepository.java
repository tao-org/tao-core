package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.DryRunMetrics;

@Repository
@Qualifier(value = "dryRunMetricsRepository")
@Transactional
public interface DryRunMetricsRepository extends PagingAndSortingRepository<DryRunMetrics, Long> {
}
