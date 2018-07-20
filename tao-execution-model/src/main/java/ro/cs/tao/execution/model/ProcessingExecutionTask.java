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
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Oana H.
 */
public class ProcessingExecutionTask extends ExecutionTask {

    // format is: jobId-taskId-[internalState-]fileName
    private static final String nameTemplate = "%s-%s-%s%s";

    private ProcessingComponent component;

    private String instanceTargetOutput;

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
        Variable variable = this.inputParameterValues.stream()
                                                     .filter(v -> v.getKey().equals(parameterId))
                                                     .findFirst()
                                                     .orElse(null);
        SourceDescriptor sourceDescriptor = this.component.getSources().stream()
                                                                       .filter(s -> s.getName().equals(parameterId))
                                                                       .findFirst()
                                                                       .orElse(null);
        final int cardinality = sourceDescriptor != null ? sourceDescriptor.getCardinality() : 1;
        if (variable != null) {
            if (cardinality == 1) {
                variable.setValue(value);
            } else {
                variable.setValue(appendValueToList(variable.getValue(), value));
            }
        } else {
            Variable var;
            if (cardinality == 1) {
                var = new Variable(parameterId, value);
            } else {
                String newValue = appendValueToList(null, value);
                var = new Variable(parameterId, newValue);
            }
            this.inputParameterValues.add(var);
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
        Variable variable = this.outputParameterValues.stream()
                                                      .filter(v -> v.getKey().equals(parameterId))
                                                      .findFirst()
                                                      .orElse(null);
        TargetDescriptor targetDescriptor = this.component.getTargets().stream()
                                                                       .filter(t -> t.getName().equals(parameterId))
                                                                       .findFirst()
                                                                       .orElse(null);
        final int cardinality = targetDescriptor != null ? targetDescriptor.getCardinality() : 1;
        if (variable != null) {
            if (cardinality == 1) {
                variable.setValue(value);
            } else {
                variable.setValue(appendValueToList(variable.getValue(), value));
            }
        } else {
            Variable var;
            if (cardinality == 1) {
                var = new Variable(parameterId, value);
            } else {
                String newValue = appendValueToList(null, value);
                var = new Variable(parameterId, newValue);
            }
            this.outputParameterValues.add(var);
        }
    }

    @Override
    public String buildExecutionCommand() {
        if (component == null) {
            return null;
        }
        Map<String, String> inputParams = new HashMap<>();
        if (inputParameterValues != null) {
            for (Variable input : inputParameterValues) {
                String value = input.getValue();
                if (value.startsWith("[") && value.endsWith("]")) {
                    value = value.substring(1, value.length() - 1).replace(",", " ");
                }
                inputParams.put(input.getKey(), value);
            }
        }
        for (TargetDescriptor descriptor : this.component.getTargets()) {
            String location = descriptor.getDataDescriptor().getLocation();
            if (location != null) {
                inputParams.put(descriptor.getName(), getInstanceTargetOuptut(descriptor));
            }
        }
        Map<String, String> variables = new HashMap<>();
        variables.put(SystemVariable.USER_WORKSPACE.key(), getContext().getWorkspace().toString());
        variables.put(SystemVariable.SHARED_WORKSPACE.key(), SystemVariable.SHARED_WORKSPACE.value());
        variables.put(SystemVariable.USER_FILES.key(), getContext().getWorkspace().resolve("files").toString());
        variables.put(SystemVariable.SHARED_FILES.key(), SystemVariable.SHARED_FILES.value());
        return this.component.buildExecutionCommand(inputParams, variables);
    }

    public String getInstanceTargetOuptut(TargetDescriptor descriptor) {
        if (this.instanceTargetOutput == null) {
            this.instanceTargetOutput = descriptor.getDataDescriptor().getLocation();
            if (this.instanceTargetOutput != null) {
                Path path = Paths.get(this.instanceTargetOutput);
                SessionContext context = getContext();
                if (!path.isAbsolute()) {
                    path = context.getWorkspace().resolve(path);
                }
                String fileName = path.getFileName().toString();
                String folderName = String.format(nameTemplate,
                                                  this.getJob().getId(),
                                                  this.getId(),
                                                  this.internalState == null ? "" : this.internalState + "-",
                                                  FileUtils.getFilenameWithoutExtension(fileName));
                fileName = folderName + FileUtils.getExtension(fileName);
                try {
                    FileUtils.ensureExists(path.getParent().resolve(folderName));
                } catch (IOException e) {
                    Logger.getLogger(ProcessingExecutionTask.class.getName()).severe(e.getMessage());
                }
                this.instanceTargetOutput = path.getParent().resolve(folderName).resolve(fileName).toString();
            }
        }
        return this.instanceTargetOutput;
    }
}
