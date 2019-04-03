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
package ro.cs.tao.datasource.remote.result;

import java.util.List;

/**
 * Generic interface to be implemented by parsers that handle the response of a remote data source.
 *
 * @param <T>   The type of a result record.
 *
 * @author Cosmin Cara
 */
public interface ResponseParser<T> {

    /**
     * Parses the response of a data source query and, if successful, returns a list of results.
     * @param content   The data source query response
     *
     * @throws ParseException   If the parser was unable to parse the content.
     */
    List<T> parse(String content) throws ParseException;

    T parseValue(String content) throws ParseException;

    default long parseCount(String content) throws ParseException { return -1; }

    /**
     * Returns a list of attribute names to be excluded from the parsing operation.
     * Implementors should overwrite this method.
     */
    default String[] getExcludedAttributes() { return null; }

}
