package ro.cs.tao.execution;

import ro.cs.tao.configuration.Configuration;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecutionConfiguration {
    private static Boolean forceMemoryRequirements;
    private static String dockerMasterBindMount;
    private static String dockerNodeBindMount;
    private static String containerMount;
    private static String dockerRegistry;
    private static Boolean useDocker;

    public static boolean forceMemoryConstraint() {
        if (forceMemoryRequirements == null) {
            forceMemoryRequirements = Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(Configuration.DRMAA.FORCE_MEMORY_REQUIREMENTS,
                                                                                                       "false"));
        }
        return forceMemoryRequirements;
    }

    public static String getDockerMasterBindMount() throws IOException {
        if (dockerMasterBindMount == null) {
            dockerMasterBindMount = ConfigurationManager.getInstance()
                                                        .getValue(Configuration.Docker.CONTAINER_MOUNT,
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
            dockerNodeBindMount = ConfigurationManager.getInstance().getValue(Configuration.FileSystem.NODE_SHARE_MOUNT).trim()
                                + ":" + getContainerMount();
        }
        return dockerNodeBindMount;
    }

    public static String getDockerRegistry() {
        if (dockerRegistry == null) {
            dockerRegistry = ConfigurationManager.getInstance().getValue(Configuration.Docker.REGISTRY_URL);
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
            useDocker = Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(Configuration.Docker.PLUGINS_USE_DOCKER, "false"));
        }
        return useDocker;
    }
}
