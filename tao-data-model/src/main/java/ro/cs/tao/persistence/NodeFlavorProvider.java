package ro.cs.tao.persistence;

import ro.cs.tao.topology.NodeFlavor;

public interface NodeFlavorProvider extends EntityProvider<NodeFlavor, String> {

    NodeFlavor getMasterFlavor();
    NodeFlavor getMatchingFlavor(NodeFlavor flavor);

}
