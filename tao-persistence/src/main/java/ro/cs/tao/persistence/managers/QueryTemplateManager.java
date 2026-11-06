package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.QueryTemplateProvider;
import ro.cs.tao.persistence.repository.QueryTemplateRepository;
import ro.cs.tao.userdb.QueryTemplate;
import ro.cs.tao.utils.StringUtilities;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("queryTemplateManager")
public class QueryTemplateManager extends EntityManager<QueryTemplate, String, QueryTemplateRepository>
        implements QueryTemplateProvider {
    @Override
    protected String identifier() {
        return "name";
    }

    @Override
    protected boolean checkEntity(QueryTemplate entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getName())
                && !StringUtilities.isNullOrEmpty(entity.getDescription())
                && !StringUtilities.isNullOrEmpty(entity.getSql());
    }
}
