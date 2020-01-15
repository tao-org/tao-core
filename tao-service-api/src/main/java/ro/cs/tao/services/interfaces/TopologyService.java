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

import ro.cs.tao.Tag;
import ro.cs.tao.docker.Container;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface TopologyService extends CRUDService<NodeDescription, String> {
    List<Container> getDockerImages();
    List<Tag> getNodeTags();
    List<NodeDescription> getNodes(boolean active);
    List<NodeDescription> getNodes(NodeFlavor nodeFlavor);
}
