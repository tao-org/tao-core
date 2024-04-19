package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.ResourceSubscriptionProvider;
import ro.cs.tao.persistence.repository.ResourceSubscriptionRepository;
import ro.cs.tao.subscription.ResourceSubscription;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("resourceSubscriptionManager")
public class ResourceSubscriptionManager extends EntityManager<ResourceSubscription, Long, ResourceSubscriptionRepository>
                                            implements ResourceSubscriptionProvider {

    @Override
    public List<ResourceSubscription> getByUser(String userId) {
        return repository.getByUser(userId);
    }

    @Override
    public List<ResourceSubscription> getEndedSubscriptionsByUser(String userId) {
        return repository.getEndedSubscriptionsByUser(userId);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(ResourceSubscription entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getUserId())
                && entity.getType() != null && entity.getFlavor() != null && entity.getFlavorQuantity() > 0;
    }
}
