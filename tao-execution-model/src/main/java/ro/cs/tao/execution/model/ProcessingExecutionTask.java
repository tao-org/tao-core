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

import ro.cs.tao.component.*;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.security.SessionContext;
import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Specialization of an execution task that holds a processing component.
 *
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
    public void setInputParameterValue(String parameterName, String value) {
        boolean descriptorExists = false;
        List<ParameterDescriptor> descriptorList = this.component.getParameterDescriptors();
        for (ParameterDescriptor descriptor : descriptorList) {
            if (descriptor.getName().equals(parameterName)) {
                descriptorExists = true;
                break;
            }
        }
        List<SourceDescriptor> sources = this.component.getSources();
        for (SourceDescriptor source : sources) {
            if (source.getName().equals(parameterName)) {
                descriptorExists = true;
                break;
            }
        }
        List<TargetDescriptor> targets = this.component.getTargets();
        for (TargetDescriptor target : targets) {
            if (target.getName().equals(parameterName)) {
                descriptorExists = true;
                break;
            }
        }
        if (!descriptorExists) {
            throw new ValidationException(String.format("The parameter ID [%s] does not exists in the component '%s'",
                    parameterName, component.getLabel()));
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        Variable variable = this.inputParameterValues.stream()
                                                     .filter(v -> v.getKey().equals(parameterName))
                                                     .findFirst()
                                                     .orElse(null);
        SourceDescriptor sourceDescriptor = this.component.getSources().stream()
                                                                       .filter(s -> s.getName().equals(parameterName))
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
                var = new Variable(parameterName, value);
            } else {
                String newValue = appendValueToList(null, value);
                var = new Variable(parameterName, newValue);
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
        Map<String, Object> inputParams = new HashMap<>();
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
            Variable variable = outputParameterValues.stream()
                    .filter(v -> descriptor.getName().equals(v.getKey())).findFirst().orElse(null);
            if (variable != null) {
                String location = variable.getValue() != null ? variable.getValue() : descriptor.getDataDescriptor().getLocation();
                if (location != null) {
                    this.instanceTargetOutput = getInstanceTargetOuptut(location);
                    inputParams.put(descriptor.getName(), this.instanceTargetOutput);
                    setInputParameterValue(descriptor.getName(), this.instanceTargetOutput);
                }
            }
        }
        Map<String, String> variables = new HashMap<>();
        variables.put(SystemVariable.USER_WORKSPACE.key(), getContext().getWorkspace().toString());
        variables.put(SystemVariable.SHARED_WORKSPACE.key(), SystemVariable.SHARED_WORKSPACE.value());
        variables.put(SystemVariable.USER_FILES.key(), getContext().getWorkspace().resolve("files").toString());
        variables.put(SystemVariable.SHARED_FILES.key(), SystemVariable.SHARED_FILES.value());
        String[] outPath = buildOutputPath();
        variables.put("$TEMPLATE_REAL_PATH", outPath != null ? outPath[0] : null);
        variables.put("$TEMPLATE_CMD_PATH", outPath != null ? outPath[1] : null);
        return this.component.buildExecutionCommand(inputParams, variables);
    }

    /**
     * Computes the actual path of the output of this task.
     * The "virtual" path (what is reported to the next task) is build considering the common share,
     * while the actual path is build considering the master workspace path.
     * If the value of the parameter is different from the default one, then it is left "as-is", because
     * it may represent a name expression that will be externally parsed.
     * @param location  The template value of the target of this task.
     */
    public String getInstanceTargetOuptut(String location) {
        if (this.instanceTargetOutput == null) {
            this.instanceTargetOutput = location;
            if (this.instanceTargetOutput != null) {
                Path path = Paths.get(this.instanceTargetOutput);
                SessionContext context = getContext();
                if (!path.isAbsolute()) {
                    path = context.getNetSpace().resolve(path);
                }
                final String fileName;
                if (this.component.getTargets().stream().anyMatch(t -> t.getDataDescriptor().getLocation().equals(location))) {
                    fileName = String.format(nameTemplate,
                                             this.getJob().getId(),
                                             this.getId(),
                                             this.internalState == null ? "" : this.internalState + "-",
                                             path.getFileName().toString());
//                    folderName = FileUtilities.getFilenameWithoutExtension(fileName);
                } else {
                    fileName = path.getFileName().toString();
//                    folderName = String.format(nameTemplate,
//                                               this.getJob().getId(),
//                                               this.getId(),
//                                               this.internalState == null ? "" : this.internalState + "-",
//                                               FileUtilities.getFilenameWithoutExtension(fileName));
                }
                final String folderName = String.format(nameTemplate,
                                                        this.getJob().getId(),
                                                        this.getId(),
                                                        this.internalState == null ? "" : this.internalState + "-",
                                                        this.component.getId());
                try {
                    FileUtilities.ensureExists(context.getWorkspace().resolve(folderName));
                } catch (IOException e) {
                    Logger.getLogger(ProcessingExecutionTask.class.getName()).severe(e.getMessage());
                }
                this.instanceTargetOutput = path.getParent().resolve(folderName).resolve(fileName).toString().replace('\\', '/');
            }
        }
        return this.instanceTargetOutput;
    }

    public String[] buildOutputPath() {
        String[] outPath = null;
        String location = null;
        for (TargetDescriptor descriptor : this.component.getTargets()) {
            Variable variable = outputParameterValues.stream()
                    .filter(v -> descriptor.getName().equals(v.getKey())).findFirst().orElse(null);
            if (variable != null) {
                location = variable.getValue() != null ? variable.getValue() : descriptor.getDataDescriptor().getLocation();
                if (location != null) {
                    break;
                }
            }
        }
        if (location != null) {
            Path path = Paths.get(location);
            SessionContext context = getContext();
            if (!path.isAbsolute()) {
                path = context.getNetSpace().resolve(path);
            }
            String fileName = path.getFileName().toString();
            String folderName = String.format(nameTemplate,
                                              this.getJob().getId(),
                                              this.getId(),
                                              this.internalState == null ? "" : this.internalState + "-",
                                              FileUtilities.getFilenameWithoutExtension(fileName));
            outPath = new String[] { context.getWorkspace().resolve(folderName).toString().replace('\\', '/'),
                                     path.getParent().resolve(folderName).toString().replace('\\', '/') };
        }
        return outPath;
    }
}
