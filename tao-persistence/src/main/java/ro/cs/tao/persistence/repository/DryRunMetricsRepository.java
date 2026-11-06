package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.DryRunMetrics;

@Repository
@Qualifier(value = "dryRunMetricsRepository")
@Transactional
public interface DryRunMetricsRepository extends JpaRepository<DryRunMetrics, Long> {

    @Query(value = "SELECT p.* FROM component.component_dry_run_metrics p WHERE p.processing_component_id = :component_id", nativeQuery = true)
    DryRunMetrics getByComponentId(@Param("component_id") String componentId);
}

