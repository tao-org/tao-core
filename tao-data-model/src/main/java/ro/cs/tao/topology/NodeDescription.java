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
package ro.cs.tao.topology;

import ro.cs.tao.component.StringIdentifiable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * A node represents a computer (physical or virtual) on which TAO processing components will run.
 *
 * @author  Cosmin Udroiu
 */
@XmlRootElement(name = "node")
public class NodeDescription extends StringIdentifiable {
    private String userName;
    private String userPass;
    private NodeFlavor flavor;
    private String description;
    private NodeRole role;
    private Boolean active;
    private Boolean isVolatile;
    private List<NodeServiceStatus> servicesStatus;
    private List<String> tags;
    private String owner;
    private String appId;

    public NodeDescription() { this.active = true;}

    @Override
    public String defaultId() { return "NewNode"; }

    @XmlElement(name = "userName")
    public String getUserName() { return userName; }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @XmlElement(name = "password")
    public String getUserPass() { return userPass; }
    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    @XmlElement(name = "flavor")
    public NodeFlavor getFlavor() { return flavor; }
    public void setFlavor(NodeFlavor flavor) { this.flavor = flavor; }

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "role")
    public NodeRole getRole() { return role; }
    public void setRole(NodeRole role) { this.role = role; }

    @XmlElement(name = "active")
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    @XmlElement(name = "volatile")
    public Boolean getVolatile() { return isVolatile; }
    public void setVolatile(Boolean value) { isVolatile = value; }

    @XmlElement(name = "owner")
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    @XmlElement(name = "appId")
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    @XmlElementWrapper(name = "services")
    public List<NodeServiceStatus> getServicesStatus() {
        return this.servicesStatus != null ? this.servicesStatus : new ArrayList<>();
    }
    public void setServicesStatus(List<NodeServiceStatus> servicesStatus) { this.servicesStatus = servicesStatus; }

    public void addServiceStatus(NodeServiceStatus serviceStatus) {
        if (this.servicesStatus == null) {
            this.servicesStatus = new ArrayList<>();
        }
        this.servicesStatus.add(serviceStatus);
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
