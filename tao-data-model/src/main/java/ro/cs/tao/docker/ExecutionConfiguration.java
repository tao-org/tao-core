package ro.cs.tao.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.utils.FileUtilities;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecutionConfiguration {
    private static Boolean useDocker;

    public static boolean developmentModeEnabled() {
        return Boolean.parseBoolean(ConfigurationManager.getInstance().getValue("tao.dev.mode", "false"));
    }

    public static boolean forceMemoryConstraint() {
        return Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(Constants.FORCE_MEMORY_REQUIREMENTS_KEY,
                                                                                "false"));
    }

    public static DockerVolumeMap getMasterContainerVolumeMap() {
        final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_MASTER_MAPPINGS);
        if (value == null) {
            throw new RuntimeException("No volume mappings found for master!");
        }
        Map<String, String> map = new HashMap<>();
        try {
            map = JsonMapper.instance().readerFor(map.getClass()).readValue(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        final boolean isDockerVolumeStyle = SystemUtils.IS_OS_LINUX ||
                ConfigurationManager.getInstance()
                                    .getValue("docker.volume.map.style", "docker")
                                    .equals("docker");
        return new DockerVolumeMap(map.entrySet().stream()
                                      .collect(Collectors.toMap(Map.Entry::getKey,
                                            e -> {
                                                final String[] values = e.getValue().split(":");
                                                return (isDockerVolumeStyle
                                                        ? FileUtilities.asUnixPath(Paths.get(values[0]), false)
                                                        : Paths.get(values[0]).toAbsolutePath())
                                                        + ":" + values[1];
                                            })));
    }

    public static DockerVolumeMap getMasterContainerVolumeMap(boolean insideDocker) {
        final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_MASTER_MAPPINGS);
        if (value == null) {
            throw new RuntimeException("No volume mappings found for master!");
        }
        Map<String, String> map = new HashMap<>();
        try {
            map = JsonMapper.instance().readerFor(map.getClass()).readValue(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new DockerVolumeMap(map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> {
                            final String[] values = e.getValue().split(":");
                            return (insideDocker
                                    ? FileUtilities.asUnixPath(Paths.get(values[0]), insideDocker)
                                    : Paths.get(values[0]).toAbsolutePath())
                                    + ":" + values[1];
                        })));
    }

    public static DockerVolumeMap getWorkerContainerVolumeMap() {
        final String value = ConfigurationManager.getInstance().getValue(Constants.DOCKER_WORKER_MAPPINGS);
        if (value == null) {
            throw new RuntimeException("No volume mappings found for worker nodes!");
        }
        Map<String, String> map = new HashMap<>();
        try {
            map = JsonMapper.instance().readerFor(map.getClass()).readValue(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new DockerVolumeMap(map);
    }

    public static String getDockerRegistry() {
        return ConfigurationManager.getInstance().getValue(Constants.DOCKER_REGISTRY);
    }

    public static boolean useDocker() {
        if (useDocker == null) {
            useDocker = "docker".equals(ConfigurationManager.getInstance().getValue(Constants.PLUGINS_USE_DOCKER, ""));
        }
        return useDocker;
    }
}
