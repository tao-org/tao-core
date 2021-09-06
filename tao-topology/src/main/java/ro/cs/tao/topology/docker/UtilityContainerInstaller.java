package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;

import java.io.IOException;

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
