package ro.cs.tao.utils.executors.container;

import java.util.*;

/**
 * Represents a container configuration for execution through containerization tools such as Docker or Kubernetes.
 * This class encapsulates all metadata and settings required to define and execute a container instance.
 *
 * Fields include the container type, container name, container registry, volume mappings,
 * environment variables, and container-specific arguments.
 */
public class ContainerUnit {
    private final ContainerType type;
    private String containerName;
    private String containerRegistry;
    private Map<String, String> volumeMap;
    private Map<String, String> environmentVariables;
    private List<String> arguments;

    /**
     * Constructs a new ContainerUnit instance with the specified container type.
     *
     * @param type the type of the container, defining whether it is a Docker or Kubernetes container
     */
    public ContainerUnit(ContainerType type) {
        this.type = type;
    }

    /**
     * Adds a volume mapping to the container.
     * The mapping associates a source path on the host system with a target path within the container.
     *
     * @param source the source path on the host system to be mapped, represented as a {@code String}
     * @param target the target path within the container, represented as a {@code String}
     */
    public void addVolumeMapping(String source, String target) {
        if (this.volumeMap == null) {
            this.volumeMap = new LinkedHashMap<>();
        }
        this.volumeMap.put(source, target);
    }

    /**
     * Adds an environment variable to the container configuration.
     * This method associates the given name with the specified value, ensuring
     * the variable is included in the environment settings during container execution.
     *
     * @param name the name of the environment variable, represented as a {@code String}
     * @param value the value of the environment variable, represented as a {@code String}
     */
    public void addEnvironmentVariable(String name, String value) {
        if (this.environmentVariables == null) {
            this.environmentVariables = new HashMap<>();
        }
        this.environmentVariables.put(name, value);
    }

    /**
     * Sets the environment variables for the container configuration.
     * The environment variables are passed as a map where each key-value pair
     * represents the variable name and its corresponding value.
     *
     * @param environmentVariables a {@code Map<String, String>} containing the environment
     *                             variables to be set, with the keys as variable names
     *                             and the values as variable values
     */
    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * Adds an argument to the list of arguments for the container.
     * This method appends the provided argument name and its associated value
     * to the container's argument list.
     *
     * @param name  the name of the argument, represented as a {@code String}.
     * @param value the value of the argument, represented as a {@code String}.
     */
    public void addArgument(String name, String value) {
        if (this.arguments == null) {
            this.arguments = new ArrayList<>();
        }
        this.arguments.add(name);
        this.arguments.add(value);
    }

    /**
     * Sets the list of arguments for the container.
     * These arguments are utilized during the execution of the container,
     * providing additional configuration or runtime parameters.
     *
     * @param arguments a {@code List<String>} containing the arguments
     *                  to be set for the container. Each element in the list
     *                  represents an individual argument.
     */
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * Sets the container name for the container unit.
     *
     * @param containerName the name of the container, represented as a {@code String}
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * Sets the container registry for the container unit.
     * The container registry defines the source location for container images
     * and will typically be a URL or a registry name.
     *
     * @param containerRegistry the container registry, represented as a {@code String}
     */
    public void setContainerRegistry(String containerRegistry) {
        this.containerRegistry = containerRegistry;
    }

    /**
     * Retrieves the type of the container.
     * The container type defines whether it is a Docker or Kubernetes container.
     *
     * @return the type of the container as a {@code ContainerType}
     */
    public ContainerType getType() {
        return type;
    }

    /**
     * Retrieves the name of the container.
     *
     * @return the name of the container as a {@code String}
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Retrieves the container registry associated with this container unit.
     * The container registry specifies the source location for container images
     * and is typically represented as a URL or registry name.
     *
     * @return the container registry as a {@code String}
     */
    public String getContainerRegistry() {
        return containerRegistry;
    }

    /**
     * Retrieves the volume mappings associated with the container.
     * The returned map represents the volume bindings where each key is the source path on the host system
     * and each value is the target path within the container.
     *
     * @return a map of volume mappings as a {@code Map<String, String>} where keys are source paths
     *         and values are target paths.
     */
    public Map<String, String> getVolumeMap() {
        return volumeMap;
    }

    /**
     * Retrieves the environment variables associated with the container.
     * Each entry in the returned map represents an environment variable,
     * where the key is the variable name and the value is the variable value.
     *
     * @return a map of environment variables as a {@code Map<String, String>}
     *         where keys are variable names and values are variable values.
     */
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Retrieves the list of arguments associated with the container.
     * These arguments are generally used to configure or provide runtime parameters
     * for the container during its execution.
     *
     * @return a list of arguments as a {@code List<String>}, where each element
     *         represents an individual argument.
     */
    public List<String> getArguments() {
        return arguments;
    }
}
