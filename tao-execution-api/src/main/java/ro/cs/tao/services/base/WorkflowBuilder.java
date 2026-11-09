package ro.cs.tao.services.base;

import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.TransactionalMethod;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.services.interfaces.WorkflowService;
import ro.cs.tao.services.utils.WorkflowUtilities;
import ro.cs.tao.utils.Triple;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.Status;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder class for constructing a WorkflowDescriptor object. This class provides a fluent API to
 * define properties of a workflow, add workflow nodes, and establish connections between nodes.
 */
public class WorkflowBuilder {

    private WorkflowDescriptor workflow;
    private final Map<String, WorkflowNodeDescriptor> nodeMap;
    private final Map<String, Triple<String, String, String>> incomingLinksMap;

    public WorkflowBuilder() {
        this.workflow = new WorkflowDescriptor();
        this.workflow.setCreated(LocalDateTime.now());
        this.nodeMap = new LinkedHashMap<>();
        this.incomingLinksMap = new HashMap<>();
    }

    public WorkflowBuilder withUserId(String userId) {
        this.workflow.setUserId(userId);
        return this;
    }

    public WorkflowBuilder withName(String name) {
        this.workflow.setName(name);
        return this;
    }

    public WorkflowBuilder withStatus(Status status) {
        this.workflow.setStatus(status);
        return this;
    }

    public WorkflowBuilder withActive(boolean active) {
        this.workflow.setActive(active);
        return this;
    }

    public WorkflowBuilder withVisibility(Visibility visibility) {
        this.workflow.setVisibility(visibility);
        return this;
    }

    public WorkflowNodeBuilder newNodeBuilder() {
        return new WorkflowNodeBuilder(this);
    }

    public WorkflowBuilder withNode(WorkflowNodeDescriptor node) {
        this.workflow.addNode(node);
        this.nodeMap.put(node.getName(), node);
        return this;
    }

    public WorkflowDescriptor build() throws PersistenceException {
        final WorkflowService service = SpringContextBridge.services().getService(WorkflowService.class);
        return TransactionalMethod.withExceptionType(PersistenceException.class)
                           .execute(() ->
          {
              WorkflowBuilder.this.workflow = service.save(WorkflowBuilder.this.workflow);
              WorkflowNodeDescriptor sourceNode;
              WorkflowNodeDescriptor targetNode;
              for (Map.Entry<String, Triple<String, String, String>> entry : WorkflowBuilder.this.incomingLinksMap.entrySet()) {
                  sourceNode = findNode(entry.getValue().getKeyOne());
                  targetNode = findNode(entry.getKey());
                  final TaoComponent sourceComponent = WorkflowUtilities.findComponent(sourceNode);
                  if (sourceComponent == null) {
                      throw new PersistenceException("Component " + sourceNode.getComponentId() + " not found");
                  }
                  final TargetDescriptor sourceTarget = sourceComponent.getTargets().stream().filter(t -> t.getName().equals(entry.getValue().getKeyTwo())).findFirst().orElse(null);
                  if (sourceTarget == null) {
                      throw new PersistenceException(sourceNode.getComponentId() + "." + entry.getValue().getKeyTwo() + " does not exist");
                  }
                  final TaoComponent targetComponent = WorkflowUtilities.findComponent(targetNode);
                  if (targetComponent == null) {
                      throw new PersistenceException("Component " + targetNode.getComponentId() + " not found");
                  }
                  final SourceDescriptor targetSource = targetComponent.getSources().stream().filter(s -> s.getName().equals(entry.getValue().getKeyThree())).findFirst().orElse(null);
                  if (sourceTarget == null) {
                      throw new PersistenceException(targetNode.getComponentId() + "." + entry.getValue().getKeyThree() + " does not exist");
                  }
                  service.addLink(sourceNode.getId(), sourceTarget.getId(), targetNode.getId(), targetSource.getId());
              }
              return WorkflowBuilder.this.workflow;
          });
    }

    WorkflowNodeDescriptor findNode(String name) {
        return this.nodeMap.get(name);
    }

    void addLink(String sourceNode, String sourcePort, String targetNode, String targetPort) {
        this.incomingLinksMap.put(targetNode, new Triple<>(sourceNode, sourceNode, targetPort));
    }
}
