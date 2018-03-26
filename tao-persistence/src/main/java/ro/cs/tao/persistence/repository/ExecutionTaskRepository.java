package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.ExecutionTask;

/**
 * CRUD repository for ExecutionTask entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "executionTaskRepository")
@Transactional
public interface ExecutionTaskRepository extends PagingAndSortingRepository<ExecutionTask, Long>

    {
        /**
         * Find ExecutionTask entity by its given identifier
         * @param id - the given task identifier
         * @return the corresponding ExecutionTask entity
         */
        ExecutionTask findById(Long id);

        /**
         * Find ExecutionTask entity by its resource identifier
         * @param resourceId - the given task resource identifier
         * @return the corresponding ExecutionTask entity
         */
        ExecutionTask findByResourceId(String resourceId);

        @Query(value = "SELECT * from tao.task where job_id = :jobId and graph_node_id = :nodeId", nativeQuery = true)
        ExecutionTask findByJobAndWorkflowNode(@Param("jobId") long jobId,
                                               @Param("nodeId") long nodeId);

        @Query(value = "SELECT * from tao.task where task_group_id = :groupId and graph_node_id = :nodeId", nativeQuery = true)
        ExecutionTask findByGroupAndWorkflowNode(@Param("groupId") long groupId,
                                                 @Param("nodeId") long nodeId);
}
