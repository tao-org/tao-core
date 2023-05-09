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
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.datasource.DataSourceComponent;

import java.util.ArrayList;

/**
 * Specialization of an execution task for a data source operation.
 *
 * @author Oana H.
 */
public class DataSourceExecutionTask extends ExecutionTask {

    private DataSourceComponent component;

    public DataSourceExecutionTask() {
        super();
    }

    public DataSourceComponent getComponent() {
        return component;
    }

    public void setComponent(DataSourceComponent component) {
        this.component = component;
    }

    @Override
    public void setInputParameterValue(String parameterId, String value) {
        /*boolean descriptorExists = false;
        Collection<ParameterDescriptor> descriptors =
                DataSourceManager.getInstance().getSupportedParameters(component.getSensorName(),
                        component.getDataSourceName()).values();
        for (ro.cs.tao.datasource.param.ParameterDescriptor descriptor : descriptors) {
            if (descriptor.getName().equalsIgnoreCase(parameterId)) {
                descriptorExists = true;
                break;
            }
        }*/
        if (!DataSourceComponent.QUERY_PARAMETER.equals(parameterId) &&
            !DataSourceComponent.SYNC_PARAMETER.equals(parameterId)) {
            throw new ValidationException(String.format("The parameter ID [%s] does not exists in the component '%s'",
                    parameterId, component.getLabel()));
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        this.inputParameterValues.add(new Variable(parameterId, value));
    }

    @Override
    public void setOutputParameterValue(String parameterId, String value) {
        if (this.outputParameterValues == null) {
            this.outputParameterValues = new ArrayList<>();
        }
        Variable variable = this.outputParameterValues.stream().filter(v -> v.getKey().equals(parameterId)).findFirst().orElse(null);
        if (variable == null) {
            variable = new Variable(parameterId, value);
            this.outputParameterValues.add(variable);
        } else {
            variable.setValue(value);
        }
    }

    @Override
    public String buildExecutionCommand() {
        return null;
    }
}
