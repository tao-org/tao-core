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
 * A simple implementation of the {@link Principal} interface, designating the "system" account (not related to any
 * TAO user).
 *
 * @author Cosmin Cara
 */
public final class SystemPrincipal implements Principal {

    private static final SystemPrincipal instance = new SystemPrincipal();

    public static Principal instance() { return instance; }

    private SystemPrincipal() { }

    @Override
    public String getName() {
        return "SystemAccount";
    }

    @Override
    public boolean implies(Subject subject) {
        return true;
    }
}
