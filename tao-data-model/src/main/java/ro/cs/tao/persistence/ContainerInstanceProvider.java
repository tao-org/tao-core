package ro.cs.tao.persistence;

import ro.cs.tao.docker.ContainerInstance;

import java.util.List;

public interface ContainerInstanceProvider extends EntityProvider<ContainerInstance, String> {

    List<ContainerInstance> getByContainerId(String containerId);
    List<ContainerInstance> getByUserName(String userName);
    ContainerInstance getByContainerIdAndUserName(String containerId, String userName);
    List<Integer> getAllocatedPorts();
}
