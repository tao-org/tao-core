package ro.cs.tao.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "workflow")
public class WorkflowDescriptor {
    private Long id;
    private String name;
    private LocalDateTime created;
    private String userName;
    private Visibility visibility;
    private String path;
    private boolean active;
    private Status status;
    private List<WorkflowNodeDescriptor> nodes;

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

    @XmlElementWrapper(name = "nodes")
    public List<WorkflowNodeDescriptor> getNodes() { return nodes; }
    public void setNodes(List<WorkflowNodeDescriptor> nodes) { this.nodes = nodes; }
}
