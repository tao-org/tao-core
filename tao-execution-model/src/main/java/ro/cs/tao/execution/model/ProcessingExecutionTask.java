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

import ro.cs.tao.component.*;
import ro.cs.tao.component.validation.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Oana H.
 */
public class ProcessingExecutionTask extends ExecutionTask {

    private ProcessingComponent component;

    public ProcessingExecutionTask() {
        super();
    }

    public ProcessingComponent getComponent() {
        return component;
    }

    public void setComponent(ProcessingComponent component) {
        this.component = component;
    }

    @Override
    public void setInputParameterValue(String parameterId, String value) {
        boolean descriptorExists = false;
        List<ParameterDescriptor> descriptorList = this.component.getParameterDescriptors();
        for (ParameterDescriptor descriptor : descriptorList) {
            if (descriptor.getId().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        List<SourceDescriptor> sources = this.component.getSources();
        for (SourceDescriptor source : sources) {
            if (source.getName().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        if (!descriptorExists) {
            throw new ValidationException(String.format("The parameter ID [%s] does not exists in the component '%s'",
                    parameterId, component.getLabel()));
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        Variable variable = this.inputParameterValues.stream().filter(v -> v.getKey().equals(parameterId)).findFirst().orElse(null);
        if (variable != null) {
            variable.setValue(value);
        } else {
            this.inputParameterValues.add(new Variable(parameterId, value));
        }
    }

    @Override
    public void setOutputParameterValue(String parameterId, String value) {
        boolean descriptorExists = false;
        List<TargetDescriptor> targets = this.component.getTargets();
        for (TargetDescriptor target : targets) {
            if (target.getName().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        if (!descriptorExists) {
            throw new ValidationException(String.format("The output parameter ID [%s] does not exists in the component '%s'",
                    parameterId, component.getLabel()));
        }
        if (this.outputParameterValues == null) {
            this.outputParameterValues = new ArrayList<>();
        }
        Variable variable = this.outputParameterValues.stream().filter(v -> v.getKey().equals(parameterId)).findFirst().orElse(null);
        if (variable != null) {
            variable.setValue(value);
        } else {
            this.outputParameterValues.add(new Variable(parameterId, value));
        }
    }

    @Override
    public String buildExecutionCommand() {
        if (component == null) {
            return null;
        }
        Map<String, String> inputParams = new HashMap<>();
        if (inputParameterValues != null) {
            inputParams.putAll(inputParameterValues.stream()
                    .collect(Collectors.toMap(Variable::getKey, Variable::getValue)));
        }
        return this.component.buildExecutionCommand(inputParams);
    }
}
