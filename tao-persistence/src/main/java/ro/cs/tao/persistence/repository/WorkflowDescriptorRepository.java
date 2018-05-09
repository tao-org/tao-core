package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.enums.Status;

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

    List<WorkflowDescriptor> findByStatus(Status status);
}
