package ro.cs.tao.topology;

import ro.cs.tao.docker.Container;

import java.util.List;

/**
 * Created by cosmin on 7/17/2017.
 */
public interface ITopologyManager {
    NodeDescription get(String hostName) throws TopologyException;
    List<NodeDescription> list() throws TopologyException;
    void add(NodeDescription info) throws TopologyException;
    void update(NodeDescription nodeInfo) throws TopologyException;
    void remove(String hostName) throws TopologyException;
    List<Container> getAvailableDockerImages();
}
