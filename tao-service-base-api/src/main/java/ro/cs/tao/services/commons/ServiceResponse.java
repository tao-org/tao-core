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

package ro.cs.tao.services.commons;

public class ServiceResponse<T> {
    private T data;
    private String message;
    private ResponseStatus status;

    public ServiceResponse(T data) {
        this.data = data;
        this.status = ResponseStatus.SUCCEEDED;
    }

    public ServiceResponse(T data, ResponseStatus status) {
        this(data, null, status);
    }

    public ServiceResponse(String message, ResponseStatus status) {
        this(null, message, status);
    }

    public ServiceResponse(T data, String message, ResponseStatus status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    public T getData() { return data; }
    public String getMessage() { return message; }
    public ResponseStatus getStatus() { return status; }
}
