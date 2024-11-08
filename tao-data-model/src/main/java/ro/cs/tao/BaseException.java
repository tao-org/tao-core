/*
 *
 *  * Copyright (C) 2018 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */
package ro.cs.tao;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for TAO exceptions.
 * It adds additional information to a "standard" exception in the form of key-value pairs.
 *
 * @author Cosmin Cara
 */
public class BaseException extends RuntimeException {
    private Map<String, Object> additionalInfo;

    public BaseException() {
        super();
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        addAdditionalInfo(cause.getClass().getSimpleName(), cause.getMessage());
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Object... arguments) {
        super(String.format(message, arguments));
    }

    /**
     * Returns any additional information this exception may hold.
     */
    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Adds an item of additional information.
     *
     * @param key   The key (name)
     * @param info  The information
     */
    public void addAdditionalInfo(String key, Object info) {
        if (this.additionalInfo == null) {
            this.additionalInfo = new HashMap<>(2);
        }
        this.additionalInfo.put(key, info);
    }
}
