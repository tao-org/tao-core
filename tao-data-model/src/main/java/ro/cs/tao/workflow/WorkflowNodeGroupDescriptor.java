package ro.cs.tao.workflow;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "nodeGroup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class WorkflowNodeGroupDescriptor
        extends WorkflowNodeDescriptor implements NodeListOrderer {

    private List<WorkflowNodeDescriptor> nodes = new ArrayList<>();

    @XmlElementWrapper(name = "nodes")
    public List<WorkflowNodeDescriptor> getNodes() {
        return this.nodes;
    }

    @Transient
    public List<WorkflowNodeDescriptor> getOrderedNodes() {
        return orderNodes(this.nodes);
    }

    // addNode and removeNode are needed for bidirectional relationship
    public void addNode(WorkflowNodeDescriptor node) {
        if (this.nodes == null) {
            this.nodes = new ArrayList<>();
        }
        if (!this.nodes.contains(node)) {
            this.nodes.add(node);
        }
    }

    public void removeNode(WorkflowNodeDescriptor node) {
        if(this.nodes.contains(node)) {
            node.setWorkflow(null);
            this.nodes.remove(node);
        }
    }

    public void setNodes(List<WorkflowNodeDescriptor> nodes) {
        if (this.nodes != null) {
            this.nodes.clear();
        }
        for (WorkflowNodeDescriptor node : nodes) {
            addNode(node);
        }
    }
}
