package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;

import java.util.List;

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
     * Finds ExecutionJob entity by its given identifier
     * @param id - the given job identifier
     * @return the corresponding ExecutionJob entity
     */
    ExecutionJob findById(Long id);

    List<ExecutionJob> findByWorkflowId(long workflowId);

    /**
     * Returns all the entities having the given status
     * @param status    The status to lookup for
     */
    List<ExecutionJob> findByExecutionStatus(ExecutionStatus status);
}
