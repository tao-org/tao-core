package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workflow.WorkflowDescriptor;

import java.util.List;

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

    @Query(value = "SELECT * from tao.workflow_graph WHERE username = :user AND status_id = :statusId " +
            "ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getUserWorkflowsByStatus(@Param("user") String user, @Param("statusId") int statusId);

    @Query(value = "SELECT * from tao.workflow_graph WHERE username = :user AND visibility_id = :visibilityId " +
            "ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getUserPublishedWorkflowsByVisibility(@Param("user") String user,
                                                                   @Param("visibilityId") int visibilityId);

    @Query(value = "SELECT * from tao.workflow_graph WHERE username != :user AND visibility_id = 1 " +
            "AND status_id = 3 ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getOtherPublicWorkflows(@Param("user") String user);
}
