package ro.cs.tao.services.model.component;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ComponentCategory;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;

import java.util.List;

/**
 * Read-only bean for a ProcessingComponent entity
 */
public class ProcessingComponentInfo {

    private String id;
    private String label;
    private String version;
    private String description;
    private String authors;
    private String copyright;
    private String nodeAffinity;
    private String containerId;
    private ProcessingComponentVisibility visibility;
    private boolean active;
    private ProcessingComponentType componentType;
    private ComponentCategory category;
    private boolean managedOutput;
    private List<String> tags;

    public ProcessingComponentInfo(ProcessingComponent component) {
        this.id = component.getId();
        this.label = component.getLabel();
        this.version = component.getVersion();
        this.description = component.getDescription();
        this.authors = component.getAuthors();
        this.copyright = component.getCopyright();
        this.nodeAffinity = component.getNodeAffinity() != null ? component.getNodeAffinity().getValue() : null;
        this.containerId = component.getContainerId();
        this.visibility = component.getVisibility();
        this.active = component.getActive();
        this.componentType = component.getComponentType();
        this.category = component.getCategory();
        if (this.category == null) {
            this.category = ComponentCategory.MISC;
        }
        this.tags = component.getTags();
        this.managedOutput = component.isOutputManaged();
    }

    public String getId() { return id; }

    public String getLabel() { return label; }

    public String getVersion() { return version; }

    public String getDescription() { return description; }

    public String getAuthors() { return authors; }

    public String getCopyright() { return copyright; }

    public String getNodeAffinity() { return nodeAffinity; }

    public String getContainerId() { return containerId; }

    public ProcessingComponentVisibility getVisibility() { return visibility; }

    public boolean isActive() { return active; }

    public ProcessingComponentType getComponentType() { return componentType; }

    public ComponentCategory getCategory() {
        return category;
    }

    public boolean isManagedOutput() {
        return managedOutput;
    }

    public void setManagedOutput(boolean managedOutput) {
        this.managedOutput = managedOutput;
    }

    public List<String> getTags() { return tags; }
}
