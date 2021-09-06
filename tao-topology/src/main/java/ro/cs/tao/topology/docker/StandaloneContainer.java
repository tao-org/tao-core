package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;

import java.io.IOException;

/**
 * Marker interface for Docker plugins which are intended to start and keep alive a container.
 *
 * @author Cosmin Cara
 */
public interface StandaloneContainer {

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

    /**
     * Starts the Docker container
     */
    void start() throws IOException;

    /**
     * Shuts down the Docker container
     */
    void shutdown() throws IOException;
}
