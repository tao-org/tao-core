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
package ro.cs.tao.security;

import java.security.Principal;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
class SystemSessionContext extends SessionContext {
    private static final SessionContext instance = new SystemSessionContext();

    public static SessionContext instance() { return instance; }

    private SystemSessionContext() { super(); }

    @Override
    protected Principal setPrincipal() { return SystemPrincipal.instance(); }

    @Override
    protected Map<String, String> setPreferences() { return null; }
}
