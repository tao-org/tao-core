package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.ExecutionTask;

import java.time.LocalDateTime;
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
    @Transactional(readOnly = true)
    List<ExecutionTask> getRunningTasks();

    @Query(value = "SELECT t.* FROM execution.task t JOIN execution.job j ON j.id = t.job_id " +
            "WHERE j.app_id = :appId AND t.discriminator = 11 AND t.execution_status_id = 2 AND t.resource_id IS NOT NULL", nativeQuery = true)
    @Transactional(readOnly = true)
    List<ExecutionTask> getExecutingTasks(@Param("appId") String appId);

    @Query(value = "SELECT * FROM execution.task WHERE discriminator = 33 AND execution_status_id = 2 AND resource_id IS NOT NULL", nativeQuery = true)
    @Transactional(readOnly = true)
    List<ExecutionTask> getWPSExecutingTasks();

    @Query(value = "SELECT * FROM execution.task WHERE discriminator = 34 AND execution_status_id = 2 AND resource_id IS NOT NULL", nativeQuery = true)
    @Transactional(readOnly = true)
    List<ExecutionTask> getWMSExecutingTasks();

    @Query(value = "SELECT * FROM execution.task WHERE execution_status_id IN (1, 2, 7) AND execution_node_host_name = :host", nativeQuery = true)
    @Transactional(readOnly = true)
    List<ExecutionTask> getTasksByHost(@Param("host") String host);

    @Query(value = "SELECT COUNT(id) FROM execution.task WHERE execution_status_id IN (1, 2, 7) AND execution_node_host_name = :host AND (start_time >= :since OR COALESCE(last_updated, start_time) >= :since)", nativeQuery = true)
    @Transactional(readOnly = true)
    int getRunningTaskCountByHostSince(@Param("host") String host, @Param("since") LocalDateTime since);

    @Query(value = "SELECT MAX(COALESCE(end_time, COALESCe(last_updated, start_time))) FROM execution.task WHERE execution_node_host_name = :host", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime getLastRun(@Param("host") String host);

    @Query(value = "SELECT t.* FROM execution.task t JOIN component.data_source_component c ON c.id = t.component_id WHERE t.job_id = :jobId", nativeQuery = true)
    List<ExecutionTask> getDataSourceTasks(@Param("jobId") long jobId);

    @Query(value = "SELECT COALESCE(SUM(t.used_cpu), 0) FROM execution.task as t " +
            "JOIN execution.job AS j ON j.id = t.job_id " +
            "JOIN usr.user u ON u.id = j.user_id " +
            "WHERE t.execution_status_id IN (1, 2) AND u.id = :userId", nativeQuery = true)
    int getCPUsForUser(@Param("userId") String userId);

    @Query(value = "SELECT COALESCE(SUM(t.used_ram), 0) FROM execution.task as t " +
            "JOIN execution.job AS j ON j.id = t.job_id " +
            "JOIN usr.user u ON u.id = j.user_id " +
            "WHERE t.execution_status_id IN (1, 2) AND u.id = :userId", nativeQuery = true)
    int getMemoryForUser(@Param("userId") String userId);

    @Query(value = "with deps as (select id as jid, json_object_keys(task_dependencies) as parentId from execution.job where id = :jobId) " +
            "select count(t.id) from execution.task t where cast(t.id as text) in (" +
            "select json_array_elements_text(json_extract_path(j.task_dependencies, cast(d.parentId as text))) from execution.job j join deps d on j.id = d.jid where cast(d.parentId as integer) = :taskId) " +
            "and not (t.execution_status_id = 4 or (t.execution_status_id = 5 and exists(select id from workflow.graph_node where id = t.graph_node_id and behavior_id = 2)))",
            nativeQuery = true)
    int getRunningParents(@Param("jobId") long jobId, @Param("taskId") long taskId);

    @Modifying
    @Query(value = "INSERT INTO execution.component_time (component_id, average_duration_seconds) VALUES (:id, :duration) " +
            "ON CONFLICT (component_id) DO UPDATE SET average_duration_seconds = (component_time.average_duration_seconds + :duration) / 2", nativeQuery = true)
    void updateComponentTime(@Param("id") String id, @Param("duration") int duration);

    @Query(value = "SELECT COUNT(id) FROM execution.task WHERE job_id = :jobId AND execution_status_id < 4", nativeQuery = true)
    int countRunableTasks(@Param("jobId") long jobId);

    @Query(value = "SELECT COUNT(l.target_graph_node_id) = 0 FROM workflow.component_link l JOIN execution.task t ON t.graph_node_id = l.source_graph_node_id " +
            "WHERE t.id = :taskId", nativeQuery = true)
    boolean isTerminalTask(@Param("taskId") long taskId);

}
