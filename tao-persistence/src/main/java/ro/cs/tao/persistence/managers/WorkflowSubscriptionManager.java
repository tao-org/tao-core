package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.WorkflowSubscriptionProvider;
import ro.cs.tao.persistence.repository.WorkflowSubscriptionRepository;
import ro.cs.tao.subscription.WorkflowSubscription;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("workflowSubscriptionManager")
public class WorkflowSubscriptionManager extends EntityManager<WorkflowSubscription, Long, WorkflowSubscriptionRepository>
                                            implements WorkflowSubscriptionProvider {


    @Override
    public List<WorkflowSubscription> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<WorkflowSubscription> getByWorkflow(long workflowId) {
        return repository.findByWorkflowId(workflowId);
    }

    @Override
    public WorkflowSubscription getByUserAndWorkflow(String userId, long workflowId) {
        return repository.findByUserIdAndWorkflowId(userId, workflowId);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(WorkflowSubscription entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getUserId()) && entity.getWorkflowId() > 0;
    }
}
