package ro.cs.tao.persistence;

import ro.cs.tao.workflow.WorkflowDescriptor;

import java.util.List;

public interface WorkflowProvider extends EntityProvider<WorkflowDescriptor, Long> {

    WorkflowDescriptor getByName(String name);
    WorkflowDescriptor loadWorkflowDescriptor(long identifier);
    WorkflowDescriptor getByNodeId(long workflowNodeId);
    List<WorkflowDescriptor> listActive();
    List<WorkflowDescriptor> listPublic();
    List<WorkflowDescriptor> listUserWorkflowsByStatus(String user, int statusId);
    List<WorkflowDescriptor> listUserPublishedWorkflowsByVisibility(String user, int visibilityId);
    List<WorkflowDescriptor> listOtherPublicWorkflows(String user);
    List<WorkflowDescriptor> listUserVisible(String user);

}
