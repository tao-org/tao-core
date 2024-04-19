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

import java.time.LocalDateTime;
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
        final VolatileInstance instance = get(id);
        if (instance != null && instance.getDestroyed() == null) {
            delete(instance);
        }
    }

    @Override
    public void delete(VolatileInstance entity) throws PersistenceException {
        entity.setDestroyed(LocalDateTime.now());
        update(entity);
    }

    @Override
    public void delete(Iterable<Long> ids) throws PersistenceException {
        if (ids != null) {
            for (Long id : ids) {
                delete(id);
            }
        }
    }

    @Override
    public void deleteByNode(String nodeId) throws PersistenceException {
        final List<VolatileInstance> instances = repository.getByNodeId(nodeId);
        if (instances != null) {
            for (VolatileInstance instance : instances) {
                delete(instance);
            }
        }
    }

    @Override
    public void deleteByUser(String userId) throws PersistenceException {
        final List<VolatileInstance> instances = repository.findByUserId(userId);
        if (instances != null) {
            for (VolatileInstance instance : instances) {
                delete(instance);
            }
        }
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
