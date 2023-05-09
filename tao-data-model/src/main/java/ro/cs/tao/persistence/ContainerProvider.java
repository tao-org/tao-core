package ro.cs.tao.persistence;

import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;

import java.util.List;

public interface ContainerProvider extends EntityProvider<Container, String> {

    Container getByName(String name);

    List<Container> getByType(ContainerType type);

}
