/*
 * Copyright (C) 2017 CS ROMANIA
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
package ro.cs.tao.serialization;

/**
 * @author Cosmin Cara
 */
public class SerializerFactory {

    public static <T> BaseSerializer<T> create(Class<T> clazz, MediaType mediaType) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz);
            case XML:
            default:
                return new XmlSerializer<T>(clazz);
        }
    }

    public static <T> BaseSerializer<T> create(Class<T> clazz, MediaType mediaType, Class...dependencies) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz, dependencies);
            case XML:
            default:
                return new XmlSerializer<T>(clazz, dependencies);
        }
    }

}
