package ro.cs.tao.workspaces;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Objects;

public class Repository extends StringIdentifiable {
    private String name;
    private String description;
    private String urlPrefix;
    private String userName;
    private RepositoryType type;
    private LinkedHashMap<String, String> parameters;
    private boolean readOnly;
    private boolean system;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    /**
     * Returns the root of this workspace
     */
    public String root() {
        return this.type != null && this.parameters != null ? this.parameters.get(this.type.rootKey()) : null;
    }

    public boolean isRoot(String path) {
        return StringUtilities.isNullOrEmpty(path) || "/".equals(path);
    }

    /**
     * Returns the bucket of this workspace (if the workspace is not local) or the root folder (if the workspace is local)
     */
    public String bucketName() {
        final String root = root();
        final int idx = root.lastIndexOf('/');
        return idx > 0 ? root.substring(0, idx) : root;
    }

    /**
     * Returns the full path of the given relative path.
     *
     * @param relativePath The path
     */
    public String resolve(String relativePath) {
        final String workspaceRoot = root();
        if (StringUtilities.isNullOrEmpty(relativePath) || "/".equals(relativePath)) {
            return workspaceRoot;
        }
        return relativePath.startsWith(workspaceRoot)
                ? relativePath
                : relativePath.startsWith("/")
                    ? workspaceRoot + relativePath
                    : workspaceRoot + "/" + relativePath;
    }

    /**
     * Relativize the given path to the bucket of the workspace (full path minus the bucket)
     *
     * @param path  The path
     */
    public String relativizeToBucket(String path) {
        final String bucketName = bucketName();
        if (StringUtilities.isNullOrEmpty(path) || path.equals(bucketName) || path.equals(bucketName + "/")) {
            return "";
        }
        final String relPath = ("/".equals(path) ? root() : resolve(path)).replaceFirst(bucketName, "");
        return relPath.startsWith("/") ? relPath.substring(1) : relPath;
    }

    /**
     * Relativize the given path to the root of the workspace
     *
     * @param path  The path
     */
    public String relativizeToRoot(String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return root();
        }
        if (path.startsWith("/")) {
            return path;
        }
        final String commonPath = FileUtilities.findCommonPath(root(), path);
        return commonPath != null ? path.replace(commonPath + "/", "") : path;
    }

    /**
     * Relativize the second path to the first path.
     * @param refPath       The reference path
     * @param otherPath     The path to relativize
     */
    public String relativize(String refPath, String otherPath) {
        if (StringUtilities.isNullOrEmpty(otherPath) || otherPath.startsWith("/")) {
            return "";
        }
        final String commonPath = FileUtilities.findCommonPath(refPath, otherPath);
        return commonPath != null ? otherPath.replace(commonPath + "/", "") : otherPath;
    }

    /**
     * Returns the file name (the last element of the path)
     * @param path  The path
     */
    public String fileName(String path) {
        final int lastSlashIdx = path.lastIndexOf('/', path.length() - 2);
        return lastSlashIdx > 0 ? path.substring(lastSlashIdx + 1) : path.replace("/", "");
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
        if (!(o instanceof Repository)) return false;
        if (!super.equals(o)) return false;
        Repository workspace = (Repository) o;
        return Objects.equals(id, workspace.id) || (name.equals(workspace.name) && description.equals(workspace.description) && urlPrefix.equals(workspace.urlPrefix) && type == workspace.type && Objects.equals(parameters, workspace.parameters));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, urlPrefix, type, parameters);
    }
}
