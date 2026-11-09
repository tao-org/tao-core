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
    /**
     * Parses the provided JSON content and retrieves a list of values that match the specified filters.
     *
     * @param content The JSON content to be parsed.
     * @param filters Optional filters to apply when extracting values from the JSON content.
     *                These filters can dictate which attributes to include or exclude.
     * @return A list of parsed values of type T. Returns an empty list or null if no values match the criteria
     *         or an error occurs during parsing.
     * @throws IOException If an input or output exception occurs while parsing the content.
     */
    default List<T> readValues(String content, AttributeFilter...filters) throws IOException { return null; }

    /**
     * Counts the number of values present in the given JSON content.
     *
     * @param content The JSON content to analyze for counting values.
     * @return The count of values found in the JSON content. Returns -1 if an error occurs during processing.
     * @throws IOException If an input or output exception occurs during the operation.
     */
    default long countValues(String content) throws IOException { return -1; }

    /**
     * Parses the provided JSON content and retrieves a single value of type T, optionally applying the specified filters.
     *
     * @param content The JSON content to be parsed.
     * @param filters Optional filters to apply during parsing. These filters can determine specific attributes
     *                to include or exclude in the resulting value.
     * @return A parsed value of type T. Returns null if the content is invalid, no values match the criteria,
     *         or an error occurs during parsing.
     * @throws IOException If an input or output exception occurs while parsing the content.
     */
    default T readValue(String content, AttributeFilter...filters) throws IOException { return null; }
}
