package ro.cs.tao.docker;

import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.utils.FileUtilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class DockerVolumeMap {
    private static final String DOCKER_WORKSPACE_MOUNT_KEY = "workspace.mount";
    private static final String DOCKER_TEMP_MOUNT_KEY = "temp.mount";
    private static final String DOCKER_CFG_MOUNT_KEY = "cfg.mount";
    private static final String DOCKER_EODATA_MOUNT_KEY = "eodata.mount";
    private static final String DOCKER_ADDITIONAL_MOUNT_KEY = "additional.mount";
    private final String hostWorkspaceFolder;
    private final String containerWorkspaceFolder;
    private final String hostTemporaryFolder;
    private final String containerTemporaryFolder;
    private final String hostConfigurationFolder;
    private final String containerConfigurationFolder;
    private String hostEoDataFolder;
    private String containerEoDataFolder;
    private Map<String, String> additionalMappings;

    public DockerVolumeMap(Map<String, String> volumeMap) {
        String value = volumeMap.get(DOCKER_WORKSPACE_MOUNT_KEY);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Workspace volume map is missing!");
        }
        String[] values = safeSplit(value);
        this.hostWorkspaceFolder = values[0];
        this.containerWorkspaceFolder = values[1];

        value = volumeMap.get(DOCKER_TEMP_MOUNT_KEY);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Temp volume map is missing!");
        }
        values = safeSplit(value);
        this.hostTemporaryFolder = values[0];
        this.containerTemporaryFolder = values[1];

        value = volumeMap.get(DOCKER_CFG_MOUNT_KEY);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Configuration volume map is missing!");
        }
        values = safeSplit(value);
        this.hostConfigurationFolder = values[0];
        this.containerConfigurationFolder = values[1];

        value = volumeMap.get(DOCKER_EODATA_MOUNT_KEY);
        if (StringUtils.isNotEmpty(value)) {
            values = safeSplit(value);
            this.hostEoDataFolder = values[0];
            this.containerEoDataFolder = values[1];
        }

        value = volumeMap.get(DOCKER_ADDITIONAL_MOUNT_KEY);
        if (StringUtils.isNotEmpty(value)) {
            final String[] maps = value.split(";");
            for (String v : maps) {
                values = safeSplit(v);
                if (this.additionalMappings == null) {
                    this.additionalMappings = new LinkedHashMap<>();
                }
                this.additionalMappings.put(values[0], values[1]);
            }
        }
    }

    public String getHostWorkspaceFolder() {
        return hostWorkspaceFolder;
    }

    public String getContainerWorkspaceFolder() {
        return containerWorkspaceFolder;
    }

    public String getHostTemporaryFolder() {
        return hostTemporaryFolder;
    }

    public String getContainerTemporaryFolder() {
        return containerTemporaryFolder;
    }

    public String getHostConfigurationFolder() {
        return hostConfigurationFolder;
    }

    public String getContainerConfigurationFolder() {
        return containerConfigurationFolder;
    }

    public String getHostEODataFolder() { return hostEoDataFolder; }

    public String getContainerEoDataFolder() { return containerEoDataFolder; }

    public Map<String, String> getAdditionalMappings() {
        return additionalMappings;
    }

    public String relativizePath(String path) {
        if (path.startsWith(getContainerWorkspaceFolder()) &&
                !path.startsWith(FileUtilities.asUnixPath(SystemVariable.ROOT.value(), true))) {
            // Path is already inside the docker container
            return path;
        }
        String result;
        final String strPath = path.startsWith("[") ? path.substring(1, path.length() - 1) : path;
        try {
            Path p = FileUtilities.toPath(strPath).toAbsolutePath();
            Path absPath = FileUtilities.resolveSymLinks(FileUtilities.toPath(strPath)).toAbsolutePath();
            if (Files.isSymbolicLink(p)) {
                // If the path is a symlink to a /eodata product, maybe we want to temporarily copy it locally
                if (!absPath.startsWith(getHostEODataFolder())) {
                    p = absPath;
                }
            } else {
                p = FileUtilities.resolveSymLinks(p);
            }
            final Path root;
            final String mountPoint;
            if (getHostEODataFolder() != null && p.startsWith(getHostEODataFolder())) {
                root = Paths.get(getHostEODataFolder());
                mountPoint = getContainerEoDataFolder();
            } else {
                root = FileUtilities.resolveSymLinks(Paths.get(SystemVariable.ROOT.value()).toAbsolutePath());
                mountPoint = getContainerWorkspaceFolder();
            }
            final String unixPath = FileUtilities.asUnixPath(root.relativize(p), true);
            result = mountPoint + (mountPoint.endsWith("/") ? "" : "/") + (unixPath.startsWith("/") ? unixPath.substring(1) : unixPath);
        } catch (Exception e1) {
            e1.printStackTrace();
            result = strPath;
        }
        return result;
    }

    public String[] safeSplit(String input) {
        String[] values;
        int i = input.indexOf(':');
        if ((i = input.indexOf(':', i + 1)) > 0) {
            values = new String[2];
            values[0] = input.substring(0, i);
            values[1] = input.substring(i + 1);
        } else {
            values = input.split(":");
        }
        return values;
    }
}
