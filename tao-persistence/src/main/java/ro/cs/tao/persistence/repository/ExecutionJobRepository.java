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

    @Query(value = "SELECT COUNT(id) FROM execution.job WHERE execution_status_id = :status", nativeQuery = true)
    int countByExecutionStatus(@Param("status") int status);

    @Query(value = "SELECT COUNT(id) FROM execution.job WHERE user_id = :userId AND execution_status_id = :status", nativeQuery = true)
    int countByExecutionStatus(@Param("userId") String userId, @Param("status") int status);

    @Query(value = "SELECT * FROM execution.job WHERE execution_status_id in (:statuses) " +
            "ORDER BY start_time ASC", nativeQuery = true)
    List<ExecutionJob> findByExecutionStatuses(@Param("statuses") Set<Integer> statuses);

    @Query(value = "SELECT * FROM execution.job WHERE user_id = :userId AND execution_status_id in (:statuses) " +
            "ORDER BY end_time DESC, start_time DESC", nativeQuery = true)
    List<ExecutionJob> findByStatusAndUser(@Param("statuses") Set<Integer> statuses, @Param("userId") String userId);

    @Query(value = "SELECT * FROM execution.job WHERE execution_status_id in (:statuses) " +
            "ORDER BY end_time DESC, start_time DESC", nativeQuery = true)
    List<ExecutionJob> findByStatus(@Param("statuses") Set<Integer> statuses);

    @Query(value = "SELECT CONCAT(CAST(t.job_id as text), '-', CAST(t.id as text)) FROM execution.task_output tk " +
            "JOIN execution.task t ON t.id = tk.task_id " +
            "JOIN execution.job e ON e.id = t.job_id " +
            "WHERE e.workflow_id = :workflowId " +
            "ORDER BY t.job_id, t.id", nativeQuery = true)
    List<String> getWorkflowJobsOutputs(@Param("workflowId") Long workflowId);

    @Query(value = "SELECT CONCAT(CAST(t.job_id as text), '-', CAST(t.id as text)) FROM execution.task_output tk " +
            "JOIN execution.task t ON t.id = tk.task_id " +
            "WHERE t.job_id = :jobId " +
            "ORDER BY t.id", nativeQuery = true)
    List<String> getJobOutputs(@Param("jobId") Long jobId);
}
