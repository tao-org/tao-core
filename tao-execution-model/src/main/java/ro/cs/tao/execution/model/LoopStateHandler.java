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

package ro.cs.tao.execution.model;

import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.SerializerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

public class LoopStateHandler implements InternalStateHandler<LoopState> {
    private LoopState loopState;

    public LoopStateHandler(LoopState loopState) {
        this.loopState = loopState;
    }

    @Override
    public void setCurrentState(String serializedState) throws SerializationException {
        if (serializedState != null) {
            BaseSerializer<LoopState> serializer = SerializerFactory.create(LoopState.class, MediaType.JSON);
            this.loopState = serializer.deserialize(new StreamSource(new StringReader(serializedState)));
        }
    }

    @Override
    public LoopState currentState() { return this.loopState; }

    @Override
    public LoopState nextState() {
        if (this.loopState != null && this.loopState.getCurrent() <= this.loopState.getLimit()) {
            this.loopState.setCurrent(this.loopState.getCurrent() + 1);
            return this.loopState;
        } else {
            return null;
        }
    }

    @Override
    public String serializeState() throws SerializationException {
        if (this.loopState != null) {
            BaseSerializer<LoopState> serializer = SerializerFactory.create(LoopState.class, MediaType.JSON);
            return serializer.serialize(this.loopState);
        } else {
            return null;
        }
    }
}
