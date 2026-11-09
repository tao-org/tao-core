/*
 * Copyright (C) 2025 CS ROMANIA
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

package ro.cs.tao.execution.monitor;

import java.util.Map;

/**
 * Represents information about runtime system resources, including CPU usage,
 * memory usage, and disk usage aggregated for all cluster nodes and a map with each node.
 */
public class RuntimeInfoCluster {
    // aggregated info
    private RuntimeInfo clusterInfo;
    // node detailed info
    private Map<String, RuntimeInfo> nodesInfo;

    public RuntimeInfoCluster(){
    }

    public RuntimeInfo getClusterInfo() {
        return clusterInfo;
    }

    public void setClusterInfo(RuntimeInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public Map<String, RuntimeInfo> getNodesInfo() {
        return nodesInfo;
    }

    public void setNodesInfo(Map<String, RuntimeInfo> nodesInfo) {
        this.nodesInfo = nodesInfo;
    }
}
