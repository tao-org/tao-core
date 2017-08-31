package ro.cs.tao.topology;

/**
 * Created by cosmin on 7/17/2017.
 */
public interface ITopologyManager {
    void addNode(NodeDescription info);
    void removeNode(NodeDescription info);
    void editNode(NodeDescription nodeInfo);
}
