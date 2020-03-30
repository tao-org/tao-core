package ro.cs.tao.execution;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecutionConfiguration {
    private static Boolean forceMemoryRequirements;
    private static String dockerMasterBindMount;
    private static String dockerNodeBindMount;
    private static String dockerTempMount;
    private static String dockerCfgMount;
    private static String containerMount;
    private static String dockerRegistry;
    private static Boolean useDocker;
    private static Boolean devMode;

    public static boolean developmentModeEnabled() {
        if (devMode == null) {
            devMode = Boolean.parseBoolean(ConfigurationManager.getInstance().getValue("tao.dev.mode", "false"));
        }
        return devMode;
    }

    public static boolean forceMemoryConstraint() {
        if (forceMemoryRequirements == null) {
            forceMemoryRequirements = Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(Constants.FORCE_MEMORY_REQUIREMENTS_KEY,
                                                                                                           "false"));
        }
        return forceMemoryRequirements;
    }

    public static String getDockerMasterBindMount() throws IOException {
        if (dockerMasterBindMount == null) {
            dockerMasterBindMount = ConfigurationManager.getInstance()
                                                        .getValue(Constants.DOCKER_BIND_MOUNT_MASTER_CONFIG_KEY,
                                                                  "/mnt/tao/working_dir:/mnt").trim();
            // At least on Windows, docker doesn't handle well folder symlinks in the path
            Path path = Paths.get(dockerMasterBindMount.substring(0, dockerMasterBindMount.indexOf(':')));
            Path realPath = FileUtilities.resolveSymLinks(path);
            dockerMasterBindMount = realPath.toString().replace("\\", "/") + dockerMasterBindMount.substring(dockerMasterBindMount.indexOf(':'));
        }
        return dockerMasterBindMount;
    }

    public static String getDockerNodeBindMount() throws IOException {
        if (dockerNodeBindMount == null) {
            dockerNodeBindMount = ConfigurationManager.getInstance().getValue(Constants.DOCKER_BIND_MOUNT_SLAVE_CONFIG_KEY).trim()
                                + ":" + getContainerMount();
        }
        return dockerNodeBindMount;
    }

    public static String getDockerNodeTempMount() {
        if (dockerTempMount == null) {
            final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_BIND_MOUNT_TEMP_CONFIG_KEY);
            if (!StringUtilities.isNullOrEmpty(value)) {
                dockerTempMount = value.trim() + ":/tmp_workspace";
            }
        }
        return dockerTempMount;
    }

    public static String getDockerNodeConfigMount() {
        if (dockerCfgMount == null) {
            final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_BIND_MOUNT_CFG_CONFIG_KEY);
            if (!StringUtilities.isNullOrEmpty(value)) {
                dockerCfgMount = value.trim() + ":/cfg";
            }
        }
        return dockerCfgMount;
    }

    public static String getDockerRegistry() {
        if (dockerRegistry == null) {
            dockerRegistry = ConfigurationManager.getInstance().getValue(Constants.DOCKER_REGISTRY);
        }
        return dockerRegistry;
    }

    public static String getContainerMount() throws IOException {
        if (containerMount == null) {
            final String value = getDockerMasterBindMount();
            containerMount = value.substring(value.lastIndexOf(':') + 1);
        }
        return containerMount;
    }

    public static boolean useDocker() {
        if (useDocker == null) {
            useDocker = Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(Constants.PLUGINS_USE_DOCKER, "false"));
        }
        return useDocker;
    }
}
