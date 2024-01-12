package ro.cs.tao.workspaces;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;

import javax.xml.bind.annotation.XmlTransient;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.Callable;

public class Repository extends StringIdentifiable {
    private String name;
    private String description;
    private String urlPrefix;
    private String userId;
    private RepositoryType type;
    private LinkedHashMap<String, String> parameters;
    private boolean readOnly;
    private boolean system;
    private boolean editable;
    private short order;
    private boolean persistentStorage;
    private LocalDateTime created;

    @XmlTransient
    private Callable<String> rootFunctor;
    @XmlTransient
    private Callable<String> bucketFunctor;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public short getOrder() {
        return order;
    }

    public void setOrder(short order) {
        this.order = order;
    }

    public boolean isPersistentStorage() {
        return persistentStorage;
    }

    public void setPersistentStorage(boolean persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setRootFunctor(Callable<String> rootFunctor) {
        this.rootFunctor = rootFunctor;
    }

    public void setBucketFunctor(Callable<String> bucketFunctor) {
        this.bucketFunctor = bucketFunctor;
    }

    /**
     * Returns the root of this workspace
     */
    public String root() {
        try {
            return this.rootFunctor == null
                    ? this.type != null && this.parameters != null ? this.parameters.get(this.type.rootKey()) : null
                    : this.rootFunctor.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRoot(String path) {
        return StringUtilities.isNullOrEmpty(path) || "/".equals(path);
    }

    /**
     * Returns the bucket of this workspace (if the workspace is not local) or the root folder (if the workspace is local)
     */
    public String bucketName() {
        if (this.bucketFunctor != null) {
            try {
                return this.bucketFunctor.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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
        String workspaceRoot = root();
        if (workspaceRoot.endsWith("/")) {
            workspaceRoot = workspaceRoot.substring(0, workspaceRoot.length() - 1);
        }
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
        if (StringUtilities.isNullOrEmpty(path) || path.equals(bucketName)) {
            return "";
        }
        String relPath = resolve(path).replaceFirst(bucketName, "");
        /*if (path.startsWith(bucketName)) {
            return path.substring(bucketName.length() + 1);
        } else if (!path.startsWith("/")) {
            return path;
        }
        final String relPath = ("/".equals(path) ? root() : resolve(path)).replaceFirst(root(), "").replaceFirst(bucketName + "/", "");*/
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
            return path.substring(1);
        }
        String commonPath = FileUtilities.findCommonPath(root(), path);
        boolean wasNull = false;
        if (commonPath == null) {
            wasNull = true;
            /*if (root().contains("/") && root().startsWith(bucketName())) {
                commonPath = root().replace(bucketName() + "/", "");
                if (!commonPath.endsWith("/")) {
                    commonPath += "/";
                }
            } else {*/
                commonPath = "";
            //}
        }
        return wasNull ? commonPath + path : path.replace(commonPath + "/", "");
    }

    /**
     * Relativize the second path to the first path.
     * @param refPath       The reference path
     * @param otherPath     The path to relativize
     */
    public String relativize(String refPath, String otherPath) {
        if (StringUtilities.isNullOrEmpty(otherPath) || (refPath.charAt(0) != '/' && otherPath.charAt(0) == '/')) {
            return "";
        }
        final String commonPath = FileUtilities.findCommonPath(refPath.charAt(0) == '/' ? refPath.substring(1) : refPath, otherPath);
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
