/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.workflow;

import com.fasterxml.jackson.annotation.JsonBackReference;
import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.workflow.enums.ComponentType;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Descriptor of a workflow node.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "node")
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = WorkflowNodeDescriptor.class)
public class WorkflowNodeDescriptor extends GraphObject {

    private String componentId;
    private ComponentType componentType;
    private float xCoord;
    private float yCoord;
    private int level;
    private Set<ComponentLink> incomingLinks;
    private boolean preserveOutput;
    private List<ParameterValue> additionalInfo;
    private TransitionBehavior behavior = TransitionBehavior.FAIL_ON_ERROR;
    private Long createdFromNodeId;

    private WorkflowDescriptor workflow;

    @XmlElement(name = "id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @XmlElement(name = "componentId")
    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }

    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }

    @XmlTransient
    public float getxCoord() { return xCoord; }
    public void setxCoord(float xCoord) { this.xCoord = xCoord; }

    @XmlTransient
    public float getyCoord() { return yCoord; }
    public void setyCoord(float yCoord) { this.yCoord = yCoord; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public TransitionBehavior getBehavior() { return behavior; }
    public void setBehavior(TransitionBehavior behavior) { this.behavior = behavior; }

    public boolean getPreserveOutput() { return preserveOutput; }
    public void setPreserveOutput(boolean preserveOutput) { this.preserveOutput = preserveOutput; }

    @XmlElementWrapper(name = "incomingNodes")
    public Set<ComponentLink> getIncomingLinks() { return incomingLinks; }

    public void setIncomingLinks(Set<ComponentLink> incomingLinks) {
        this.incomingLinks = incomingLinks;
    }

    public void addLink(ComponentLink link) {
        if (this.incomingLinks == null) {
            this.incomingLinks = new HashSet<>();
        }
        if (this.incomingLinks.stream().noneMatch(l -> l.equals(link))) {
            this.incomingLinks.add(link);
        }
    }

    public void removeLink(ComponentLink link) {
        if (this.incomingLinks != null && link != null) {
            this.incomingLinks.removeIf(l -> l.equals(link));
        }
    }

    @XmlElementWrapper(name = "additionalInfo")
    public List<ParameterValue> getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(List<ParameterValue> values) { this.additionalInfo = values; }
    public void addInfo(String key, String value) {
        ParameterValue parameterValue = new ParameterValue();
        parameterValue.setParameterName(key);
        parameterValue.setParameterValue(value);
        if (this.additionalInfo == null) {
            this.additionalInfo = new ArrayList<>();
        }
        final ParameterValue param = this.additionalInfo.stream().filter(p -> p.getParameterName().equals(key)).findFirst().orElse(null);
        if (param == null) {
            this.additionalInfo.add(parameterValue);
        } else {
            param.setParameterValue(value);
        }
    }

    public Long getCreatedFromNodeId() {
        return createdFromNodeId;
    }

    public void setCreatedFromNodeId(Long createdFromNodeId) {
        this.createdFromNodeId = createdFromNodeId;
    }

    @XmlTransient
    @JsonBackReference
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
