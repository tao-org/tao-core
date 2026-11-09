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
package ro.cs.tao.datasource;

import ro.cs.tao.BaseException;

/**
 * Typed query exception.
 *
 * @author Cosmin Cara
 */
public class QueryException extends BaseException {
    private final boolean retryLater;
    private final boolean continueWithNext;

    public QueryException() {
        this.retryLater = false;
        this.continueWithNext = false;
    }

    public QueryException(String message) {
        this(message, false);
    }

    public QueryException(String message, boolean retryLater) {
        super(message);
        this.retryLater = retryLater;
        this.continueWithNext = false;
    }

    public QueryException(String message, Throwable cause) {
        this(message, cause, false);
    }

    public QueryException(String message, Throwable cause, boolean retryLater) {
        this(message, cause, retryLater, false);
    }

    public QueryException(String message, Throwable cause, boolean retryLater, boolean continueWithNext) {
        super(message, cause);
        this.retryLater = retryLater;
        this.continueWithNext = continueWithNext;
    }

    public QueryException(Throwable cause) {
        this(cause, false);
    }

    public QueryException(Throwable cause, boolean retryLater) {
        this(cause, retryLater, false);
    }

    public QueryException(Throwable cause, boolean retryLater, boolean continueWithNext) {
        super(cause);
        this.retryLater = retryLater;
        this.continueWithNext = continueWithNext;
    }

    public boolean shouldRetry() {
        return retryLater;
    }

    public boolean continueWithNext() {
        return continueWithNext;
    }
}
