package ro.cs.tao.topology;

import ro.cs.tao.component.IntIdentifiable;

import java.util.HashMap;
import java.util.Objects;

public class Workspace extends IntIdentifiable {
    private String name;
    private String description;
    private String urlPrefix;
    private String userName;
    private WorkspaceType type;
    private HashMap<String, String> parameters;
    private boolean readOnly;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public WorkspaceType getType() {
        return type;
    }

    public void setType(WorkspaceType type) {
        this.type = type;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void addParameter(String key, String value) {
        if (!this.type.getParameters().contains(key)) {
            throw new IllegalArgumentException("[" + key + "] not supported");
        }
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
    }

    public String getValue(String key) {
        if (!this.type.getParameters().contains(key)) {
            throw new IllegalArgumentException("[" + key + "] not supported");
        }
        return this.parameters != null ? this.parameters.get(key) : null;
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
