package ro.cs.tao.workflow;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "workflow")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class WorkflowDescriptor extends WorkflowNodeGroupDescriptor {
    private String userName;
    private Visibility visibility;
    protected Status status;
    private String path;
    private boolean active;

    @XmlElement(name = "userName")
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    @XmlElement(name = "visibility")
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    @XmlElement(name = "status")
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @XmlElement(name = "definitionPath")
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    @XmlElement(name = "active")
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public void addNode(WorkflowNodeDescriptor node) {
        super.addNode(node);
        node.setWorkflow(this);
    }

}
