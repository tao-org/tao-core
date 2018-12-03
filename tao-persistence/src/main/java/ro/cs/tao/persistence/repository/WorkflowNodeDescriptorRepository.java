package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.util.List;
import java.util.Set;

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

    @Query(value = "SELECT * from tao.graph_node where id in (:ids) order by node_level", nativeQuery = true)
    List<WorkflowNodeDescriptor> getWorkflowsById(@Param("ids") Set<Long> ids);

    @Query(value = "SELECT * from tao.graph_node where workflow_id = :workflowId and component_id = :componentId " +
                    "order by node_level", nativeQuery = true)
    List<WorkflowNodeDescriptor> findByComponentId(@Param("workflowId") long workflowId,
                                                   @Param("componentId") String componentId);

    @Query(value = "SELECT n.* FROM tao.graph_node n JOIN tao.graph_node_group_nodes gn ON gn.graph_node_group_id = n.id " +
            "WHERE gn.graph_node_id = :nodeId", nativeQuery = true)
    WorkflowNodeGroupDescriptor getGroupNode(@Param("nodeId") long nodeId);
}
