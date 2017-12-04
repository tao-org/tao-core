package ro.cs.tao.docker;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor for a Docker container
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "container")
public class Container {

    private String id;
    private String name;
    private String tag;
    private String applicationPath;
    private List<Application> applications;

    @XmlElement(name = "id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "tag")
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    @XmlElement(name = "applicationPath")
    public String getApplicationPath() { return applicationPath; }
    public void setApplicationPath(String value) { applicationPath = value; }

    @XmlElementWrapper(name = "applications")
    public List<Application> getApplications() {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        return applications;
    }
    public void setApplications(List<Application> applications) { this.applications = applications; }
    public void addApplication(Application application) {
        getApplications().add(application);
    }
}
