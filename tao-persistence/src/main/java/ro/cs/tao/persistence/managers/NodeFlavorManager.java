package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.Sort;
import ro.cs.tao.persistence.NodeFlavorProvider;
import ro.cs.tao.persistence.repository.NodeFlavorRepository;
import ro.cs.tao.topology.NodeFlavor;
import ro.cs.tao.topology.NodeRole;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("nodeFlavorManager")
public class NodeFlavorManager extends EntityManager<NodeFlavor, String, NodeFlavorRepository> implements NodeFlavorProvider {

    @Override
    public List<NodeFlavor> list() {
        return super.list().stream().filter(f -> !f.getId().equals(NodeRole.MASTER.value())).collect(Collectors.toList());
    }

    @Override
    public List<NodeFlavor> list(Sort sort) {
        return super.list(sort).stream().filter(f -> !f.getId().equals(NodeRole.MASTER.value())).collect(Collectors.toList());
    }

    @Override
    public List<NodeFlavor> list(int pageNumber, int pageSize, Sort sort) {
        return super.list(pageNumber, pageSize, sort).stream().filter(f -> !f.getId().equals(NodeRole.MASTER.value())).collect(Collectors.toList());
    }

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
    public NodeFlavor getMatchingFlavor(int cpu, int memoryMB) {
        return repository.getMatchingFlavor(cpu, memoryMB);
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
