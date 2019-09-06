package ro.cs.tao.optimization;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.RuntimeOptimizer;

import java.util.ArrayList;
import java.util.List;

/**
 * An OptimizationNode is used for simulating a WorkflowNodeDescriptor, but with dependencies in both ways.
 *
 * @author Alexandru Pirlea
 */
public class OptimizationNode {
    private List<OptimizationNode> successors   = new ArrayList<>();
    private List<OptimizationNode> predecessors = new ArrayList<>();
    private List<Long> nodeIds = new ArrayList<>();
    private ComponentsDefinition defined;
    private int level = -1;

    public ComponentsDefinition getComponentDefinition() {
        return defined;
    }

    public OptimizationNode(Long nodeId, ComponentsDefinition defined) {
        nodeIds.add(nodeId);
        this.defined = defined;
    }

    public List<OptimizationNode> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<OptimizationNode> successors) {
        this.successors = successors;
    }

    public List<OptimizationNode> getPredecessors() {
        return predecessors;
    }

    public Long getFirstNodeId() {
        return nodeIds.get(0);
    }

    public Long getLastNodeId() {
        return nodeIds.get(nodeIds.size() - 1);
    }

    public List<Long> getNodeIds() {
        return nodeIds;
    }

    public void addChild(OptimizationNode n) {
        successors.add(n);
    }

    public void addParent(OptimizationNode n) {
        predecessors.add(n);
    }

    public boolean isCompatibleWith(OptimizationNode node) {
        if (node == null) {
            return false;
        }

        if (this.successors.size() != 1 || node.predecessors.size() != 1) {
            return false;
        }

        /* First node must have component with only one output. */
        ProcessingComponent uComponent = defined.getComponent(this.getLastNodeId());

        /* Second node must have component with only one input. */
        ProcessingComponent vComponent = defined.getComponent(node.getFirstNodeId());

        if (uComponent == null || vComponent == null) {
            return false;
        }

        if (uComponent.getTargets().size() != 1 || vComponent.getSources().size() != 1) {
            return false;
        }

        if (uComponent.getTargets().get(0).getCardinality() != 1 || vComponent.getSources().get(0).getCardinality() != 1) {
            return false;
        }

        /* Components must have the same aggregator. */
        RuntimeOptimizer uOptimizer = defined.getOptimizer(this.getFirstNodeId());
        RuntimeOptimizer vOptimizer = defined.getOptimizer(node.getFirstNodeId());

        if (uOptimizer == null || vOptimizer == null) {
            return false;
        }

        return uOptimizer.equals(vOptimizer);
    }

    public int getLevel() {
        // if level is not defined
        if (level == -1) {
            if (predecessors.isEmpty()) {
                level = 0;

            } else {
                predecessors.forEach((n) -> level = Integer.max(level, n.getLevel()));
                level++;

            }
        }

        return level;
    }
}
