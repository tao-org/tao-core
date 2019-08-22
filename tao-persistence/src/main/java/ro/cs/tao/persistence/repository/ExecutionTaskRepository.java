package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.ExecutionTask;

import java.util.List;

/**
 * CRUD repository for ExecutionTask entities
 *
 * @author oana
 */
@Repository
@Qualifier(value = "executionTaskRepository")
@Transactional
public interface ExecutionTaskRepository extends PagingAndSortingRepository<ExecutionTask, Long> {

    /**
     * Find ExecutionTask entity by its resource identifier
     *
     * @param resourceId - the given task resource identifier
     * @return the corresponding ExecutionTask entity
     */
    ExecutionTask findByResourceId(String resourceId);

    @Query(value = "SELECT * from execution.task where job_id = :jobId and graph_node_id = :nodeId and instance_id = :instanceId", nativeQuery = true)
    ExecutionTask findByJobAndWorkflowNode(@Param("jobId") long jobId,
                                           @Param("nodeId") long nodeId,
                                           @Param("instanceId") int instanceId);

    @Query(value = "SELECT * from execution.task where task_group_id = :groupId and graph_node_id = :nodeId and instance_id = :instanceId", nativeQuery = true)
    ExecutionTask findByGroupAndWorkflowNode(@Param("groupId") long groupId,
                                             @Param("nodeId") long nodeId,
                                             @Param("instanceId") int instanceId);

    @Query(value = "SELECT * from execution.task where execution_status_id = 2", nativeQuery = true)
    List<ExecutionTask> getRunningTasks();

    @Query(value = "SELECT * FROM execution.task WHERE discriminator = 11 AND execution_status_id = 2 AND resource_id IS NOT NULL", nativeQuery = true)
    List<ExecutionTask> getExecutingTasks();

    @Query(value = "SELECT COALESCE(SUM(t.used_cpu), 0) FROM execution.task as t JOIN execution.job AS j ON j.id = t.job_id WHERE t.execution_status_id IN (1, 2) AND j.username = :userName", nativeQuery = true)
    int getCPUsForUser(@Param("userName") String userName);

    @Query(value = "SELECT COALESCE(SUM(t.used_ram), 0) FROM execution.task as t JOIN execution.job AS j ON j.id = t.job_id WHERE t.execution_status_id IN (1, 2) AND j.username = :userName", nativeQuery = true)
    int getMemoryForUser(@Param("userName") String userName);
    
}
