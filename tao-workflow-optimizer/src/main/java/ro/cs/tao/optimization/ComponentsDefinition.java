package ro.cs.tao.optimization;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.RuntimeOptimizer;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * A ComponentsDefinition class is a helper class with dependencies from a component id to a processing
 * component and a workflow node.
 *
 * @author Alexandru Pirlea
 */
public class ComponentsDefinition {
    private Map<Long, ProcessingComponent> idToComp = new HashMap<>();
    private Map<Long, WorkflowNodeDescriptor> idToWorkflowNode = new HashMap<>();
    private Map<Long, RuntimeOptimizer> idToOptimizer = new HashMap<>();

    public void addComponent(ProcessingComponent comp, Long id) {
        idToComp.put(id, comp);
    }

    public ProcessingComponent getComponent(Long id) {
        return idToComp.get(id);
    }

    public void addWorkflowNode(WorkflowNodeDescriptor node, Long id) {
        idToWorkflowNode.put(id, node);
    }

    public WorkflowNodeDescriptor getWorkflowNode(Long id) {
        return idToWorkflowNode.get(id);
    }

    public void addOptimizer(RuntimeOptimizer optimizer, Long id) {
        idToOptimizer.put(id, optimizer);
    }

    public RuntimeOptimizer getOptimizer(Long id) {
        return idToOptimizer.get(id);
    }
}
