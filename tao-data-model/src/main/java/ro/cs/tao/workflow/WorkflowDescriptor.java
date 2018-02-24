package ro.cs.tao.workflow;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.component.ComponentLink;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "workflow")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class WorkflowDescriptor {
    private Long id;
    private String name;
    private LocalDateTime created;
    private String userName;
    private Visibility visibility;
    private String path;
    private boolean active;
    private Status status;
    private List<WorkflowNodeDescriptor> nodes = new ArrayList<>();

    @XmlElement(name = "id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "created")
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    @XmlElement(name = "userName")
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    @XmlElement(name = "visibility")
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    @XmlElement(name = "definitionPath")
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    @XmlElement(name = "active")
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @XmlElement(name = "status")
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

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
        return this.nodes;
    }

   // addNode and removeNode are needed for bidirectional relationship
    public void addNode(WorkflowNodeDescriptor node)
    {
        if (this.nodes == null)
        {
            this.nodes = new ArrayList<>();
        }
        if (!this.nodes.contains(node))
        {
            this.nodes.add(node);
            node.setWorkflow(this);
        }
    }

    public void removeNode(WorkflowNodeDescriptor node)
    {
        if(this.nodes.contains(node))
        {
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

    private void orderNodes() {
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
