package ro.cs.tao.persistence;

import ro.cs.tao.docker.Container;

public interface ContainerProvider extends EntityProvider<Container, String> {

    Container getByName(String name);

}
