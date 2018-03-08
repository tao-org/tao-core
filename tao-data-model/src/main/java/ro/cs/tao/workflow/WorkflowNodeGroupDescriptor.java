/*
 * Copyright (C) 2017 CS ROMANIA
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
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
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
