package ro.cs.tao.execution.model;

import ro.cs.tao.topology.NodeDescription;

/**
 * This class contains the node on which a task can be executed together with the number of
 * CPU's and the memory that should be used.
 *
 * @author Lucian Barbulescu
 */
public class NodeData {
    private final NodeDescription node;
    private final int cpu;
    private final long memory;

    /**
     * Constructor.
     *
     * @param node the node descriptor
     * @param cpu the number of cpus
     * @param memory the memory
     */
    public NodeData(NodeDescription node, int cpu, long memory) {
        this.node = node;
        this.cpu = cpu;
        this.memory = memory;
    }

    public NodeDescription getNode() {
        return node;
    }

    public int getCpu() {
        return cpu;
    }

    public long getMemory() {
        return memory;
    }
}