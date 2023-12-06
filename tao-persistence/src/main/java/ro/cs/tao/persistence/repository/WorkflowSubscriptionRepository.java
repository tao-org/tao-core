package ro.cs.tao.persistence.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import ro.cs.tao.workflow.WorkflowSubscription;

import java.util.List;

public interface WorkflowSubscriptionRepository extends PagingAndSortingRepository<WorkflowSubscription, Long> {

    List<WorkflowSubscription> findByUserId(String userId);

    List<WorkflowSubscription> findByWorkflowId(long workflowId);

    WorkflowSubscription findByUserIdAndWorkflowId(String userId, long workflowId);

}
