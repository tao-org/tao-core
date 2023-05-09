package ro.cs.tao.persistence;

import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.util.List;

public interface WorkflowNodeProvider extends EntityProvider<WorkflowNodeDescriptor, Long> {

    List<WorkflowNodeDescriptor> listByComponentId(long workflowId, String componentId);
    WorkflowNodeGroupDescriptor getGroupNode(long childNodeId);
    WorkflowNodeDescriptor save(WorkflowNodeDescriptor node, WorkflowDescriptor workflow) throws PersistenceException;
    void updatePosition(Long id, float[] coordinates) throws PersistenceException;
    WorkflowNodeDescriptor findClonedNode(long workflowId, long originalNodeId);

}
