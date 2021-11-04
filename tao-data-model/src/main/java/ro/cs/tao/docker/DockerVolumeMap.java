package ro.cs.tao.docker;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class DockerVolumeMap {
    private static final String DOCKER_WORKSPACE_MOUNT_KEY = "workspace.mount";
    private static final String DOCKER_TEMP_MOUNT_KEY = "temp.mount";
    private static final String DOCKER_CFG_MOUNT_KEY = "cfg.mount";
    private final String hostWorkspaceFolder;
    private final String containerWorkspaceFolder;
    private final String hostTemporaryFolder;
    private final String containerTemporaryFolder;
    private final String hostConfigurationFolder;
    private final String containerConfigurationFolder;

    public DockerVolumeMap(Map<String, String> volumeMap) {
        String value = volumeMap.get(DOCKER_WORKSPACE_MOUNT_KEY);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Workspace volume map is missing!");
        }
        String[] values = value.split(":");
        this.hostWorkspaceFolder = values[0];
        this.containerWorkspaceFolder = values[1];
        value = volumeMap.get(DOCKER_TEMP_MOUNT_KEY);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Temp volume map is missing!");
        }
        values = value.split(":");
        this.hostTemporaryFolder = values[0];
        this.containerTemporaryFolder = values[1];
        value = volumeMap.get(DOCKER_CFG_MOUNT_KEY);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Configuration volume map is missing!");
        }
        values = value.split(":");
        this.hostConfigurationFolder = values[0];
        this.containerConfigurationFolder = values[1];
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
}
