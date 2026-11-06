package ro.cs.tao.utils.executors.container;

import ro.cs.tao.utils.StringUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides utility methods to construct command-line arguments required
 * for executing containers using different containerization platforms such as Docker or Kubernetes.
 *
 * This class contains a single static method to generate a list of arguments
 * to be used when starting a containerized application. The arguments are tailored
 * based on the container type and its associated configuration.
 */
public class ContainerCmdBuilder {

    /**
     * Builds a list of command-line arguments required to execute a containerized application
     * using the specified containerization platform (e.g., Docker or Kubernetes).
     *
     * The arguments are constructed based on the type of container and its configuration,
     * such as environment variables, volume mappings, container registry, and additional arguments.
     *
     * @param unit the {@code ContainerUnit} object containing the configuration and details
     *             of the container for which the command-line arguments are built
     * @return a list of strings representing the command-line arguments necessary
     *         for executing the containerized application
     */
    public static List<String> buildCommandLineArguments(ContainerUnit unit) {
        final List<String> args = new ArrayList<>();
        switch (unit.getType()) {
            case KUBERNETES:
                // TODO
                break;
            case DOCKER:
                args.add("docker");
                args.add("run");
                args.addAll(unit.getArguments());
                final Map<String, String> variables = unit.getEnvironmentVariables();
                if (variables != null) {
                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                        args.add("-e");
                        if (entry.getValue() != null) {
                            args.add(entry.getKey() + "=" + entry.getValue());
                        } else {
                            args.add(entry.getKey());
                        }
                    }
                }
                final Map<String, String> volumeMap = unit.getVolumeMap();
                if (volumeMap != null) {
                    for (Map.Entry<String, String> entry : volumeMap.entrySet()) {
                        args.add("-v");
                        args.add(entry.getKey() + ":" + entry.getValue());
                    }
                }
                final String registry = unit.getContainerRegistry();
                if (!StringUtilities.isNullOrEmpty(registry)) {
                    args.add(registry + "/" + unit.getContainerName());
                } else {
                    args.add(unit.getContainerName());
                }
            default:
                break;
        }
        return args;
    }
}
