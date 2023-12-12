package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.DataSubscriptionProvider;
import ro.cs.tao.persistence.repository.DataSubscriptionRepository;
import ro.cs.tao.subscription.DataSubscription;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("dataSubscriptionManager")
public class DataSubscriptionManager extends EntityManager<DataSubscription, Long, DataSubscriptionRepository>
                                        implements DataSubscriptionProvider {
    @Override
    public List<DataSubscription> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<DataSubscription> getSubscribersFor(String userId) {
        return repository.getSubscribersFor(userId);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(DataSubscription entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getName())
                && !StringUtilities.isNullOrEmpty(entity.getUserId())
                && !StringUtilities.isNullOrEmpty(entity.getDataRootPath());
    }
}
