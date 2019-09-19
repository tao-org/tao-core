package ro.cs.tao.optimization;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.RuntimeOptimizer;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * A NodesDefinition class is a helper class with dependencies from a node's id to:
 * processing component / workflow node / runtime optimizer
 *
 * @author Alexandru Pirlea
 */
public class NodesDefinition {
    private Map<Long, ProcessingComponent>    idToComponent    = new HashMap<>();
    private Map<Long, WorkflowNodeDescriptor> idToWorkflowNode = new HashMap<>();
    private Map<Long, RuntimeOptimizer>       idToOptimizer    = new HashMap<>();

    protected void addComponent(ProcessingComponent comp, Long id) {
        idToComponent.put(id, comp);
    }

    protected ProcessingComponent getComponent(Long id) {
        return idToComponent.get(id);
    }

    protected void addWorkflowNode(WorkflowNodeDescriptor node, Long id) {
        idToWorkflowNode.put(id, node);
    }

    protected WorkflowNodeDescriptor getWorkflowNode(Long id) {
        return idToWorkflowNode.get(id);
    }

    protected void addOptimizer(RuntimeOptimizer optimizer, Long id) {
        idToOptimizer.put(id, optimizer);
    }

    protected RuntimeOptimizer getOptimizer(Long id) {
        return idToOptimizer.get(id);
    }
}
