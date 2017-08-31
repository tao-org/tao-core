package ro.cs.tao.topology;

/**
 * Created by cosmin on 7/17/2017.
 */
public interface ITopologyToolInstaller {
    void setMasterNodeDescription(NodeDescription masterNodeInfo);
    void installNewNode(NodeDescription info);
    void uninstallNode(NodeDescription info);
    void editNode(NodeDescription nodeInfo);
}
