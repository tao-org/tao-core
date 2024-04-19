package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workflow.WorkflowDescriptor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CRUD repository for WorkflowDescriptor entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "workflowDescriptorRepository")
@Transactional
public interface WorkflowDescriptorRepository extends PagingAndSortingRepository<WorkflowDescriptor, Long> {

    @Query(value = "SELECT w FROM WorkflowDescriptor w JOIN FETCH w.nodes n " +
            "LEFT JOIN FETCH n.incomingLinks l " +
            "WHERE w.id = :id")
    WorkflowDescriptor getDetailById(@Param("id") Long id);

    Optional<WorkflowDescriptor> findByName(String name);

    @Query(value = "SELECT * from workflow.graph g JOIN workflow.graph_node n ON n.workflow_id = g.id WHERE n.id = :nodeId",
            nativeQuery = true)
    WorkflowDescriptor getByNodeId(@Param("nodeId") long nodeId);

    @Query(value = "SELECT * from workflow.graph WHERE user_id = :userId AND status_id = :statusId " +
            "ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getUserWorkflowsByStatus(@Param("userId") String userId, @Param("statusId") int statusId);

    @Query(value = "SELECT * from workflow.graph WHERE user_id = :userId AND visibility_id = :visibilityId " +
            "ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getUserPublishedWorkflowsByVisibility(@Param("userId") String userId,
                                                                   @Param("visibilityId") int visibilityId);

    @Query(value = "SELECT * from workflow.graph WHERE user_id != :userId AND visibility_id = 1 " +
            "AND status_id = 3 ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getOtherPublicWorkflows(@Param("userId") String userId);

    @Query(value = "select w.* from workflow.graph w join usr.user u on u.id = w.user_id join usr.user_group g on g.user_id = u.id " +
            "where g.group_id = 1 and w.status_id = 3 and w.visibility_id = 1 order by w.name ", nativeQuery = true)
    List<WorkflowDescriptor> getPublicWorkflows();

    @Query(value = "SELECT * from workflow.graph WHERE user_id = :userId ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getUserWorkflows(@Param("userId") String userId);

    @Query(value = "SELECT g.* from workflow.graph g JOIN subscription.workflow s on s.workflow_id = g.id WHERE s.user_id = :userId ORDER BY g.name DESC", nativeQuery = true)
    List<WorkflowDescriptor> getSubscribedWorkflows(@Param("userId") String userId);

    @Query(value = "SELECT workflow_id, image from workflow.graph_image WHERE workflow_id IN (:ids)", nativeQuery = true)
    List<Object[]> getWorkflowImages(@Param("ids") Set<Long> ids);

    @Query(value = "SELECT image from workflow.graph_image WHERE workflow_id = :id", nativeQuery = true)
    String getWorkflowImage(@Param("id") long id);

    @Modifying
    @Query(value = "INSERT INTO workflow.graph_image (workflow_id, image) VALUES (:id, :image)", nativeQuery = true)
    void insertImage(@Param("id") long id, @Param("image") String image);

    @Modifying
    @Query(value = "UPDATE workflow.graph_image SET image = :image WHERE workflow_id = :id", nativeQuery = true)
    void updateImage(@Param("id") long id, @Param("image") String image);

    @Modifying
    @Query(value = "DELETE FROM workflow.graph_image WHERE workflow_id = :id", nativeQuery = true)
    void deleteImage(@Param("id") long id);
}
