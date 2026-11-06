package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.SavedQueryProvider;
import ro.cs.tao.persistence.repository.SavedQueryRepository;
import ro.cs.tao.userdb.SavedQuery;
import ro.cs.tao.userdb.SavedQueryId;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("savedQueryManager")
public class SavedQueryManager extends EntityManager<SavedQuery, SavedQueryId, SavedQueryRepository>
        implements SavedQueryProvider {
    @Transactional
    @Override
    public List<SavedQuery> getByUserId(String userId) {
        return repository.findByUserIdOrderByNameAsc(userId);
    }

    @Override
    protected String identifier() {
        return "name";
    }

    @Override
    protected boolean checkId(SavedQueryId entityId, boolean existingEntity) {
        return entityId != null
                && !StringUtilities.isNullOrEmpty(entityId.getName())
                && !StringUtilities.isNullOrEmpty(entityId.getUserId())
                && existingEntity == (get(entityId) != null);
    }

    @Override
    protected boolean checkEntity(SavedQuery entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getName())
                && !StringUtilities.isNullOrEmpty(entity.getUserId())
                && !StringUtilities.isNullOrEmpty(entity.getSql());
    }
}
