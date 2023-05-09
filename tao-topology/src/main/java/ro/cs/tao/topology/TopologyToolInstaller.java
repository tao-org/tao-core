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

import ro.cs.tao.component.StringIdentifiable;

/**
 * @author Cosmin Udroiu
 */
public abstract class TopologyToolInstaller extends StringIdentifiable {
    @Override
    public String defaultId() { return "NewToolInstaller"; }

    public abstract void setMasterNodeDescription(NodeDescription masterNodeInfo);
    public abstract ServiceInstallStatus installNewNode(NodeDescription info) throws TopologyException;
    public abstract ServiceInstallStatus uninstallNode(NodeDescription info)throws TopologyException;
    public abstract void editNode(NodeDescription nodeInfo)throws TopologyException;
}
