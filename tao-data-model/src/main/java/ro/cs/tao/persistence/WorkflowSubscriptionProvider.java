package ro.cs.tao.persistence;

import ro.cs.tao.subscription.WorkflowSubscription;

import java.util.List;

public interface WorkflowSubscriptionProvider extends EntityProvider<WorkflowSubscription, Long> {

    List<WorkflowSubscription> getByUser(String userId);

    List<WorkflowSubscription> getByWorkflow(long workflowId);

    WorkflowSubscription getByUserAndWorkflow(String userId, long workflowId);
}
