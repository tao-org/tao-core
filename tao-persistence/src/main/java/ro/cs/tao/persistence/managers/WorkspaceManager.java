package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.WorkspaceProvider;
import ro.cs.tao.persistence.repository.WorkspaceRepository;
import ro.cs.tao.topology.Workspace;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("workspaceManager")
public class WorkspaceManager extends EntityManager<Workspace, Integer, WorkspaceRepository>
                                implements WorkspaceProvider {

    @Override
    public List<Workspace> getByUser(String userName) {
        return this.repository.getByUser(userName);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(Workspace entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getName()) &&
                !StringUtilities.isNullOrEmpty(entity.getDescription()) && entity.getType() != null;
    }
}
