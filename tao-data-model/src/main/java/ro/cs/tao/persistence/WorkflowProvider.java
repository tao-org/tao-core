package ro.cs.tao.persistence;

import ro.cs.tao.workflow.WorkflowDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    Map<Long, String> getWorkflowImages(Set<Long> ids);
    String getWorkflowImage(long id);
    void addWorkflowImage(long id, String image);
    void updateWorkflowImage(long id, String newImage);
    void deleteWorkflowImage(long id);
}
