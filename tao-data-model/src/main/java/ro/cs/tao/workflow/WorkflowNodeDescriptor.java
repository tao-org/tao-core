package ro.cs.tao.workflow;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.component.ComponentLink;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "node")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class WorkflowNodeDescriptor {
    private long id;
    private String name;
    private String componentId;
    private float xCoord;
    private float yCoord;
    private ArrayList<ComponentLink> incomingLinks;
    private ArrayList<ParameterValue> customValues;

    private WorkflowDescriptor workflow;

    @XmlElement(name = "id")
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

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

    @XmlElementWrapper(name = "incomingNodes")
    public List<ComponentLink> getIncomingLinks() { return incomingLinks; }
    public void setIncomingLinks(ArrayList<ComponentLink> links) { this.incomingLinks = links; }

    @XmlElementWrapper(name = "customValues")
    public List<ParameterValue> getCustomValues() { return customValues; }
    public void setCustomValues(ArrayList<ParameterValue> customValues) { this.customValues = customValues; }

    @XmlTransient
    public WorkflowDescriptor getWorkflow() {
        return workflow;
    }
    public void setWorkflow(WorkflowDescriptor workflow) {
        this.workflow = workflow;
    }
}