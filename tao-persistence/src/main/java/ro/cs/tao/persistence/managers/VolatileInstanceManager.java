package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.VolatileInstanceProvider;
import ro.cs.tao.persistence.repository.VolatileInstanceRepository;
import ro.cs.tao.topology.VolatileInstance;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("volatileInstanceManager")
public class VolatileInstanceManager
        extends EntityManager<VolatileInstance, Long, VolatileInstanceRepository>
        implements VolatileInstanceProvider {

    @Override
    public List<VolatileInstance> getUserInstances(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<VolatileInstance> getFlavorInstances(String flavorId) {
        return repository.findByFlavorId(flavorId);
    }

    @Override
    public void delete(Long id) throws PersistenceException {
        throw new PersistenceException("Delete is not allowed");
    }

    @Override
    public void delete(VolatileInstance entity) throws PersistenceException {
        throw new PersistenceException("Delete is not allowed");
    }

    @Override
    public void delete(Iterable<Long> ids) throws PersistenceException {
        throw new PersistenceException("Delete is not allowed");
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(VolatileInstance entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getNodeId())
                && !StringUtilities.isNullOrEmpty(entity.getFlavorId())
                && !StringUtilities.isNullOrEmpty(entity.getUserId())
                && entity.getCreated() != null;
    }
}
