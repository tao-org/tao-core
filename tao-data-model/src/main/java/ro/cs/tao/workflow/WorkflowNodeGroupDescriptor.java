package ro.cs.tao.workflow;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.component.ComponentLink;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@XmlRootElement(name = "nodeGroup")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class WorkflowNodeGroupDescriptor extends WorkflowNodeDescriptor {

    protected List<WorkflowNodeDescriptor> nodes = new ArrayList<>();

    // NOT WORKING! (save nodes not working properly)
    /*@XmlElementWrapper(name = "nodes")
    public List<WorkflowNodeDescriptor> getNodes() {
        if (this.nodes != null && this.nodes.size() > 0) {
            orderNodes();
        }
        return this.nodes != null ? this.nodes : new ArrayList<>();
    }*/

    @XmlElementWrapper(name = "nodes")
    public List<WorkflowNodeDescriptor> getNodes() {
        orderNodes();
        return this.nodes;
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

    /* NEVER use a classic setter for a one to many
     * HHH000346: Error during managed flush [java.util.ArrayList cannot be cast to org.hibernate.collection.spi.PersistentCollection]
     */
    /*public void setNodes(List<WorkflowNodeDescriptor> nodes) { this.nodes = nodes; }*/

//    public void setNodes(List<WorkflowNodeDescriptor> nodes)
//    {
//        System.out.println("-------------call setNodes " + nodes.size());
//        if (this.nodes == null)
//        {
//            this.nodes = new ArrayList<>();
//        }
//        else
//        {
//            this.nodes.clear();
//        }
//
//        /*if (this.nodes.size() > 0)
//        {
//            for (WorkflowNodeDescriptor node : this.nodes)
//            {
//                removeNode(node);
//            }
//        }*/
//
//        for (WorkflowNodeDescriptor node : nodes)
//        {
//            addNode(node);
//        }
//    }

//    public void setNodes(List<WorkflowNodeDescriptor> nodes)
//    {
//        this.nodes.clear();
//        this.nodes.addAll(nodes);
//    }

    public void setNodes(List<WorkflowNodeDescriptor> nodes) {
        if (this.nodes != null) {
            this.nodes.clear();
        }
        for (WorkflowNodeDescriptor node : nodes) {
            addNode(node);
        }
    }

    private void orderNodes() {
        if (this.nodes != null) {
            List<WorkflowNodeDescriptor> newList = new ArrayList<>();
            WorkflowNodeDescriptor root = nodes.stream()
                    .filter(n -> n.getIncomingLinks() == null || n.getIncomingLinks().isEmpty())
                    .findFirst().orElse(null);
            if (root == null) {
                throw new IllegalArgumentException(String.format("This workflow [%s] has no entry point", this.id));
            } else {
                newList.add(root);
            }
            Stack<WorkflowNodeDescriptor> stack = new Stack<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                List<WorkflowNodeDescriptor> children = findChildren(stack.pop());
                if (children != null && children.size() > 0) {
                    children.forEach(n -> {
                        if (!newList.contains(n)) {
                            newList.add(n);
                            stack.push(n);
                        }
                    });
                }
            }

            // WRONG !!!
            //nodes = newList;
        /* Hibernate requires complete ownership of the children collection in the parent object.
        If you set it to a new object, Hibernate is unable to track changes to that collection and thus has no idea how to apply
        the cascading persistence to your objects. */

            this.nodes.clear();
            this.nodes.addAll(newList);
        }
        //setNodes(newList);
    }

    private List<WorkflowNodeDescriptor> findChildren(WorkflowNodeDescriptor node) {
        if (nodes == null || nodes.isEmpty() || node == null) {
            return null;
        }
        return nodes.stream().filter(n -> {
            List<ComponentLink> links = n.getIncomingLinks();
            return links != null &&
                    links.stream().anyMatch(l -> node.getComponentId().equals(l.getInput().getParentId()));
        }).collect(Collectors.toList());
    }
}
