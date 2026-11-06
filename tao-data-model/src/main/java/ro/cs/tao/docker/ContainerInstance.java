package ro.cs.tao.docker;

import jakarta.xml.bind.annotation.XmlRootElement;
import ro.cs.tao.component.StringIdentifiable;

import java.util.Map;

/**
 * Descriptor for a Docker container instance
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "containerInstance")
public class ContainerInstance extends StringIdentifiable {
    private String containerId;
    private String host;
    private String name;
    private String userId;
    private Integer port;
    private Map<String, String> properties;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}

