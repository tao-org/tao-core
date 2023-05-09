package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.RepositoryProvider;
import ro.cs.tao.persistence.repository.RepositoryJPARepository;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.workspaces.Repository;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("repositoryManager")
public class RepositoryManager extends EntityManager<Repository, String, RepositoryJPARepository>
                                implements RepositoryProvider {

    @Override
    public List<Repository> getByUser(String userName) {
        return this.repository.getByUser(userName);
    }

    @Override
    public Repository getByUserAndName(String userName, String repoName) {
        return this.repository.getByUserAndName(userName, repoName);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(Repository entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getName()) &&
                !StringUtilities.isNullOrEmpty(entity.getDescription()) && entity.getType() != null;
    }
}
