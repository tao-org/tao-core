/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;

import java.io.IOException;

/**
 * Interface representing a service for installing and managing Docker images.
 * This interface defines the core operations required to retrieve the name of
 * a container and install a Docker image as a container.
 */
public interface DockerImageInstaller {
    /**
     * Retrieves the name of the Docker container associated with the service.
     *
     * @return the name of the container as a String
     */
    String getContainerName();

    /**
     * Installs a Docker image and creates a corresponding container representation.
     * The implementation of this method is responsible for performing the
     * necessary operations to fetch, initialize, and set up the image as a
     * Docker container.
     *
     * @return a Container object representing the installed Docker image
     * @throws IOException if an error occurs during the installation process
     */
    Container installImage() throws IOException;
}
