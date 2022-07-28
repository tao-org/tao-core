package ro.cs.tao.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.utils.FileUtilities;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecutionConfiguration {
    private static Boolean forceMemoryRequirements;
    private static String dockerRegistry;
    private static Boolean useDocker;
    private static Boolean devMode;
    private static DockerVolumeMap masterVolumeMap;
    private static DockerVolumeMap workerVolumeMap;

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

    public static DockerVolumeMap getMasterContainerVolumeMap() {
        if (masterVolumeMap == null) {
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
            map = map.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                          e -> {
                                                              final String[] values = e.getValue().split(":");
                                                              return FileUtilities.asUnixPath(Paths.get(values[0]), false) + ":" + values[1];
                                                          }));
            masterVolumeMap = new DockerVolumeMap(map);
        }
        return masterVolumeMap;
    }

    public static DockerVolumeMap getWorkerContainerVolumeMap() {
        if (workerVolumeMap == null) {
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
            workerVolumeMap = new DockerVolumeMap(map);
        }
        return workerVolumeMap;
    }

    public static String getDockerRegistry() {
        if (dockerRegistry == null) {
            dockerRegistry = ConfigurationManager.getInstance().getValue(Constants.DOCKER_REGISTRY);
        }
        return dockerRegistry;
    }

    public static boolean useDocker() {
        if (useDocker == null) {
            useDocker = "docker".equals(ConfigurationManager.getInstance().getValue(Constants.PLUGINS_USE_DOCKER, ""));
        }
        return useDocker;
    }
}
