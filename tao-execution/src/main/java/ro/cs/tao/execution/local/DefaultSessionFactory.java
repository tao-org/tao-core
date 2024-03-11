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
package ro.cs.tao.execution.local;

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import ro.cs.tao.drmaa.Environment;

/**
 * Specialization of the DRMAA {@link SessionFactory} base class for local invocations.
 *
 * @author Cosmin Cara
 */
public class DefaultSessionFactory extends SessionFactory {
    private DefaultSession thisSession;

    public DefaultSessionFactory() { }

    @Override
    public Environment getEnvironment() {
        return Environment.DEFAULT;
    }

    @Override
    public Session getSession() {
        synchronized (this) {
            if (thisSession == null) {
                thisSession = new DefaultSession();
            }
        }

        return thisSession;
    }
}
