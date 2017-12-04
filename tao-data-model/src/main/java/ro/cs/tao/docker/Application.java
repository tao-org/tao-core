package ro.cs.tao.docker;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Descriptor for an application inside a Docker container.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "application")
public class Application {

    private String path;
    private String name;

    @XmlElement(name = "path")
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
