package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.execution.model.ResourceUsage;
import ro.cs.tao.execution.persistence.ResourceUsageProvider;
import ro.cs.tao.persistence.repository.ResourceUsageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("resourceUsageManager")
public class ResourceUsageManager extends EntityManager<ResourceUsage, Long, ResourceUsageRepository>
        implements ResourceUsageProvider {

    @Override
    public List<ResourceUsage> getByUserId(String userId) {
        return repository.getByUserId(userId);
    }

    @Override
    public List<ResourceUsage> getByUserIdAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.getByUserIdAndInterval(user, startDate, endDate);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(ResourceUsage entity) {
        return entity.getId() != null && entity.getHost() != null;
    }
}
