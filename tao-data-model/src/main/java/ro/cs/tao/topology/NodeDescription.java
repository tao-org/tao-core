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
package ro.cs.tao.topology;

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
public class NodeDescription {
    private String hostName;
    private String userName;
    private String userPass;
    private int processorCount;
    private int memorySizeGB;
    private int diskSpaceSizeGB;
    private String description;
    private Boolean active;
    private List<NodeServiceStatus> servicesStatus;

    public NodeDescription() { this.active = true;}

    @XmlElement(name = "hostName")
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @XmlElement(name = "userName")
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @XmlElement(name = "password")
    public String getUserPass() {
        return userPass;
    }
    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    @XmlElement(name = "processors")
    public int getProcessorCount() {
        return processorCount;
    }
    public void setProcessorCount(int processorCount) {
        this.processorCount = processorCount;
    }

    @XmlElement(name = "memory")
    public int getMemorySizeGB() { return memorySizeGB; }
    public void setMemorySizeGB(int memorySizeGB) { this.memorySizeGB = memorySizeGB; }

    @XmlElement(name = "diskSpace")
    public int getDiskSpaceSizeGB() { return diskSpaceSizeGB; }
    public void setDiskSpaceSizeGB(int diskSpaceSizeGB) { this.diskSpaceSizeGB = diskSpaceSizeGB; }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

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
}
