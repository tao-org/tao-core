package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.DryRunMetrics;
import ro.cs.tao.persistence.DryRunMetricsProvider;
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
}
