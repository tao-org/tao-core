package ro.cs.tao.topology;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cosmin on 7/17/2017.
 */
@XmlRootElement(name = "node")
public class NodeDescription {
    private String hostName;
    private String userName;
    private String userPass;
    private int processorCount;
    private int memorySizeGB;
    private int diskSpaceSizeGB;

    public NodeDescription() { }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @XmlElement(name = "hostName")
    public String getHostName() {
        return hostName;
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

    public void setProcessorCount(int processorCount) {
        this.processorCount = processorCount;
    }

    @XmlElement(name = "processors")
    public int getProcessorCount() {
        return processorCount;
    }

    @XmlElement(name = "memory")
    public int getMemorySizeGB() { return memorySizeGB; }

    public void setMemorySizeGB(int memorySizeGB) { this.memorySizeGB = memorySizeGB; }

    @XmlElement(name = "diskSpace")
    public int getDiskSpaceSizeGB() { return diskSpaceSizeGB; }

    public void setDiskSpaceSizeGB(int diskSpaceSizeGB) { this.diskSpaceSizeGB = diskSpaceSizeGB; }
}
