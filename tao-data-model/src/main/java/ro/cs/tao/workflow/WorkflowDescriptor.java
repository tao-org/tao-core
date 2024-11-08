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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.workflow.enums.Status;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor for a workflow graph.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "workflow")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = WorkflowDescriptor.class)
public class WorkflowDescriptor
        extends GraphObject implements NodeListOrderer {

    private String userId;
    private Visibility visibility;
    protected Status status;
    private String path;
    private Long createdFromWorkflowId;
    private boolean active;
    private List<WorkflowNodeDescriptor> nodes = new ArrayList<>();
    private float xCoord;
    private float yCoord;
    private float zoom;
    private boolean temporary;
    private List<String> tags;

    @XmlElement(name = "userName")
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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

    @XmlElement(name = "createdFromWorkflowId")
    public Long getCreatedFromWorkflowId() { return createdFromWorkflowId; }
    public void setCreatedFromWorkflowId(Long createdFromWorkflowId) { this.createdFromWorkflowId = createdFromWorkflowId; }

    @XmlTransient
    public float getxCoord() { return xCoord; }
    public void setxCoord(float xCoord) { this.xCoord = xCoord; }

    @XmlTransient
    public float getyCoord() { return yCoord; }
    public void setyCoord(float yCoord) { this.yCoord = yCoord; }

    @XmlTransient
    public float getZoom() { return zoom; }
    public void setZoom(float zoom) { this.zoom = zoom; }

    @XmlElement(name = "temporary")
    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    @XmlElementWrapper(name = "nodes")
    @JsonManagedReference
    public List<WorkflowNodeDescriptor> getNodes() {
        return this.nodes;
    }

    @Transient
    public List<WorkflowNodeDescriptor> getOrderedNodes() {
        return orderNodes(this.nodes, null);
    }

    // addNode and removeNode are needed for bidirectional relationship
    public void addNode(WorkflowNodeDescriptor node) {
        if (this.nodes == null) {
            this.nodes = new ArrayList<>();
        }
        if (this.nodes.stream().noneMatch(n -> (n.getId() != null && n.getId().equals(node.getId())) ||
                                               (n.equals(node)))) {
            this.nodes.add(node);
            node.setWorkflow(this);
        }
    }

    public void removeNode(WorkflowNodeDescriptor node) {
        if(this.nodes.stream().anyMatch(n -> (n.getId() != null && n.getId().equals(node.getId())) ||
                                             (n.equals(node)))) {
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

    @XmlElementWrapper(name = "tags")
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }
    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }
    }
}
