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

import ro.cs.tao.utils.StringUtilities;

import javax.security.auth.Subject;
import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

/**
 * Represents a user principal that implements the Principal and Serializable
 * interfaces. This class encapsulates information about a user's unique identifier
 * and associated roles.
 */
public class UserPrincipal implements Principal, Serializable {

    /** Generated serial ID. */
	private static final long serialVersionUID = -138887607776483992L;
	
	private final String id;
    private final Set<String> roles;

    public UserPrincipal(String id) {
        this(id, null);
    }

    public UserPrincipal(String id, Set<String> roles) {
        this.id = id;
        this.roles = roles;
    }

    /**
     * Checks if the current user has the specified role.
     *
     * The method performs the following checks in sequence:
     * 1. Verifies that the roles set of this user is not null.
     * 2. Checks that the provided role string is not null or empty.
     * 3. Determines if the role (converted to uppercase) is present in the roles set.
     *
     * @param role the role to check, provided as a non-null and non-empty string
     * @return true if the user's roles contain the specified role (case-insensitive), false otherwise
     */
    public boolean hasRole(String role) {
        return this.roles != null && !StringUtilities.isNullOrEmpty(role) && this.roles.contains(role.toUpperCase());
    }

    @Override
    public String getName() { return this.id; }

    @Override
    public boolean implies(Subject subject) { return true; }

    @Override
    public String toString() {
        return id;
    }
}
