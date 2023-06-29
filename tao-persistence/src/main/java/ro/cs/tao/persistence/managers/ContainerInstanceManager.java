package ro.cs.tao.persistence.managers;

import org.springframework.stereotype.Component;
import ro.cs.tao.docker.ContainerInstance;
import ro.cs.tao.persistence.ContainerInstanceProvider;
import ro.cs.tao.persistence.repository.ContainerInstanceRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;

@Component("containerInstanceManager")
public class ContainerInstanceManager
        extends EntityManager<ContainerInstance, String, ContainerInstanceRepository>
        implements ContainerInstanceProvider {


    @Override
    public List<ContainerInstance> getByContainerId(String containerId) {
        return repository.findByContainerId(containerId);
    }

    @Override
    public List<ContainerInstance> getByUserName(String userName) {
        return repository.findByUserName(userName);
    }

    @Override
    public ContainerInstance getByContainerIdAndUserName(String containerId, String userName) {
        return repository.findByContainerIdAndUserName(containerId, userName);
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
                && !StringUtilities.isNullOrEmpty(entity.getUserName());
    }
}
