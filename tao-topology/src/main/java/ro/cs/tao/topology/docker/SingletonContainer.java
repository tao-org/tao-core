package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;

import java.io.IOException;

/**
 * Marker interface for Docker plugins which are intended to start and keep alive a container.
 *
 * @author Cosmin Cara
 */
public interface SingletonContainer {

    default boolean isPerUser() { return false; }

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
    default String start() throws IOException {
        return null;
    }

    default String start(String user, String token) throws IOException {
        return null;
    }

    /**
     * Shuts down the Docker container
     */
    void shutdown() throws IOException;
}
