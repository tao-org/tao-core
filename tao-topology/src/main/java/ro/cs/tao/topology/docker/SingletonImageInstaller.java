package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.utils.DockerHelper;

import java.io.IOException;
import java.util.List;

/**
 * SingletonImageInstaller is an abstract base class that provides common functionality
 * for managing a single instance of a Docker container. It enforces a singleton pattern
 * for the container lifecycle by handling installation, startup, and shutdown processes.
 * Subclasses are required to implement certain methods to customize behavior specific
 * to the container being managed.
 *
 * This class extends {@code BaseImageInstaller} and implements {@code SingletonContainer}.
 * It provides core logic for actions such as container installation, starting, and stopping
 * while leaving container-specific details to the implementing subclass.
 *
 * @author  Cosmin Cara
 * @since   1.6
 */
public abstract class SingletonImageInstaller extends BaseImageInstaller implements SingletonContainer {

    @Override
    public String getContainerDescription() {
        return getDescription();
    }

    @Override
    public Container install() throws IOException {
        return installImage();
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
    protected List<String> startupArguments() {
        return List.of();
    }
}
