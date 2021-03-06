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
package ro.cs.tao.datasource.remote.result.json;

import ro.cs.tao.datasource.remote.result.filters.AttributeFilter;

import java.io.IOException;
import java.util.List;

/**
 * Interface to be implemented by handlers that deal with JSON content.
 *
 * @author Cosmin Cara
 */
public interface JSonResponseHandler<T> {
    default List<T> readValues(String content, AttributeFilter...filters) throws IOException { return null; }
    default long countValues(String content) throws IOException { return -1; }
    default T readValue(String content, AttributeFilter...filters) throws IOException { return null; }
}
