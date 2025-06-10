package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.utils.DockerHelper;

import java.io.IOException;
import java.util.List;

/**
 * BaseSingletonInstaller is an abstract class that serves as the foundation for creating and managing
 * Docker-based singleton utility containers within a system. It extends the functionality of
 * {@code BaseUtilityInstaller} and implements the {@code SingletonContainer} interface, enabling the
 * management of lifecycle operations for Docker containers required for the utility's operation.
 *
 * This class provides implementation for critical container lifecycle methods, such as starting
 * the container, performing installation, and shutting it down. It ensures that Docker is available
 * on the system, as its functionality depends on Docker management capabilities.
 *
 * The primary responsibilities of this class include:
 * - Starting a Docker container in daemon mode with specific startup arguments.
 * - Overriding the installation process to set metadata for the created container (type and visibility).
 * - Managing shutdown operations for the Docker container.
 *
 * Subclasses are required to implement the method {@code startupArguments()} to define the specific
 * startup arguments needed for initializing the Docker container.
 */
public abstract class BaseSingletonInstaller extends BaseUtilityInstaller
        implements SingletonContainer {

    public BaseSingletonInstaller() {
        super();
    }

    @Override
    public String start() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        final String name = SystemPrincipal.instance().getName();
        return DockerManager.runDaemon(getContainerName(), name, null, name, startupArguments());
    }

    @Override
    public Container install() throws IOException {
        final Container container = super.install();
        if (container != null) {
            container.setType(ContainerType.UTILITY);
            container.setVisibility(ContainerVisibility.PUBLIC);
        }
        return container;
    }

    @Override
    public void shutdown() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        DockerManager.stopInstances(getContainerName());
    }

    /**
     * Provides the startup arguments required for initializing a Docker container in daemon mode
     * for the utility. This method must be implemented by subclasses to define the specific
     * arguments needed for the container's configuration and runtime behavior.
     *
     * @return a list of strings representing the startup arguments for the Docker container.
     */
    protected abstract List<String> startupArguments();

}
