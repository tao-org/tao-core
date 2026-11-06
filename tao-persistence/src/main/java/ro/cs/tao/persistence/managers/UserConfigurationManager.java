package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.configuration.UserConfigurationItem;
import ro.cs.tao.configuration.UserConfigurationItemId;
import ro.cs.tao.configuration.UserPersistentConfigurationProvider;
import ro.cs.tao.persistence.repository.UserConfigurationRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@Component("userConfigurationManager")
public class UserConfigurationManager
        extends EntityManager<UserConfigurationItem, UserConfigurationItemId, UserConfigurationRepository>
        implements UserPersistentConfigurationProvider {

    @Override
    protected String identifier() {
        return "key";
    }

    @Override
    protected boolean checkEntity(UserConfigurationItem entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getUserId())
                && !StringUtilities.isNullOrEmpty(entity.getKey());
    }

    @Override
    protected boolean checkId(UserConfigurationItemId entityId, boolean existingEntity) {
        return entityId != null
                && !StringUtilities.isNullOrEmpty(entityId.getUserId())
                && !StringUtilities.isNullOrEmpty(entityId.getKey())
                && existingEntity == (get(entityId) != null);
    }

    @Override
    public List<UserConfigurationItem> getItems(String userId) {
        return repository.getByUser(userId);
    }

    @Override
    public List<UserConfigurationItem> getItems(String userId, String filter) {
        return repository.getByUserStartingWith(userId, filter);
    }

    @Override
    public UserConfigurationItem getItem(String userId, String key) {
        return repository.getItem(userId, key);
    }

    @Override
    public UserConfigurationItem saveItem(UserConfigurationItem item) {
        return repository.saveAndFlush(item);
    }

    @Override
    public List<UserConfigurationItem> saveItems(List<UserConfigurationItem> items) {
        return repository.saveAll(items);
    }
}
