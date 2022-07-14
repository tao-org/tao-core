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

/**
 * Structure for any controller response
 * @param <T>   The type of the response data
 */
public class ServiceResponse<T> {
    private T data;
    private String message;
    private ResponseStatus status;

    /**
     * Constructs an empty response
     */
    public ServiceResponse() {

    }

    /**
     * Constructs a success response with the given data
     * @param data  The response data
     */
    public ServiceResponse(T data) {
        this.data = data;
        this.status = ResponseStatus.SUCCEEDED;
    }
    /**
     * Constructs a response with the given data and the given status
     * @param data      The response data
     * @param status    The response status
     */
    public ServiceResponse(T data, ResponseStatus status) {
        this(data, null, status);
    }
    /**
     * Constructs a response with the given text and status
     * @param message  The response message
     * @param status    The response status
     */
    public ServiceResponse(String message, ResponseStatus status) {
        this(null, message, status);
    }
    /**
     * Constructs a response with the given data, text and status
     * @param data      The response data
     * @param message   The response message
     * @param status    The response status
     */
    public ServiceResponse(T data, String message, ResponseStatus status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    /**
     * Returns the data (payload) of this response
     */
    public T getData() { return data; }
    /**
     * Returns the message of this response
     */
    public String getMessage() { return message; }
    /**
     * Returns the status of this response
     */
    public ResponseStatus getStatus() { return status; }
}
