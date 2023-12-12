package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.subscription.WorkflowSubscription;

import java.util.List;

@Repository
@Qualifier(value = "workflowSubscriptionRepository")
@Transactional
public interface WorkflowSubscriptionRepository extends PagingAndSortingRepository<WorkflowSubscription, Long> {

    List<WorkflowSubscription> findByUserId(String userId);

    List<WorkflowSubscription> findByWorkflowId(long workflowId);

    WorkflowSubscription findByUserIdAndWorkflowId(String userId, long workflowId);

}
