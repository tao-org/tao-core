package ro.cs.tao.topology;

/**
 * Created by cosmin on 7/17/2017.
 */
public interface ITopologyToolInstaller {
    void setMasterNodeDescription(NodeDescription masterNodeInfo);
    void installNewNode(NodeDescription info) throws TopologyException;
    void uninstallNode(NodeDescription info)throws TopologyException;
    void editNode(NodeDescription nodeInfo)throws TopologyException;
}
