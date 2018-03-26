package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.List;

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

    @Query(value = "SELECT * from tao.graph_node where workflow_id = :workflowId and processing_component_id = :componentId " +
                    "order by node_level", nativeQuery = true)
    List<WorkflowNodeDescriptor> findByComponentId(@Param("workflowId") long workflowId,
                                                   @Param("componentId") String componentId);
}
