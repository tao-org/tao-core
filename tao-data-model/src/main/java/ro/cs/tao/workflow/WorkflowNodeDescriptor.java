package ro.cs.tao.workflow;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.component.ComponentLink;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "node")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class WorkflowNodeDescriptor {
    protected Long id;
    protected String name;
    protected LocalDateTime created;
    private String componentId;
    private float xCoord;
    private float yCoord;
    private List<ComponentLink> incomingLinks;
    private List<ParameterValue> customValues;

    private WorkflowDescriptor workflow;

    @XmlElement(name = "id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "componentId")
    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }

    @XmlTransient
    public float getxCoord() { return xCoord; }
    public void setxCoord(float xCoord) { this.xCoord = xCoord; }

    @XmlTransient
    public float getyCoord() { return yCoord; }
    public void setyCoord(float yCoord) { this.yCoord = yCoord; }

    @XmlElement(name = "created")
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    @XmlElementWrapper(name = "incomingNodes")
    public List<ComponentLink> getIncomingLinks() { return incomingLinks; }
    public void setIncomingLinks(List<ComponentLink> links) {
        if (this.incomingLinks != null) {
            this.incomingLinks.clear();
        }
        if (links != null) {
            for (ComponentLink link : links) {
                addLink(link);
            }
        }
    }
    public void addLink (ComponentLink link) {
        if (this.incomingLinks == null) {
            this.incomingLinks = new ArrayList<>();
        }
        if (!this.incomingLinks.contains(link)) {
            this.incomingLinks.add(link);
        }
    }

    public void removeLink(ComponentLink link) {
        if (this.incomingLinks.contains(link)) {
            this.incomingLinks.remove(link);
        }
    }

    @XmlElementWrapper(name = "customValues")
    public List<ParameterValue> getCustomValues() { return customValues; }
    public void setCustomValues(List<ParameterValue> customValues) { this.customValues = customValues; }
    public void addCustomValue(ParameterValue parameterValue) {
        if (this.customValues == null) {
            this.customValues = new ArrayList<>();
        }
        this.customValues.add(parameterValue);
    }
    public void addCustomValue(String name, String value) {
        ParameterValue parameterValue = new ParameterValue();
        parameterValue.setParameterName(name);
        parameterValue.setParameterValue(value);
        addCustomValue(parameterValue);
    }

    @XmlTransient
    public WorkflowDescriptor getWorkflow() {
        return workflow;
    }
    public void setWorkflow(WorkflowDescriptor workflow) {
        this.workflow = workflow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowNodeDescriptor )) return false;
        return id != null && id.equals(((WorkflowNodeDescriptor) o).id);
    }
    @Override
    public int hashCode() {
        return 31 * (name != null ? name.hashCode() : 0) + 67 * (id != null ? id.hashCode() : 0);
    }
}
