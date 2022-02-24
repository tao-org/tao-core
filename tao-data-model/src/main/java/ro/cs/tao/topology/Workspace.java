package ro.cs.tao.topology;

import ro.cs.tao.component.IntIdentifiable;

import java.util.Map;
import java.util.Objects;

public class Workspace extends IntIdentifiable {
    private String name;
    private String description;
    private String urlPrefix;
    private WorkspaceType type;
    private Map<String, String> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public WorkspaceType getType() {
        return type;
    }

    public void setType(WorkspaceType type) {
        this.type = type;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String key, String value) {
        if (!this.type.getParameters().contains(key)) {
            throw new IllegalArgumentException("[" + key + "] not supported");
        }
        this.parameters.put(key, value);
    }

    public String getValue(String key) {
        if (!this.type.getParameters().contains(key)) {
            throw new IllegalArgumentException("[" + key + "] not supported");
        }
        return this.parameters.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Workspace)) return false;
        if (!super.equals(o)) return false;
        Workspace workspace = (Workspace) o;
        return name.equals(workspace.name) && description.equals(workspace.description) && urlPrefix.equals(workspace.urlPrefix) && type == workspace.type && Objects.equals(parameters, workspace.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, urlPrefix, type, parameters);
    }
}
