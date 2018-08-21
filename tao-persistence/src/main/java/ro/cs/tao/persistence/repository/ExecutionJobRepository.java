package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;

import java.util.List;
import java.util.Set;

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

    List<ExecutionJob> findByWorkflowId(long workflowId);

    /**
     * Returns all the entities having the given status
     * @param status    The status to lookup for
     */
    List<ExecutionJob> findByExecutionStatus(ExecutionStatus status);

    @Query(value = "SELECT * FROM tao.job WHERE username = :userName AND execution_status_id in (:statuses)",
            nativeQuery = true)
    List<ExecutionJob> findByStatusAndUser(@Param("statuses") Set<Integer> statuses, @Param("userName") String userName);
}
