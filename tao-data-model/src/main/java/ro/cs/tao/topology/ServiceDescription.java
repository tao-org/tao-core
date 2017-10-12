package ro.cs.tao.topology;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A service represents a mandatory software component that should be on a topology node.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "service")
public class ServiceDescription {
    private Integer id;
    private String name;
    private String version;
    private String description;

    public ServiceDescription() { }

    public ServiceDescription(String name, String version, String description) {
        this.name = name;
        this.version = version;
        this.description = description;
    }

    @XmlTransient
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "version")
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    @XmlElement(name = "description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}
