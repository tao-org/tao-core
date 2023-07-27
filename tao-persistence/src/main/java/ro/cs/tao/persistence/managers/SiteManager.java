package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.SiteProvider;
import ro.cs.tao.persistence.repository.SiteRepository;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.workspaces.Site;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("siteManager")
public class SiteManager extends EntityManager<Site, String, SiteRepository>
        implements SiteProvider {

    @Override
    public List<Site> getByUser(String userId) {
        return this.repository.getByUser(userId);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(Site entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getName())
                && entity.getFootprint() != null && entity.getStartDate() != null && entity.getEndDate() != null
                && entity.getStartDate().isBefore(entity.getEndDate());
    }
}
