package ro.cs.tao.docker;

import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.utils.FileUtilities;
import tools.jackson.core.JacksonException;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility class for managing and accessing execution configurations, particularly related
 * to Docker usage and configurations. This class provides methods to determine the
 * execution environment, Docker registry settings, volume mappings, and memory constraints.
 */
public class ExecutionConfiguration {
    private static Boolean useDocker;

    /**
     * Checks whether the development mode is enabled in the configuration.
     * The method retrieves the value of the configuration property "tao.dev.mode" and parses it as a boolean.
     * If the property is not set, the default value "false" is used.
     *
     * @return true if development mode is enabled, false otherwise
     */
    public static boolean developmentModeEnabled() {
        return Boolean.parseBoolean(ConfigurationManager.getInstance().getValue("tao.dev.mode", "false"));
    }

    /**
     * Forces memory constraints by checking the system configuration.
     * The method retrieves the value of the configuration property
     * "tao.force.memory.requirements" and parses it as a boolean. If the property
     * is not set, the default value "false" is used.
     *
     * @return true if memory constraints are forced according to the configuration, false otherwise
     */
    public static boolean forceMemoryConstraint() {
        return Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(Constants.FORCE_MEMORY_REQUIREMENTS_KEY,
                                                                                "false"));
    }

    /**
     * Retrieves and constructs the volume mappings for the Docker master container.
     * The volume mappings are obtained from the configuration and processed based on the platform
     * and volume mapping style.
     *
     * @return An instance of {@link DockerVolumeMap} containing the master container's
     *         volume mappings composed of source and destination paths.
     * @throws RuntimeException if no volume mappings are found or if there's an error
     *         when parsing the configuration.
     */
    public static DockerVolumeMap getMasterContainerVolumeMap() {
        final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_MASTER_MAPPINGS);
        if (value == null) {
            throw new RuntimeException("No volume mappings found for master!");
        }
        Map<String, String> map = new HashMap<>();
        try {
            map = JsonMapper.instance().readerFor(map.getClass()).readValue(value);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
        final boolean isDockerVolumeStyle = SystemUtils.IS_OS_LINUX ||
                ConfigurationManager.getInstance()
                                    .getValue("docker.volume.map.style", "docker")
                                    .equals("docker");
        return new DockerVolumeMap(map.entrySet().stream()
                                      .collect(Collectors.toMap(Map.Entry::getKey,
                                            e -> Arrays.stream(e.getValue().split("\\|"))
                                                        .map(m -> {
                                                            final String[] values = m.split(":", 2);
                                                            return (isDockerVolumeStyle
                                                                    ? FileUtilities.asUnixPath(Paths.get(values[0]), false)
                                                                    : Paths.get(values[0]).toAbsolutePath())
                                                                    + ":" + values[1];
                                                        }).collect(Collectors.joining("|"))
                                            )));
    }

    /**
     * Retrieves and constructs the volume mappings for the Docker master container.
     * The method processes and formats the stored volume mappings based on whether
     * the execution is running inside a Docker container.
     *
     * @param insideDocker Indicates whether the execution is running inside a Docker container.
     *                     If true, the paths are formatted accordingly for a Unix-like system.
     * @return An instance of {@link DockerVolumeMap} containing the master container's volume mappings,
     *         composed of source and destination paths.
     * @throws RuntimeException if no volume mappings are found or if there is an error while parsing the configuration.
     */
    public static DockerVolumeMap getMasterContainerVolumeMap(boolean insideDocker) {
        final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_MASTER_MAPPINGS);
        if (value == null) {
            throw new RuntimeException("No volume mappings found for master!");
        }
        Map<String, String> map = new HashMap<>();
        try {
            map = JsonMapper.instance().readerFor(map.getClass()).readValue(value);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

        return new DockerVolumeMap(map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> Arrays.stream(e.getValue().split("\\|"))
                                    .map(m -> {
                                        final String[] values = m.split(":", 2);
                                        return (insideDocker
                                                ? FileUtilities.asUnixPath(Paths.get(values[0]), insideDocker)
                                                : Paths.get(values[0]).toAbsolutePath())
                                                + ":" + values[1];
                                    }).collect(Collectors.joining("|"))
                        )));
    }

    /**
     * Retrieves and constructs the volume mappings for the Docker worker container.
     * The volume mappings are obtained from the configuration and parsed into a map of source
     * and destination paths. A {@link DockerVolumeMap} instance is created based on this data.
     *
     * @return An instance of {@link DockerVolumeMap} containing the worker container's
     *         volume mappings composed of source and destination paths.
     * @throws RuntimeException if no volume mappings are found or if an error occurs during
     *         configuration parsing.
     */
    public static DockerVolumeMap getWorkerContainerVolumeMap() {
        final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_WORKER_MAPPINGS);
        if (value == null) {
            throw new RuntimeException("No volume mappings found for worker nodes!");
        }
        Map<String, String> map = new HashMap<>();
        try {
            map = JsonMapper.instance().readerFor(map.getClass()).readValue(value);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
        return new DockerVolumeMap(map);
    }

    /**
     * Retrieves the Docker registry address configured in the system.
     * The registry address is obtained using the key {@code tao.docker.registry}
     * from the configuration manager.
     *
     * @return The Docker registry address as a string, or null if the registry is not configured.
     */
    public static String getDockerRegistry() {
        return ConfigurationManager.getInstance().getValue(Constants.DOCKER_REGISTRY);
    }

    /**
     * Determines whether Docker is being used for plugin execution based on the system configuration.
     * The method retrieves the value of the configuration property "plugins.use" using the
     * {@link ConfigurationManager}, and compares it against the string "docker".
     * The result is cached to improve subsequent access performance.
     *
     * @return true if the configuration indicates the usage of Docker for plugins, false otherwise
     */
    public static boolean useDocker() {
        if (useDocker == null) {
            useDocker = "docker".equals(ConfigurationManager.getInstance().getValue(Constants.PLUGINS_USE_DOCKER, ""));
        }
        return useDocker;
    }
}
