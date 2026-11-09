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
package ro.cs.tao.serialization;

/**
 * Factory class responsible for creating serializer instances for a given class
 * and media type (e.g., JSON, XML). The serializers handle the conversion
 * between objects and their string representation based on the specified media type.
 *
 * @author Cosmin Cara
 */
public class SerializerFactory {

    /**
     * Creates a new instance of a {@link BaseSerializer} for the specified class
     * and media type. The serializer will handle the conversion between objects
     * of type {@code T} and their string representation based on the provided
     * media type (e.g., JSON or XML).
     *
     * @param <T>       The type of the object that the serializer is designed for.
     * @param clazz     The {@link Class} of the object to be serialized/deserialized.
     * @param mediaType The {@link MediaType} (e.g., JSON or XML) that dictates the format
     *                  of the serialization/deserialization.
     * @return A {@link BaseSerializer} capable of handling objects of type {@code T}
     *         based on the specified media type.
     * @throws SerializationException If an error occurs during the creation of the serializer.
     */
    public static <T> BaseSerializer<T> create(Class<T> clazz, MediaType mediaType) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz);
            case XML:
            default:
                return new XmlSerializer<T>(clazz);
        }
    }

    /*public static <T> BaseSerializer<T> create(Class<T> clazz, MediaType mediaType, Class...dependencies) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz, dependencies);
            case XML:
            default:
                return new XmlSerializer<T>(clazz, dependencies);
        }
    }*/

}
