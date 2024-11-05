package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.ExternalResourceSubscriptionProvider;
import ro.cs.tao.persistence.repository.ExternalResourceSubscriptionRepository;
import ro.cs.tao.subscription.ExternalResourceSubscription;
import ro.cs.tao.utils.StringUtilities;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("externalResourceSubscriptionManager")
public class ExternalResourceSubscriptionManager extends EntityManager<ExternalResourceSubscription, Long, ExternalResourceSubscriptionRepository>
                                            implements ExternalResourceSubscriptionProvider {

    @Override
    public ExternalResourceSubscription getSubscriptionByName(String name) {
        return repository.getSubscriptionByName(name);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(ExternalResourceSubscription entity) {
        return entity != null && entity.getName() != null && (entity.getFlavors() != null || entity.getObjectStorageGB() != null);
    }
}
