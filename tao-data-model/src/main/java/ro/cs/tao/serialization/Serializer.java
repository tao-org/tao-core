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

import javax.xml.transform.stream.StreamSource;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface Serializer<S, R> {

    void setFormatOutput(boolean value);
    S deserialize(StreamSource source) throws SerializationException;
    List<S> deserializeList(Class<S> sClass, StreamSource source) throws SerializationException;
    R serialize(S object) throws SerializationException;
    R serialize(List<S> objects, String name) throws SerializationException;

}
