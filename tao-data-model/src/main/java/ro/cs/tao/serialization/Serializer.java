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

import java.util.List;

/**
 * Defines an interface for serializing and deserializing objects to and from various formats.
 * The interface provides methods for handling both singular objects and collections of objects,
 * with customizable options for output formatting and exception handling.
 *
 * @param <S> The type of objects to be deserialized.
 * @param <R> The type of serialized output.
 *
 * @author Cosmin Cara
 */
public interface Serializer<S, R> {

    /**
     * Configures whether the serialized output should be formatted for readability.
     *
     * @param value A boolean flag indicating whether to enable or disable formatted output.
     *              If {@code true}, the output will be formatted (e.g., indented for readability).
     *              If {@code false}, the output will be compact without formatting.
     */
    void setFormatOutput(boolean value);
    /**
     * Deserializes the provided string input into an object of type {@code S}.
     *
     * @param source The string representation of the serialized object.
     *               It should be in the appropriate format expected by the deserialization process.
     * @return The deserialized object of type {@code S}.
     * @throws SerializationException If the deserialization process fails due to an invalid format,
     *                                 missing data, or other related errors.
     */
    S deserialize(String source) throws SerializationException;
    /**
     * Deserializes the provided string input into a list of objects of type {@code S}.
     *
     * @param sClass The {@code Class} type of the objects to be deserialized from the input.
     *               This parameter is used to ensure type safety during the deserialization process.
     * @param source The string representation of the serialized objects.
     *               It should be in the appropriate format required for deserialization.
     * @return A list of deserialized objects of type {@code S}.
     * @throws SerializationException If the deserialization process fails due to an invalid format,
     *                                 unsupported type, missing data, or other related issues.
     */
    List<S> deserialize(Class<S> sClass, String source) throws SerializationException;
    /**
     * Serializes the given object into a specified format.
     *
     * @param object The object of type {@code S} to be serialized.
     *               It should conform to the expected structure required by the serializer.
     * @return The serialized representation of the object as type {@code R}.
     * @throws SerializationException If the serialization process fails due to unsupported object types,
     *                                 invalid object structure, or other related errors.
     */
    R serialize(S object) throws SerializationException;
    /**
     * Serializes a list of objects into a specified format and associates it with a given name.
     *
     * @param objects The list of objects of type {@code S} to be serialized.
     *                Each object should conform to the expected structure required by the serializer.
     * @param name    A {@code String} representing the name to associate with the serialized output.
     *                This parameter may be used for grouping, identification, or labeling purposes.
     * @return The serialized representation of the list as type {@code R}.
     * @throws SerializationException If the serialization process fails due to unsupported object types,
     *                                 invalid object structures, or other related errors.
     */
    R serialize(List<S> objects, String name) throws SerializationException;

}
