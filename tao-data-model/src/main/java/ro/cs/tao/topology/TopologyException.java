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
package ro.cs.tao.topology;

import ro.cs.tao.BaseException;

/**
 * Represents an exception that occurs in the context of handling topologies.
 *
 * This class provides constructors to create exceptions with a message,
 * a cause, or both. It is primarily used to handle errors specific to
 * topology management operations and extends the functionality of
 * {@code BaseException} by inheriting its capability to hold
 * additional key-value information about an exception.
 *
 * Use this exception to indicate a problem or failure in topology-related processes.
 *
 * @author Cosmin Cara
 */
public class TopologyException extends BaseException {
    public TopologyException() {
    }

    public TopologyException(String message) {
        super(message);
    }

    public TopologyException(String message, Throwable cause) {
        super(message, cause);
    }

    public TopologyException(Throwable cause) {
        super(cause);
    }

    public TopologyException(String message, Object... arguments) {
        super(message, arguments);
    }
}
