package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.DryRunMetrics;
import ro.cs.tao.persistence.DryRunMetricsProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.DryRunMetricsRepository;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
public class DryRunMetricsManager extends EntityManager<DryRunMetrics, Long, DryRunMetricsRepository> implements DryRunMetricsProvider {
    @Override
    protected String identifier() {
        return "";
    }

    @Override
    protected boolean checkEntity(DryRunMetrics entity) {
        return entity != null && entity.getComponent() != null;
    }

    @Override
    public DryRunMetrics save(DryRunMetrics entity) throws PersistenceException {
        if (!checkEntity(entity, false)) {
            throw new PersistenceException(String.format("Invalid parameters provided for adding new entity of type %s!",
                    entity.getClass().getSimpleName()));
        }
        final DryRunMetrics existing = repository.getByComponentId(entity.getComponent().getId());
        if (existing != null) {
            if(!existing.getComponent().equals(entity.getComponent()))
                throw new PersistenceException(String.format("The components of the entities differ so the entity cannot be persisted. " +
                                "Conflicting components %s : %s!",
                        existing.getComponent().getId(), entity.getComponent().getId()));
            existing.setDuration(entity.getDuration());
            existing.setCpuUsageMetrics(entity.getCpuUsageMetrics());
            existing.setMemoryUsageMetrics(entity.getMemoryUsageMetrics());
            return repository.save(existing);
        }
        repository.save(entity);
        return entity;
    }
}
