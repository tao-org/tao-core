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
    private List<ServiceDescription> services;

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
    public List<ServiceDescription> getServices() {
        return this.services != null ? this.services : new ArrayList<>();
    }
    public void setServices(List<ServiceDescription> services) { this.services = services; }
}
