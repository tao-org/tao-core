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

import ro.cs.tao.serialization.SerializationException;

/**
 * Defines an object that "knows" how to deal with the internal state of an execution task,
 * and how to advance to the next internal state.
 * The internal state of a component can be anything, from a simple integer counter to more complex structures.
 *
 * @param <S>   The type of the internal state.
 *
 * @author Cosmin Cara
 */
public interface InternalStateHandler<S> {
    /**
     * Assigns a current state to a component.
     * @param serializedState   The serialized internal state representation
     */
    void setCurrentState(String serializedState) throws SerializationException;

    /**
     * Retrieves the current internal state of a component.
     */
    S currentState();

    /**
     * Retrieves the value for the next internal state.
     */
    S nextState();

    /**
     * Serializes the internal state.
     */
    String serializeState() throws SerializationException;
}
