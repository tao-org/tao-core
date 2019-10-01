package ro.cs.tao.optimization;

import java.util.ArrayList;
import java.util.List;

/**
 * An OptimizationGraph is a list of OptimizationNode.
 *
 * @author Alexandru Pirlea
 */
public class OptimizationGraph {
    private List<OptimizationNode> nodes = new ArrayList<>();

    public List<OptimizationNode> getNodes() {
        return nodes;
    }

    public void addNode(OptimizationNode node) {
        nodes.add(node);
    }

    public void removeNode(OptimizationNode node) {
        nodes.remove(node);
    }

    public int size() {
        return nodes.size();
    }
}
