package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.NodeFlavorProvider;
import ro.cs.tao.persistence.repository.NodeFlavorRepository;
import ro.cs.tao.topology.NodeFlavor;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("nodeFlavorManager")
public class NodeFlavorManager extends EntityManager<NodeFlavor, String, NodeFlavorRepository> implements NodeFlavorProvider {
    @Override
    public NodeFlavor getMasterFlavor() {
        return repository.findById("master").orElse(null);
    }

    @Override
    public NodeFlavor getMatchingFlavor(NodeFlavor flavor) {
        return flavor != null ?
                repository.getMatchingFlavor(flavor.getCpu(), flavor.getMemory(), flavor.getDisk()) : null;
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(NodeFlavor entity) {
        return entity != null && entity.getCpu() > 0 && entity.getMemory() > 0 && entity.getDisk() > 0;
    }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && (existingEntity == (get(entityId) != null));
    }
}
