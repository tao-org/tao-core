/*
 * Copyright (C) 2017 CS ROMANIA
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
package ro.cs.tao.execution.local;

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

/**
 * @author Cosmin Cara
 */
public class DefaultSessionFactory extends SessionFactory {
    private DefaultSession thisSession;

    public DefaultSessionFactory() { }

    @Override
    public Session getSession() {
        synchronized (this) {
            if (thisSession == null) {
                thisSession = new DefaultSession();
                /*final List<NodeDescription> list = TopologyManager.getInstance().list();
                    if (list != null) {
                    int size = list.size();
                    NodeDescription[] nodes = new NodeDescription[size];
                    for (int i = 0; i < size; i++) {
                        this.nodes[i] = list.get(i);
                    }
                */
                //thisSession.setNodes(new NodeDescription[0]);
            }
        }

        return thisSession;
    }
}
