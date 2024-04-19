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
import java.io.Serializable;
import java.security.Principal;

public class UserPrincipal implements Principal, Serializable {

    /** Generated serial ID. */
	private static final long serialVersionUID = -138887607776483992L;
	
	private final String id;

    public UserPrincipal(String id) {
        this.id = id;
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
