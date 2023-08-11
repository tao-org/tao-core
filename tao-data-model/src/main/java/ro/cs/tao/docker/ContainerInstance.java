package ro.cs.tao.docker;

import ro.cs.tao.component.StringIdentifiable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Descriptor for a Docker container instance
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "containerInstance")
public class ContainerInstance extends StringIdentifiable {
    private String containerId;
    private String name;
    private String userId;
    private Integer hostPort;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }
}
