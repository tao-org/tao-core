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
package ro.cs.tao.services.interfaces;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;

import java.nio.file.Path;
import java.util.List;

/**
 * Service interface for (Docker) container operations.
 *
 * @author Cosmin Cara
 */
public interface ContainerService extends CRUDService<Container, String> {

    /**
     * Returns a list of containers of the respective type
     * @param type  The type of containers
     */
    List<Container> listByType(ContainerType type);
    /**
     * Returns a list of containers of the respective type and visibility
     * @param type  The type of containers
     * @param visibility The visibility of containers
     */
    List<Container> listByTypeAndVisibility(ContainerType type, ContainerVisibility visibility);
    /**
     * Returns a list of containers visible for the given user.
     * These are containers either public or owned by the user.
     *
     * @param userId  The user identifier.
     */
    List<Container> listContainersVisibleToUser(String userId);
    /**
     * Returns a list of containers owned by the given user.
     *
     * @param userId  The user identifier.
     */
    List<Container> listUserContainers(String userId);
    /**
     * Updates database container information and its list of applications
     * @param id            The Docker container id (as returned by Docker)
     * @param name          The name of the container
     * @param path          The description of the container
     * @param applications  The list of applications exposed by this container
     */
    Container initializeContainer(String id, String name, String path, List<Application> applications);

    /**
     * Updates database container information
     * @param id            The Docker container id (as returned by Docker)
     * @param webContainer  The container to be updated (may come from a deserialization)
     */
    Container initializeContainer(String id, Container webContainer);

    /**
     * Registers a new container with Docker and populates database with its components (if provided)
     *
     * @param dockerFile    The Dockerfile path
     * @param descriptor    The container descriptor (entity)
     * @param components    The list of components contained in this container
     */
    String registerContainer(Path dockerFile, Container descriptor, ProcessingComponent[] components);

}
