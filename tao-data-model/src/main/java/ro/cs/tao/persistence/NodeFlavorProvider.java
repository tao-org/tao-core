package ro.cs.tao.persistence;

import ro.cs.tao.topology.NodeFlavor;

/**
 * Provides an abstraction for managing {@link NodeFlavor} entities. This interface
 * extends the {@code EntityProvider} interface, defining methods for retrieving, saving,
 * updating, and deleting NodeFlavor objects. Additionally, it introduces functionality
 * to retrieve specific flavors based on criteria like master status or hardware
 * specifications.
 *
 * This interface can be implemented to customize the behavior of NodeFlavor entity
 * management, including filtering or matching based on specific requirements.
 */
public interface NodeFlavorProvider extends EntityProvider<NodeFlavor, String> {

    /**
     * Retrieves the master node flavor configuration.
     *
     * @return the {@link NodeFlavor} object representing the master node flavor.
     */
    NodeFlavor getMasterFlavor();
    /**
     * Determines and retrieves the best matching {@link NodeFlavor} for the provided flavor.
     *
     * @param flavor the {@link NodeFlavor} object to be matched against available configurations
     * @return the {@link NodeFlavor} object that best matches the specified flavor
     */
    NodeFlavor getMatchingFlavor(NodeFlavor flavor);
    /**
     * Determines and retrieves the best matching {@link NodeFlavor} based on the specified
     * hardware requirements in terms of CPU and memory.
     *
     * @param cpu the number of CPU cores required
     * @param memoryMB the amount of memory required in megabytes
     * @return the {@link NodeFlavor} that best matches the specified hardware requirements
     */
    NodeFlavor getMatchingFlavor(int cpu, int memoryMB);
}
