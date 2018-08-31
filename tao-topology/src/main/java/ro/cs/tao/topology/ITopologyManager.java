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
package ro.cs.tao.topology;

import ro.cs.tao.docker.Container;
import ro.cs.tao.topology.docker.DockerImageInstaller;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Cosmin Udroiu
 */
public interface ITopologyManager {
    NodeDescription get(String hostName) throws TopologyException;
    List<NodeDescription> list() throws TopologyException;
    void add(NodeDescription info) throws TopologyException;
    void update(NodeDescription nodeInfo) throws TopologyException;
    void remove(String hostName) throws TopologyException;
    List<Container> getAvailableDockerImages();
    void registerImage(Path imagePath, String shortName, String description) throws TopologyException;
    List<DockerImageInstaller> getInstallers();
}
