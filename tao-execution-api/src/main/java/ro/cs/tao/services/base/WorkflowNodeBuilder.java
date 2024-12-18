package ro.cs.tao.services.base;

import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.time.LocalDateTime;

class WorkflowNodeBuilder {

    private final WorkflowBuilder parentBuilder;
    private final WorkflowNodeDescriptor node;

    public WorkflowNodeBuilder(WorkflowBuilder parent) {
        this.node = new WorkflowNodeDescriptor();
        this.node.setLevel(0);
        this.parentBuilder = parent;
        this.node.setCreated(LocalDateTime.now());
    }

    public WorkflowNodeBuilder withName(String name) {
        this.node.setName(name);
        return this;
    }

    public WorkflowNodeBuilder withComponent(String componentId) {
        this.node.setComponentId(componentId);
        return this;
    }

    public WorkflowNodeBuilder withComponentType(ComponentType componentType) {
        this.node.setComponentType(componentType);
        return this;
    }

    public WorkflowNodeBuilder withPreserveOutput(boolean preserveOutput) {
        this.node.setPreserveOutput(preserveOutput);
        return this;
    }

    public WorkflowNodeBuilder withTransitionBehavior(TransitionBehavior behavior) {
        this.node.setBehavior(behavior);
        return this;
    }

    public WorkflowNodeBuilder withParameterValue(String name, String value) {
        this.node.addCustomValue(name, value);
        return this;
    }

    public WorkflowNodeBuilder withLinkFrom(String sourceNode, String sourcePort, String inputPort) {
        this.parentBuilder.addLink(sourceNode, sourcePort, this.node.getName(), inputPort);
        this.node.setLevel(this.node.getLevel() + 1);
        return this;
    }

    public WorkflowNodeDescriptor build() {
        return this.node;
    }
}
