package ro.cs.tao.topology;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import ro.cs.tao.component.Identifiable;

import java.util.Objects;

@XmlRootElement(name = "adminKey")
public class AdminKey implements Identifiable<AdminKeyId> {
    private String userId;
    private String nodeId;
    private String sshKey;

    public AdminKey() {
    }

    public AdminKey(String userId, String nodeId, String sshKey) {
        this.userId = userId;
        this.nodeId = nodeId;
        this.sshKey = sshKey;
    }

    @Override
    public AdminKeyId getId() {
        return new AdminKeyId(userId, nodeId);
    }

    @Override
    public void setId(AdminKeyId id) {
        if (id != null) {
            this.userId = id.getUserId();
            this.nodeId = id.getNodeId();
        }
    }

    @Override
    public AdminKeyId defaultId() {
        return null;
    }

    @XmlElement(name = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @XmlElement(name = "nodeId")
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @XmlElement(name = "sshKey")
    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdminKey adminKey = (AdminKey) o;
        return Objects.equals(userId, adminKey.userId) && Objects.equals(nodeId, adminKey.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, nodeId);
    }
}
