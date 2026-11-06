package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.AdminKeyProvider;
import ro.cs.tao.persistence.repository.AdminKeyRepository;
import ro.cs.tao.topology.AdminKey;
import ro.cs.tao.topology.AdminKeyId;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("adminKeyManager")
public class AdminKeyManager extends EntityManager<AdminKey, AdminKeyId, AdminKeyRepository>
        implements AdminKeyProvider {

    @Transactional
    public List<AdminKey> getByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Transactional
    public List<AdminKey> getByNodeId(String nodeId) {
        return repository.findByNodeId(nodeId);
    }

    @Override
    protected String identifier() {
        return "userId";
    }

    @Override
    protected boolean checkId(AdminKeyId entityId, boolean existingEntity) {
        return entityId != null
                && !StringUtilities.isNullOrEmpty(entityId.getUserId())
                && !StringUtilities.isNullOrEmpty(entityId.getNodeId())
                && existingEntity == (get(entityId) != null);
    }

    @Override
    protected boolean checkEntity(AdminKey entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getUserId())
                && !StringUtilities.isNullOrEmpty(entity.getNodeId())
                && !StringUtilities.isNullOrEmpty(entity.getSshKey());
    }
}
