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

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * Represents a group-based principal in an authentication context.
 *
 * A GroupPrincipal is typically used to signify membership in a group
 * or a role within a specific security domain. It implements the
 * {@link Principal} interface, allowing it to be used in security-related
 * APIs that rely on Java's security framework.
 *
 * This class is immutable and ensures thread safety by making its
 * internal state final.
 */
public class GroupPrincipal implements Principal {

    private final String groupName;

    public GroupPrincipal(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String getName() { return this.groupName; }

    @Override
    public boolean implies(Subject subject) { return true; }
}
