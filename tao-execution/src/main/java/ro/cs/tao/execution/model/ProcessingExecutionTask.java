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
import ro.cs.tao.component.converters.ConverterFactory;
import ro.cs.tao.component.converters.ParameterConverter;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.utils.FileUtilities;

import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Specialization of an execution task that holds a processing component.
 *
 * @author Oana H.
 */
public class ProcessingExecutionTask extends ExecutionTask {

    private static final Set<String> arrayChars = new HashSet<>() {{
        add("["); add("{"); add("]"); add("}");
    }};

    private ProcessingComponent component;

    @XmlTransient
    protected Set<String> externalCommonParameters;

    public ProcessingExecutionTask() {
        super();
    }

    public ProcessingComponent getComponent() {
        return component;
    }

    public void setComponent(ProcessingComponent component) {
        this.component = component;
    }

    public void setExternalCommonParameters(Set<String> parameterNames) {
        this.externalCommonParameters = parameterNames;
    }

    @Override
    public void setInputParameterValue(String parameterName, String value) {
        final boolean descriptorExists =
                this.component.getParameterDescriptors().stream().anyMatch(d -> d.getName().equals(parameterName)) ||
                this.component.getSources().stream().anyMatch(s -> s.getName().equals(parameterName)) ||
                this.component.getTargets().stream().anyMatch(t -> t.getName().equals(parameterName));
        if (!descriptorExists) {
            if (this.externalCommonParameters == null || !this.externalCommonParameters.contains(parameterName)) {
                throw new ValidationException(String.format("The parameter ID [%s] does not exists in the component '%s'",
                                                            parameterName, component.getLabel()));
            }
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
                variable.setValue(value != null ? appendValueToList(variable.getValue(), value) : value);
            }
        } else {
            Variable var;
            if (cardinality == 1) {
                var = new Variable(parameterName, value);
            } else {
                var = new Variable(parameterName, value != null ? appendValueToList(null, value) : value);
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
        final List<ParameterDescriptor> parameterDescriptors = this.component.getParameterDescriptors();
        if (inputParameterValues != null) {
            for (Variable input : inputParameterValues) {
                String strValue = input.getValue();
                final String key = input.getKey();
                Object value;
                ParameterDescriptor descriptor = parameterDescriptors.stream()
                                                                     .filter(d -> d.getName().equals(key))
                                                                     .findFirst().orElse(null);
                if (descriptor == null) {
                    // maybe it is a sub-parameter of a template parameter
                    TemplateParameterDescriptor templateDescriptor = (TemplateParameterDescriptor) parameterDescriptors.stream()
                                                     .filter(d -> d.getType() == ParameterType.TEMPLATE &&
                                                             ((TemplateParameterDescriptor) d).getParameters().stream().anyMatch(p -> p.getName().equals(key)))
                                                     .findFirst().orElse(null);
                    if (templateDescriptor != null) {
                        descriptor = templateDescriptor.getParameters().stream()
                                                       .filter(d -> templateDescriptor.getName().equals(key)).findFirst().get();
                        value = getParameterTypedValue(descriptor, strValue);
                    } else {
                        // If it's not a parameter or a sub-parameter, it must be a source
                        value = getParameterTypedValue(descriptor, strValue);
                    }
                } else {
                    value = getParameterTypedValue(descriptor, strValue);
                }
                inputParams.put(key, value);
            }
        }
        for (TargetDescriptor descriptor : this.component.getTargets()) {
            Variable variable = outputParameterValues.stream()
                    .filter(v -> descriptor.getName().equals(v.getKey())).findFirst().orElse(null);
            if (variable != null) {
                String location = variable.getValue() != null ? variable.getValue() : descriptor.getDataDescriptor().getLocation();
                if (location != null && !this.getJob().isExternal()) {
                    this.instanceTargetOutput = computeTargetOutput(location);
                    final String output = getInstanceTemporaryOutput() != null ? getInstanceTemporaryOutput() : this.instanceTargetOutput;
                    inputParams.put(descriptor.getName(), output);
                    setInputParameterValue(descriptor.getName(), output);
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

    private Object getParameterTypedValue(ParameterDescriptor descriptor, String strValue) {
        Object value = strValue;
        if (descriptor != null && (descriptor.getDataType().isArray())) {
            if (arrayChars.contains(strValue.substring(0, 1)) &&
                    arrayChars.contains(strValue.substring(strValue.length() - 1))) {
                strValue = strValue.substring(1, strValue.length() - 1);
            }
            final String[] values = strValue.split("[,;]");
            value = JavaType.createArray(JavaType.fromClass(descriptor.getDataType()), values.length);
            final Class<?> type = descriptor.getDataType().getComponentType();
            final ParameterConverter<?> converter = ConverterFactory.getInstance().create(type);
            for (int i = 0; i < values.length; i++) {
                try {
                    Array.set(value, i, converter.fromString(values[i]));
                } catch (ConversionException e) {
                    throw new RuntimeException(e);
                }
            }
            // it may be a source descriptor value, which is not marked as array
        } else if (arrayChars.contains(strValue.substring(0, 1)) &&
                arrayChars.contains(strValue.substring(strValue.length() - 1))) {
            strValue = strValue.substring(1, strValue.length() - 1);
            value = strValue.replaceAll("[,;]", " ");
        } else if (strValue.contains(" ") && strValue.charAt(0) != '\'' &&  strValue.charAt(0) != '\"') {
            // Parameter values containing spaces will break the command line invocation, hence
            // the value needs to be enclosed in quotes
            value = "'" + strValue + "'";
        }
        return value;
    }

    /**
     * Computes the actual path of the output of this task.
     * The "virtual" path (what is reported to the next task) is build considering the common share,
     * while the actual path is build considering the master workspace path.
     * If the value of the parameter is different from the default one, then it is left "as-is", because
     * it may represent a name expression that will be externally parsed.
     * @param location  The template value of the target of this task.
     */
    public String computeTargetOutput(String location) {
        if (this.instanceTargetOutput == null) {
            if (location != null) {
                this.instanceTargetOutput = location;
            }
            if (this.instanceTargetOutput != null) {
                Path path = Paths.get(this.instanceTargetOutput);
                //SessionContext context = getContext();
                if (!path.isAbsolute()) {
                    //path = context.getNetSpace().resolve(path);
                    /*path = Paths.get(ExecutionConfiguration.getWorkerContainerVolumeMap().getContainerWorkspaceFolder())
                                .resolve(context.getPrincipal().getName()).resolve(path);*/
                    path = Paths.get(ExecutionConfiguration.getWorkerContainerVolumeMap().getContainerWorkspaceFolder())
                                .resolve(getJob().getUserId()).resolve(path);
                }
                final String fileName;
                if (this.component.getTargets().stream().anyMatch(t -> t.getDataDescriptor().getLocation() != null &&
                                                                       t.getDataDescriptor().getLocation().equals(location))) {
                    fileName = String.format(nameTemplate,
                                             this.getJob().getId(),
                                             this.getId(),
                                             this.internalState == null ? "" : this.internalState + "-",
                                             path.getFileName().toString());
                } else {
                    fileName = path.getFileName().toString();
                }
                final String folderName = String.format(nameTemplate,
                                                        this.getJob().getId(),
                                                        this.getId(),
                                                        this.internalState == null ? "" : this.internalState + "-",
                                                        this.component.getId());
                final Path jobPath = Paths.get(getJob().getJobOutputPath());
                try {
                    //FileUtilities.ensureExists(context.getWorkspace().resolve(folderName));
                    FileUtilities.ensureExists(jobPath.resolve(folderName));
                } catch (IOException e) {
                    Logger.getLogger(ProcessingExecutionTask.class.getName()).severe(e.getMessage());
                }
                //setInstanceTargetOutput(FileUtilities.asUnixPath(path.getParent().resolve(folderName).resolve(fileName), true));setInstanceTargetOutput(FileUtilities.asUnixPath(jobPath.resolve(folderName).resolve(fileName), true));
                setInstanceTemporaryOutput(ExecutionConfiguration.getWorkerContainerVolumeMap().getContainerTemporaryFolder() + "/" + fileName);
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
            final Path jobPath = Paths.get(getJob().getJobOutputPath());
            if (!path.isAbsolute()) {
                Path relPath = jobPath.getName(jobPath.getNameCount() - 2).resolve(jobPath.getFileName());
                path = Paths.get(ExecutionConfiguration.getWorkerContainerVolumeMap().getContainerWorkspaceFolder())
                            .resolve(relPath).resolve(path); //context.getNetSpace().resolve(path);
            }
            String fileName = path.getFileName().toString();
            String folderName = String.format(nameTemplate,
                                              this.getJob().getId(),
                                              this.getId(),
                                              this.internalState == null ? "" : this.internalState + "-",
                                              FileUtilities.getFilenameWithoutExtension(fileName));
            /*outPath = new String[] {
                    FileUtilities.asUnixPath(Paths.get(SystemVariable.ROOT.value()).resolve(principalName).resolve(folderName), false),
                    FileUtilities.asUnixPath(path.getParent().resolve(folderName), true)
            };*/
            outPath = new String[] {
                    FileUtilities.asUnixPath(jobPath.resolve(folderName), false),
                    FileUtilities.asUnixPath(path.getParent().resolve(jobPath.getFileName()).resolve(folderName), true)
            };
        }
        return outPath;
    }
}
