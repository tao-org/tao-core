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
package ro.cs.tao.security;

import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.user.UserPreference;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

/**
 * Holder class for the session of a user.
 *
 * @author Cosmin Cara
 */
public abstract class SessionContext {
    protected final Principal principal;
    private final Path workspaceRoot;
    private final Path netSpaceRoot;
    private final List<UserPreference> preferences;

    protected SessionContext() {
        this.principal = setPrincipal(null);
        this.workspaceRoot = Paths.get(SystemVariable.ROOT.value());
        final String value = SystemVariable.SHARE.value();
        this.netSpaceRoot = value != null ? Paths.get(value) : null;
        this.preferences = setPreferences();
    }

    public abstract Principal setPrincipal(Principal principal);
    protected abstract List<UserPreference> setPreferences();

    /**
     * Returns the current principal
     */
    public Principal getPrincipal() { return principal; }

    /**
     * Returns the root path in the shared file system for the current principal
     */
    public Path getWorkspace() { return workspaceRoot.resolve(getPrincipal().getName()); }

    public Path getNetSpace() { return netSpaceRoot != null ? netSpaceRoot.resolve(getPrincipal().getName()) : null; }

    /**
     * Returns the location where the current principal can upload files
     */
    public Path getUploadPath() { return getWorkspace().resolve("files"); }

    /**
     * Returns the value of the requested preference key
     *
     * @param key   The preference key
     */
    public String getPreference(String key) {
        return (preferences != null && preferences.stream().anyMatch(p -> p.getKey().equals(key))) ?
                preferences.stream().filter(p -> p.getKey().equals(key)).findAny().get().getValue() : null;
    }
}
