package ro.cs.tao.persistence;

import ro.cs.tao.docker.ContainerInstance;

import java.util.List;

public interface ContainerInstanceProvider extends EntityProvider<ContainerInstance, String> {

    List<ContainerInstance> getByContainerId(String containerId);
    List<ContainerInstance> getByUserId(String userId);
    ContainerInstance getByContainerIdAndUserId(String containerId, String userId);
    List<Integer> getAllocatedPorts();
}
