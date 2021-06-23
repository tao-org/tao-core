package ro.cs.tao.utils.executors.container;

import java.util.*;

public class ContainerUnit {
    private final ContainerType type;
    private String containerName;
    private String containerRegistry;
    private Map<String, String> volumeMap;
    private Map<String, String> environmentVariables;
    private List<String> arguments;

    public ContainerUnit(ContainerType type) {
        this.type = type;
    }

    public void addVolumeMapping(String source, String target) {
        if (this.volumeMap == null) {
            this.volumeMap = new LinkedHashMap<>();
        }
        this.volumeMap.put(source, target);
    }

    public void addEnvironmentVariable(String name, String value) {
        if (this.environmentVariables == null) {
            this.environmentVariables = new HashMap<>();
        }
        this.environmentVariables.put(name, value);
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public void addArgument(String name, String value) {
        if (this.arguments == null) {
            this.arguments = new ArrayList<>();
        }
        this.arguments.add(name);
        this.arguments.add(value);
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public void setContainerRegistry(String containerRegistry) {
        this.containerRegistry = containerRegistry;
    }

    public ContainerType getType() {
        return type;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getContainerRegistry() {
        return containerRegistry;
    }

    public Map<String, String> getVolumeMap() {
        return volumeMap;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public List<String> getArguments() {
        return arguments;
    }
}
