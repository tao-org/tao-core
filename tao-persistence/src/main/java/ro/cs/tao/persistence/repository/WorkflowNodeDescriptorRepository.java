package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

/**
 * CRUD repository for WorkflowDescriptor entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "workflowNodeDescriptorRepository")
@Transactional
public interface WorkflowNodeDescriptorRepository extends PagingAndSortingRepository<WorkflowNodeDescriptor, Long> {
    /**
     * Find WorkflowNodeDescriptor entity by its given identifier
     * @param id - the given workflow node identifier
     * @return the corresponding WorkflowNodeDescriptor entity
     */
    WorkflowNodeDescriptor findById(Long id);
}
