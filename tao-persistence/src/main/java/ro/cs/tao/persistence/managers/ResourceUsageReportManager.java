package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.execution.model.ResourceUsageReport;
import ro.cs.tao.execution.persistence.ResourceUsageReportProvider;
import ro.cs.tao.persistence.repository.ResourceUsageReportRepository;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("resourceUsageReportManager")
public class ResourceUsageReportManager extends EntityManager<ResourceUsageReport, Long, ResourceUsageReportRepository>
        implements ResourceUsageReportProvider {

    @Override
    public ResourceUsageReport getByUserId(String userId) {
        return repository.getByUserId(userId);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(ResourceUsageReport entity) {
        return entity.getUserId() != null && entity.getLastReportTime() != null;
    }
}
