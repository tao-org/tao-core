package ro.cs.tao.services.model.component;

import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.ogc.WMSComponent;
import ro.cs.tao.docker.Container;

import java.util.List;

/**
 * Read-only bean for a WPSComponent entity
 */
public class WMSComponentInfo {

    private String id;
    private String label;
    private String version;
    private String description;
    private String authors;
    private String copyright;
    private String nodeAffinity;
    private String containerId;
    private List<String> tags;

    private ProcessingComponentVisibility visibility;

    public WMSComponentInfo(WMSComponent component) {
        this.id = component.getId();
        this.label = component.getLabel();
        this.version = component.getVersion();
        this.description = component.getDescription();
        this.authors = component.getAuthors();
        this.copyright = component.getCopyright();
        this.nodeAffinity = component.getNodeAffinity();
        this.tags = component.getTags();
        this.visibility = component.getVisibility();
        final Container service = component.getService();
        if (service != null) {
            this.containerId = service.getId();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getNodeAffinity() {
        return nodeAffinity;
    }

    public void setNodeAffinity(String nodeAffinity) {
        this.nodeAffinity = nodeAffinity;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ProcessingComponentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ProcessingComponentVisibility visibility) {
        this.visibility = visibility;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
