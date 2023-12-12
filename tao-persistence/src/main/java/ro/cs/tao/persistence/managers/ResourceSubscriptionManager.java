package ro.cs.tao.persistence.managers;

import ro.cs.tao.persistence.ResourceSubscriptionProvider;
import ro.cs.tao.persistence.repository.ResourceSubscriptionRepository;
import ro.cs.tao.subscription.ResourceSubscription;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

public class ResourceSubscriptionManager extends EntityManager<ResourceSubscription, Long, ResourceSubscriptionRepository>
                                            implements ResourceSubscriptionProvider {

    @Override
    public List<ResourceSubscription> getByUser(String userId) {
        return repository.findByUserId(userId);
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
