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

package ro.cs.tao.component;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.security.SessionStore;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public abstract class SystemVariable {
    public static final SystemVariable USER_WORKSPACE = new UserWorkspace();
    public static final SystemVariable SHARED_WORKSPACE = new SharedWorkspace();
    private static final Set<SystemVariable> allVariables;

    static {
        allVariables = new HashSet<>();
        allVariables.add(USER_WORKSPACE);
        allVariables.add(SHARED_WORKSPACE);
    }

    public static Set<SystemVariable> all() { return allVariables; }

    public static String get(String key) {
        SystemVariable var = allVariables.stream().filter(v -> v.key().equalsIgnoreCase(key)).findFirst().orElse(null);
        return var != null ? var.value() : null;
    }

    public abstract String key();
    public abstract String value();

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
            return Paths.get(ConfigurationManager.getInstance().getValue("product.location"))
                        .resolve("public").toString();
        }
    }
}
