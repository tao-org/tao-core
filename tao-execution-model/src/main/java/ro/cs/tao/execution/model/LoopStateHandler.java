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

package ro.cs.tao.execution.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.serialization.SerializationException;

/**
 * Specialization of a state handler for loop states.
 *
 * @author Cosmin Cara
 */
public class LoopStateHandler implements InternalStateHandler<LoopState> {
    private LoopState loopState;

    public LoopStateHandler(LoopState loopState) {
        this.loopState = loopState;
    }

    @Override
    public void setCurrentState(String serializedState) throws SerializationException {
        try {
            if (serializedState != null) {
                this.loopState = new ObjectMapper().readValue(serializedState, LoopState.class);
            }
        } catch (Exception ex) {
            throw new SerializationException(ex);
        }
    }

    @Override
    public LoopState currentState() { return this.loopState; }

    @Override
    public LoopState nextState() {
        if (this.loopState != null && this.loopState.getCurrent() + 1 <= this.loopState.getLimit()) {
            this.loopState.setCurrent(this.loopState.getCurrent() + 1);
            return this.loopState;
        } else {
            return null;
        }
    }

    @Override
    public String serializeState() throws SerializationException {
        try {
            if (this.loopState != null) {
                return new ObjectMapper().writeValueAsString(this.loopState);
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new SerializationException(ex);
        }
    }
}
