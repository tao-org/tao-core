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

import ro.cs.tao.component.Variable;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.serialization.SerializationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Specialization of a state handler for loop states.
 *
 * @author Cosmin Cara
 */
public class LoopStateHandler implements InternalStateHandler<LoopState> {
    private LoopState loopState;
    private ExecutionGroup handledTask;

    public LoopStateHandler(LoopState loopState) {
        this.loopState = loopState;
    }

    @Override
    public void assignTask(ExecutionTask task) {
        if (ExecutionGroup.class.isAssignableFrom(task.getClass())) {
            this.handledTask = (ExecutionGroup) task;
        } else {
            throw new ClassCastException(String.format("Expected: %s. Received: %s",
                                                       ExecutionGroup.class.getSimpleName(),
                                                       task.getClass().getSimpleName()));
        }
    }

    @Override
    public void setCurrentState(String serializedState) throws SerializationException {
        try {
            if (serializedState != null) {
                this.loopState = JsonMapper.instance().readValue(serializedState, LoopState.class);
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
                return JsonMapper.instance().writeValueAsString(this.loopState);
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw new SerializationException(ex);
        }
    }

    @Override
    public List<Variable> advanceToNextState(List<Variable> inputs) {
        if (handledTask == null || inputs == null) {
            return null;
        }
        handledTask.setInputParameterValues(inputs);
        if (handledTask.inputParameterValues == null) {
            handledTask.inputParameterValues = new ArrayList<>();
        }
        List<Variable> mappedInput = new ArrayList<>();
        int index = currentState().getCurrent();
        for (Variable inputParameterValue : handledTask.inputParameterValues) {
            Variable newVar = new Variable();
            newVar.setKey(inputParameterValue.getKey());
            newVar.setValue(handledTask.getListValue(inputParameterValue.getValue(), index));
            // Advance to the next internal state, but without persisting it
            nextState();
            mappedInput.add(newVar);
        }
        return mappedInput;
    }
}
