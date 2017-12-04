package ro.cs.tao.services.interfaces;

import ro.cs.tao.docker.Container;
import ro.cs.tao.topology.NodeDescription;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface TopologyService extends CRUDService<NodeDescription> {
    List<Container> getDockerImages();
}
