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

package ro.cs.tao.component;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.security.SessionStore;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that wraps special values that may be used by all the components.
 *
 * @author Cosmin Cara
 */
public abstract class SystemVariable {
    /**
     * Variable for the current user workspace.
     */
    public static final SystemVariable USER_WORKSPACE = new UserWorkspace();
    /**
     * Variable for the shared (accessible by all users) workspace.
     */
    public static final SystemVariable SHARED_WORKSPACE = new SharedWorkspace();
    /**
     * Variable for the current user custom files location.
     */
    public static final SystemVariable USER_FILES = new UserFiles();
    /**
     * Variable for the shared (accessible by all users) files location.
     */
    public static final SystemVariable SHARED_FILES = new SharedFiles();
    /**
     * Variable for the web cache location.
     */
    public static final SystemVariable CACHE = new Cache();

    public static final SystemVariable SHARE = new Share();

    public static final SystemVariable ROOT = new WorkspaceRoot();

    private static final Set<SystemVariable> allVariables;

    static {
        allVariables = new HashSet<>();
        allVariables.add(ROOT);
        allVariables.add(USER_WORKSPACE);
        allVariables.add(SHARED_WORKSPACE);
        allVariables.add(USER_FILES);
        allVariables.add(SHARED_FILES);
        allVariables.add(CACHE);
        allVariables.add(SHARE);
    }

    /**
     * Returns all the TAO variables.
     */
    public static Set<SystemVariable> all() { return allVariables; }

    /**
     * Returns a TAO variable by its name.
     * @param key   The name of the variable
     */
    public static String get(String key) {
        SystemVariable var = allVariables.stream().filter(v -> v.key().equalsIgnoreCase(key)).findFirst().orElse(null);
        return var != null ? var.value() : null;
    }

    /**
     * The key of this variable.
     */
    public abstract String key();

    /**
     * The value of this variable.
     */
    public abstract String value();

    private static final class WorkspaceRoot extends SystemVariable {

        @Override
        public String key() { return "$ROOT"; }

        @Override
        public String value() {
            return Paths.get(ConfigurationManager.getInstance().getValue("workspace.location")).toString();
        }
    }

    private static final class UserWorkspace extends SystemVariable {

        @Override
        public String key() { return "$USER_WORKSPACE"; }

        @Override
        public String value() { return SessionStore.currentContext().getWorkspace().toString(); }
    }

    private static final class SharedWorkspace extends SystemVariable {

        @Override
        public String key() { return "$PUBLIC_WORKSPACE"; }

        @Override
        public String value() {
            return Paths.get(ConfigurationManager.getInstance().getValue("workspace.location"))
                        .resolve("public").toString();
        }
    }

    private static final class UserFiles extends SystemVariable {

        @Override
        public String key() { return "$USER_FILES"; }

        @Override
        public String value() {
            return Paths.get(USER_WORKSPACE.value()).resolve("files").toString();
        }
    }

    private static final class SharedFiles extends SystemVariable {

        @Override
        public String key() { return "$PUBLIC_FILES"; }

        @Override
        public String value() {
            return Paths.get(SHARED_WORKSPACE.value()).resolve("files").toString();
        }
    }

    private static final class Cache extends SystemVariable {

        @Override
        public String key() { return "$CACHE"; }

        @Override
        public String value() {
            return Paths.get(ConfigurationManager.getInstance().getValue("workspace.location"))
                    .resolve("cache").toString();
        }
    }

    private static final class Share extends SystemVariable {
        @Override
        public String key() { return "$SHARE"; }

        @Override
        public String value() {
            return Paths.get(ConfigurationManager.getInstance().getValue("node.mount.folder")).toString();
        }
    }
}
