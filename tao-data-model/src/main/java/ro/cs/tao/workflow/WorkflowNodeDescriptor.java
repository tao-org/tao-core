package ro.cs.tao.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "node")
public class WorkflowNodeDescriptor {
    private long id;
    private String name;
    private long workflowId;
    private String componentId;
    private float xCoord;
    private float yCoord;
    private List<Long> incomingNodes;
    private List<ParameterValue> customValues;

    @XmlElement(name = "id")
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "workflowId")
    public long getWorkflowId() { return workflowId; }
    public void setWorkflowId(long workflowId) { this.workflowId = workflowId; }

    @XmlElement(name = "componentId")
    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }

    @XmlElementWrapper(name = "incomingNodes")
    public List<Long> getIncomingNodes() { return incomingNodes; }
    public void setIncomingNodes(List<Long> incomingNodes) { this.incomingNodes = incomingNodes; }

    @XmlElementWrapper(name = "customValues")
    public List<ParameterValue> getCustomValues() { return customValues; }
    public void setCustomValues(List<ParameterValue> customValues) { this.customValues = customValues; }
}
