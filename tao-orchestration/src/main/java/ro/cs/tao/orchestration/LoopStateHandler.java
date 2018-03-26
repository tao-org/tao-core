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

package ro.cs.tao.orchestration;

import ro.cs.tao.execution.model.LoopState;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.Serializer;
import ro.cs.tao.serialization.SerializerFactory;

import javax.xml.transform.stream.StreamSource;
import java.util.function.Function;

public class LoopStateHandler implements Function<String, Integer> {
    @Override
    public Integer apply(String s) {
        Integer nextState = null;
        try {
            Serializer<LoopState, String> serializer = SerializerFactory.create(LoopState.class, MediaType.JSON);
            LoopState state = serializer.deserialize(new StreamSource(s));
            if (state != null && state.getCurrent() <= state.getLimit()) {
                nextState = state.getCurrent() + 1;
            }
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        return nextState;
    }
}
