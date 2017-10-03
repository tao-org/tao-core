package ro.cs.tao.topology;

import ro.cs.tao.component.Identifiable;

/**
 * Created by cosmin on 7/17/2017.
 */
public abstract class TopologyToolInstaller extends Identifiable {
    public abstract void setMasterNodeDescription(NodeDescription masterNodeInfo);
    public abstract void installNewNode(NodeDescription info) throws TopologyException;
    public abstract void uninstallNode(NodeDescription info)throws TopologyException;
    public abstract void editNode(NodeDescription nodeInfo)throws TopologyException;
}
