package ro.cs.tao.persistence;

import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;

import java.util.List;

public interface ContainerProvider extends EntityProvider<Container, String> {

    Container getByName(String name);

    List<Container> getByType(ContainerType type);

    List<Container> getByTypeAndVisibility(ContainerType type, ContainerVisibility visibility);

    List<Container> listContainersVisibleToUser(String userId);

    List<Container> listUserContainers(String userId);
}
