package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.docker.ContainerInstance;
import ro.cs.tao.persistence.ContainerInstanceProvider;
import ro.cs.tao.persistence.repository.ContainerInstanceRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;
@Configuration
@EnableTransactionManagement
@Component("containerInstanceManager")
public class ContainerInstanceManager
        extends EntityManager<ContainerInstance, String, ContainerInstanceRepository>
        implements ContainerInstanceProvider {


    @Override
    public List<ContainerInstance> getByContainerId(String containerId) {
        return repository.findByContainerId(containerId);
    }

    @Override
    public List<ContainerInstance> getByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public ContainerInstance getByContainerIdAndUserId(String containerId, String userId) {
        return repository.findByContainerIdAndUserId(containerId, userId);
    }

    @Override
    public List<Integer> getAllocatedPorts() {
        return repository.getAllocatedPorts();
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(ContainerInstance entity) {
        return entity != null
                && !StringUtilities.isNullOrEmpty(entity.getId())
                && !StringUtilities.isNullOrEmpty(entity.getContainerId())
                && !StringUtilities.isNullOrEmpty(entity.getUserId());
    }
}
