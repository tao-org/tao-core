package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
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
    /**
     * Find WorkflowDescriptor entity by its given identifier
     * @param id - the given workflow identifier
     * @return the corresponding WorkflowDescriptor entity
     */
    WorkflowDescriptor findById(Long id);

    @Query(value = "SELECT * from tao.workflow_graph WHERE username = :user AND status_id = :statusId " +
            "ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getUserWorkflowsByStatus(String user, int statusId);

    @Query(value = "SELECT * from tao.workflow_graph WHERE username = :user AND visibility_id = :visibilityId" +
            "ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getUserPublishedWorkflowsByVisibility(String user, int visibilityId);

    @Query(value = "SELECT * from tao.workflow_graph WHERE username != :user AND visibility_id = 1 " +
            "AND status_id = 3 ORDER BY created DESC", nativeQuery = true)
    List<WorkflowDescriptor> getOtherPublicWorkflows(String user);
}
