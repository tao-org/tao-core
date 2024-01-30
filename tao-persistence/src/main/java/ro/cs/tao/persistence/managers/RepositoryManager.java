package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.RepositoryProvider;
import ro.cs.tao.persistence.TransactionalMethod;
import ro.cs.tao.persistence.repository.RepositoryJPARepository;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.workspaces.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("repositoryManager")
public class RepositoryManager extends EntityManager<Repository, String, RepositoryJPARepository>
                                implements RepositoryProvider {

    @Override
    public List<Repository> getByUser(String userId) {
        return this.repository.getByUser(userId);
    }

    @Override
    public Repository getByUserAndName(String userId, String repoName) {
        return this.repository.getByUserAndName(userId, repoName);
    }

    @Override
    public List<Repository> getUserSystemRepositories(String userId) {
        return repository.getUserSystemRepositories(userId);
    }

    @Override
    public Repository getUserPersistentRepository(String userId) {
        return repository.getUserPeristentRepository(userId);
    }

    @Override
    public void setUserPersistentRepository(String userId, String repositoryId) throws PersistenceException {
        TransactionalMethod.withExceptionType(PersistenceException.class).execute(() -> {
            final List<Repository> repositories = getByUser(userId);
            for (Repository repository : repositories) {
                repository.setPersistentStorage(repository.getId().equals(repositoryId));
                try {
                    update(repository);
                } catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public Repository save(Repository entity) throws PersistenceException {
        entity.setOrder(repository.countUserRepositories(entity.getUserId()));
        entity.setCreated(LocalDateTime.now());
        return super.save(entity);
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
