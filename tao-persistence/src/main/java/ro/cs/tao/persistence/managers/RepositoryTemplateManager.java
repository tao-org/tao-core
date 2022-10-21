package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.RepositoryTemplateProvider;
import ro.cs.tao.persistence.repository.RepositoryTemplateJPARepository;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.workspaces.RepositoryTemplate;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("repositoryTemplateManager")
public class RepositoryTemplateManager extends EntityManager<RepositoryTemplate, String, RepositoryTemplateJPARepository>
        implements RepositoryTemplateProvider {
    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(RepositoryTemplate entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getName()) &&
                !StringUtilities.isNullOrEmpty(entity.getDescription()) && entity.getType() != null;
    }
}
