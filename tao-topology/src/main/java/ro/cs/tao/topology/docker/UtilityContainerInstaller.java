package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;

import java.io.IOException;

/**
 * Defines the contract for a utility container installer. Implementations
 * of this interface are responsible for handling the lifecycle operations
 * associated with a container, such as retrieving its metadata and managing
 * its installation process.
 */
public interface UtilityContainerInstaller {
    /**
     * Returns the name of the container
     */
    String getContainerName();

    /**
     * Returns the description of the container
     */
    String getContainerDescription();

    /**
     * Handles the container image installation
     */
    Container install() throws IOException;
}
