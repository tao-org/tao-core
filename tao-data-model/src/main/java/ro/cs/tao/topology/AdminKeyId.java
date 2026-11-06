package ro.cs.tao.topology;

import java.io.Serializable;
import java.util.Objects;

public class AdminKeyId implements Serializable {
    private String userId;
    private String nodeId;

    public AdminKeyId() {
    }

    public AdminKeyId(String userId, String nodeId) {
        this.userId = userId;
        this.nodeId = nodeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdminKeyId that = (AdminKeyId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, nodeId);
    }
}
