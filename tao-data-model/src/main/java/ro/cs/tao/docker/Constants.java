package ro.cs.tao.docker;

/**
 * A utility class that defines constant keys used across the system, primarily related to
 * Docker container configurations and plugin settings. These constants are typically used
 * for accessing configuration values.
 */
public class Constants {
    public static final String FORCE_MEMORY_REQUIREMENTS_KEY = "tao.force.memory.requirements";
    public static final String DOCKER_MASTER_MAPPINGS = "master.docker.mappings";
    public static final String DOCKER_WORKER_MAPPINGS = "node.docker.mappings";
    public static final String PLUGINS_USE_DOCKER = "plugins.use";
    public static final String DOCKER_REGISTRY = "tao.docker.registry";
}
