package ro.cs.tao.datasource.param;

import java.util.Objects;

/**
 * Descriptor for a parameter name properties.
 * A parameter has:
 * - a remote name (how it is defined at the remote data source)
 * - a local name (how it is used in TAO)
 * - a label (useful for visualisation renderings)
 * - a description (useful for visualisation renderings)
 *
 * @author Cosmin Cara
 */
public class ParameterName {
    private final String systemName;
    private final String label;
    private final String description;
    private final String remoteName;

    public static ParameterName create(String systemName, String remoteName) {
        return new ParameterName(systemName, remoteName);
    }

    public static ParameterName create(String systemName, String remoteName, String label) {
        return new ParameterName(systemName, remoteName, label);
    }

    public static ParameterName create(String systemName, String remoteName, String label, String description) {
        return new ParameterName(systemName, remoteName, label, description);
    }

    private ParameterName(String systemName, String remoteName) {
        this(systemName, remoteName, systemName, null);
    }

    private ParameterName(String systemName, String remoteName, String label) {
        this(systemName, remoteName, label, null);
    }

    private ParameterName(String systemName, String remoteName, String label, String description) {
        this.systemName = systemName;
        this.label = label;
        this.description = description;
        this.remoteName = remoteName;
    }

    public String getSystemName() { return systemName; }

    public String getLabel() { return label; }

    public String getDescription() { return description; }

    public String getRemoteName() { return remoteName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterName that = (ParameterName) o;
        return Objects.equals(systemName, that.systemName) && Objects.equals(remoteName, that.remoteName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(systemName, remoteName);
    }
}
