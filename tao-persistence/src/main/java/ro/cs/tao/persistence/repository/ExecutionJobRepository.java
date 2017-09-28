package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.execution.ExecutionJob;

/**
 * CRUD repository for ExecutionJob entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "executionJobRepository")
@Transactional
public interface ExecutionJobRepository extends PagingAndSortingRepository<ExecutionJob, Long> {
    /**
     * Find ExecutionJob entity by its given identifier
     * @param id - the given job identifier
     * @return the corresponding ExecutionJob entity
     */
    ExecutionJob findById(Long id);

    /**
     * Find ExecutionJob entity by its resource identifier
     * @param resourceId - the given job resource identifier
     * @return the corresponding ExecutionJob entity
     */
    ExecutionJob findByResourceId(String resourceId);
}
