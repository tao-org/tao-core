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

package ro.cs.tao.eodata;

import ro.cs.tao.BaseException;

/**
 * Represents an exception that is thrown when an issue occurs while handling data operations.
 *
 * <p>This class extends {@link BaseException} to provide additional context when data-related
 * errors occur, allowing messages and causes to be specified for more detailed error reporting.</p>
 *
 * <ul>
 * Key Feaures:
 * - Can initialize with a custom message using constructors.
 * - Can wrap and propagate underlying causes of errors, such as nested exceptions.
 */
public class DataHandlingException extends BaseException {
    public DataHandlingException() {
    }

    public DataHandlingException(String message) {
        super(message);
    }

    public DataHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataHandlingException(Throwable cause) {
        super(cause);
    }
}
