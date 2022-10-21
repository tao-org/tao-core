package ro.cs.tao.workspaces;

import ro.cs.tao.component.StringIdentifiable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public class RepositoryTemplate  extends StringIdentifiable {
    private String name;
    private String description;
    private String urlPrefix;
    private RepositoryType type;
    private LinkedHashMap<String, String> parameters;
    private LocalDateTime created;

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

    public RepositoryType getType() {
        return type;
    }

    public void setType(RepositoryType type) {
        this.type = type;
    }

    public LinkedHashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(LinkedHashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void addParameter(String key, String value) {
        if (!this.type.getParameters().containsKey(key)) {
            throw new IllegalArgumentException("[" + key + "] not supported");
        }
        if (this.parameters == null) {
            this.parameters = new LinkedHashMap<>();
        }
        this.parameters.put(key, value);
    }

    public String getValue(String key) {
        if (!this.type.getParameters().containsKey(key)) {
            throw new IllegalArgumentException("[" + key + "] not supported");
        }
        return this.parameters != null ? this.parameters.get(key) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryTemplate)) return false;
        if (!super.equals(o)) return false;
        RepositoryTemplate workspace = (RepositoryTemplate) o;
        return Objects.equals(id, workspace.id) || (name.equals(workspace.name) && description.equals(workspace.description) && urlPrefix.equals(workspace.urlPrefix) && type == workspace.type && Objects.equals(parameters, workspace.parameters));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, urlPrefix, type, parameters);
    }

    public Repository toRepository() {
        final Repository repository = new Repository();
        repository.setId(UUID.randomUUID().toString());
        repository.setName(this.name);
        repository.setDescription(this.description);
        repository.setType(this.type);
        repository.setParameters(new LinkedHashMap<>(this.parameters));
        repository.setUrlPrefix(this.urlPrefix);
        return repository;
    }
}

