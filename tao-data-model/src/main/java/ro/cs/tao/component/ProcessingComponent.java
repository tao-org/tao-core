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
package ro.cs.tao.component;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.component.enums.ComponentCategory;
import ro.cs.tao.component.enums.Condition;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.component.template.engine.EngineFactory;
import ro.cs.tao.component.template.engine.TemplateEngine;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A processing component acts as a descriptor for an external tool to be invoked.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "processingComponent")
public class ProcessingComponent extends TaoComponent {

    private String containerId;
    private ComponentCategory category;
    private String fileLocation;
    private String expandedFileLocation;
    private String workingDirectory;
    private TemplateType templateType;
    private TemplateEngine templateEngine;
    private Template template;
    private Set<Variable> variables;
    private Set<ParameterDescriptor> parameters;
    private boolean multiThread;
    private Integer parallelism;
    private ProcessingComponentVisibility visibility;
    private boolean active;
    private ProcessingComponentType componentType;
    private String owner;
    private boolean isTransient;
    private Boolean outputManaged;


    public ProcessingComponent() {
        super();
    }
    /**
     * Returns the binary/executable of the tool
     */
    public String getFileLocation() {
        return this.fileLocation;
    }
    /**
     * Sets the binary/executable of the tool
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    /**
     * Returns the full path (inside the container) of the tool binary/executable.
     * This path is computed at execution time.
     */
    @JsonIgnore
    public String getExpandedFileLocation() {
        return this.expandedFileLocation;
    }
    /**
     * Sets the full path (inside the container) of the tool binary/executable.
     * This path is computed at execution time.
     */
    public void setExpandedFileLocation(String expandedFileLocation) {
        this.expandedFileLocation = expandedFileLocation;
    }

    /**
     * Returns the working directory for the tool.
     */
    public String getWorkingDirectory() {
        return this.workingDirectory;
    }

    /**
     * Sets the working directory for the tool.
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Returns the template of this component.
     */
    @JsonIgnore
    public Template getTemplate() {
        return this.template;
    }

    /**
     * Sets the template of this component.
     * If the template is not of the type supported by this component, an exception is thrown.
     */
    @JsonIgnore
    public void setTemplate(Template template) throws TemplateException {
        if (template != null) {
            if (!getTemplateType().equals(template.getTemplateType())) {
                throw new TemplateException("Incompatible template type");
            }
            this.template = template;
            this.template.associateWith(getTemplateEngine());
        }
    }

    /**
     * Returns the variables of this component.
     */
    @XmlElementWrapper(name = "variables")
    public Set<Variable> getVariables() {
        return this.variables;
    }

    /**
     * Sets the variables of this component.
     */
    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }

    /**
     * Returns the parameter descriptors of this component.
     */
    @XmlElementWrapper(name = "parameters")
    public Set<ParameterDescriptor> getParameterDescriptors() {
        if (this.parameters == null) {
            this.parameters = new LinkedHashSet<>();
        }
        return this.parameters;
    }

    /**
     * Sets the parameter descriptors of this component.
     */
    public void setParameterDescriptors(Set<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterDescriptor parameterDescriptor) {
        final Set<ParameterDescriptor> descriptors = getParameterDescriptors();
        descriptors.add(parameterDescriptor);
    }

    /**
     * Check if the current component can run on multiple threads.
     */
    public boolean getMultiThread() {
        return this.multiThread;
    }
    /**
     * Instructs the current component to run on multiple threads.
     */
    public void setMultiThread(boolean multiThread) {
        this.multiThread = multiThread;
    }

    /**
     * Returns the visibility of the component.
     */
    public ProcessingComponentVisibility getVisibility() {
        return this.visibility;
    }

    /**
     * Sets the visibility of the component.
     */
    public void setVisibility(ProcessingComponentVisibility visibility) {
        this.visibility = visibility;
    }

    /**
     * Checks if the component is active (i.e. not deleted).
     */
    public boolean getActive() {
        return this.active;
    }

    /**
     * Sets the active status of the component.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the template type supported by this component.
     */
    @XmlTransient
    public TemplateType getTemplateType() {
        return this.templateType != null ? this.templateType : TemplateType.VELOCITY;
    }

    /**
     * Sets the template type supported by this component.
     */
    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
        // TODO LBU: TO VERIFY!!!!!
        if (this.templateEngine != null && !this.templateEngine.getTemplateType().equals(templateType)) {
            this.templateEngine = EngineFactory.createInstance(templateType);
        }
    }

    /**
     * Returns the template contents of the component.
     */
    @JsonGetter(value = "templatecontents")
    public String getTemplateContents(){
        return this.template != null ? this.template.getContents() : null;
    }
    /**
     * Sets the template contents of the component.
     */
    @JsonSetter(value = "templatecontents")
    public void setTemplateContents(String contents) {
        this.template = new BasicTemplate();
        this.template.setTemplateType(getTemplateType());
        this.template.setContents(contents, false);
        this.template.associateWith(getTemplateEngine());
    }

    /**
     * Returns the template engine associated with the component.
     */
    @JsonIgnore
    public TemplateEngine getTemplateEngine() {
        if (this.templateEngine == null) {
            this.templateEngine = EngineFactory.createInstance(getTemplateType());
        }
        return this.templateEngine;
    }

    /**
     * Returns the recommended degree of parallelism for this component.
     */
    public Integer getParallelism() {
        if (this.parallelism == null) {
            this.parallelism = 1;
        }
        return this.parallelism;
    }
    /**
     * Sets the recommended degree of parallelsim for this component
     */
    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    /**
     * Returns the component category
     */
    public ComponentCategory getCategory() {
        return category;
    }

    /**
     * Sets the component category
     */
    public void setCategory(ComponentCategory category) {
        this.category = category;
    }

    /**
     * Tests if the output of this component is managed by TAO.
     * If <code>false</code>, the output is managed by this component.
     */
    public Boolean isOutputManaged() {
        if (outputManaged == null) {
            outputManaged = true;
        }
        return outputManaged;
    }

    /**
     * Sets the output management. If <code>true</code> it will be managed by TAO.
     * If <code>false</code> it will be managed by the component itself.
     */
    public void setOutputManaged(Boolean value) {
        outputManaged = value;
    }

    /**
     * Validates the parameter values against the parameter descriptors.
     */
    public void validate(Map<String, Object> parameterValues) throws ValidationException {
        if (parameterValues != null) {
            final List<ParameterDescriptor> parameterDescriptors =
                    getParameterDescriptors().stream()
                            .filter(d -> parameterValues.containsKey(d.getId()))
                            .collect(Collectors.toList());
            for (ParameterDescriptor descriptor : parameterDescriptors) {
                switch (descriptor.getType()) {
                    case REGULAR:
                        validateParameter(descriptor, parameterValues);
                        break;
                    case TEMPLATE:
                        final Map<String, Object> templateValues = (Map<String, Object>) parameterValues.get(descriptor.getId());
                        final List<ParameterDescriptor> children = ((TemplateParameterDescriptor) descriptor).getParameters();
                        for (ParameterDescriptor child : children) {
                            validateParameter(child, templateValues);
                        }
                        break;
                }
            }
        }
    }

    public String getContainerId() { return this.containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }

    public ProcessingComponentType getComponentType() { return componentType; }
    public void setComponentType(ProcessingComponentType componentType) { this.componentType = componentType; }

    public String getOwner() { return this.owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public boolean isTransient() { return this.isTransient; }

    public void setTransient(boolean value) { this.isTransient = value; }

    @Override
    public ProcessingComponent clone() throws CloneNotSupportedException {
        ProcessingComponent newDescriptor = (ProcessingComponent) super.clone();
        newDescriptor.fileLocation = this.fileLocation;
        newDescriptor.workingDirectory = this.workingDirectory;
        newDescriptor.templateType = this.templateType;
        if (this.template != null) {
            newDescriptor.template = this.template.copy();
        }
        if (this.variables != null) {
            newDescriptor.variables = new HashSet<>();
            for (Variable var : this.variables) {
                newDescriptor.variables.add(var.clone());
            }
        }
        if (this.parameters != null) {
            newDescriptor.parameters = new LinkedHashSet<>();
            for (ParameterDescriptor p : this.parameters) {
                newDescriptor.parameters.add(p.clone());
            }
        }
        newDescriptor.multiThread = this.multiThread;
        newDescriptor.parallelism = this.parallelism;
        newDescriptor.active = this.active;
        newDescriptor.visibility = this.visibility;
        return newDescriptor;
    }

    public String buildExecutionCommand(Map<String, Object> parameterValues, Map<String, String> variables) throws TemplateException {
        TemplateEngine templateEngine = getTemplateEngine();
        Map<String, Object> clonedMap = new HashMap<>();
        Map<String, TemplateParameterDescriptor> templateParameters =
                this.parameters.stream().filter(p -> p instanceof TemplateParameterDescriptor)
                               .collect(Collectors.toMap(ParameterDescriptor::getName, p -> (TemplateParameterDescriptor) p));
        Map<String, ParameterDescriptor> arrayParameters =
                this.parameters.stream().filter(p -> p.getDataType().isArray())
                               .collect(Collectors.toMap(ParameterDescriptor::getName, Function.identity()));
        if (parameterValues != null) {
            for (Map.Entry<String, Object> entry : parameterValues.entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();
                if (templateParameters.containsKey(paramName)) {
                    Map<String, Object> values = (Map<String, Object>) parameterValues.get(paramName);
                    TemplateParameterDescriptor descriptor = templateParameters.get(paramName);
                    TemplateEngine paramEngine = descriptor.getTemplateEngine();
                    // A template parameter can also use values from the parent's regular parameters
                    values.putAll(parameterValues);
                    String transformed = paramEngine.transform(descriptor.getTemplate(), values);
                    if ("file".equalsIgnoreCase(descriptor.getFormat())) {
                        String outputPath = variables.get("$TEMPLATE_REAL_PATH");
                        String templateCmdPath = variables.get("$TEMPLATE_CMD_PATH");
                        if (outputPath != null && templateCmdPath != null) {
                            String file = outputPath + "/" + descriptor.getDefaultValue();
                            try {
                                Files.write(Paths.get(file), transformed.getBytes());
                                clonedMap.put(paramName, templateCmdPath + "/" + descriptor.getDefaultValue());
                            } catch (IOException e) {
                                Logger.getLogger(getClass().getName()).severe(String.format("Cannot write transformed template '%s'. Reason: %s",
                                                                                            file, e.getMessage()));
                            }
                        } else {
                            Logger.getLogger(getClass().getName()).warning(String.format("Parameter '%s' is a template parameter, but the output path cannot be determined",
                                                                                         paramName));
                        }
                    } else {
                        clonedMap.put(paramName, transformed);
                    }
                } else if (arrayParameters.containsKey(paramName)) {
                    Object values = parameterValues.get(paramName);
                    if (values != null) {
                        ParameterDescriptor descriptor = arrayParameters.get(paramName);
                        clonedMap.put(paramName, descriptor.expandValues(values));
                    }
                } else {
                    try {
                        if (paramValue instanceof String
                                && (((String) paramValue).contains(SystemUtils.IS_OS_WINDOWS ? "\\" : "/"))) {
                            clonedMap.put(paramName, FileUtilities.asUnixPath(FileUtilities.toPath((String) paramValue).toString(), true));
                        } else {
                            clonedMap.put(paramName, paramValue);
                        }
                    } catch (Exception ex) {
                        clonedMap.put(paramName, paramValue);
                    }
                }
            }
        }
        for (ParameterDescriptor parameterDescriptor : this.parameters) {
            if (!clonedMap.containsKey(parameterDescriptor.getName())) {
                if (StringUtilities.isNullOrEmpty(parameterDescriptor.getDefaultValue())) {
                    removeEmptyParameter(parameterDescriptor);
                } else {
                    clonedMap.put(parameterDescriptor.getName(), parameterDescriptor.getDefaultValue());
                }
            }
        }
        if (variables != null) {
            clonedMap.putAll(variables);
        }
        StringBuilder cmdBuilder = new StringBuilder();
        if (this.expandedFileLocation != null) {
            cmdBuilder.append(this.expandedFileLocation.replace('\\', '/')).append("\n");
        } else {
            cmdBuilder.append(this.fileLocation.replace('\\', '/')).append("\n");
        }
        String transformedTemplate = templateEngine.transform(this.template, clonedMap);
        if (transformedTemplate == null || "null".equals(transformedTemplate)) {
            Logger.getLogger(ProcessingComponent.class.getName())
                    .warning(String.format("Component %s produced an empty invocation template", this.id));
            transformedTemplate = "";
        }
        if (this.componentType == null) {
            this.componentType = ProcessingComponentType.EXECUTABLE;
        }
        switch (this.componentType) {
            case SCRIPT:
                try {
                    Path scriptPath = SessionStore.currentContext().getWorkspace().resolve("scripts");
                    FileUtilities.createDirectories(scriptPath);
                    Path scriptFile = scriptPath.resolve(this.id + "-script");
                    Files.write(scriptFile, transformedTemplate.getBytes());
                    cmdBuilder.append(scriptFile).append("\n");
                    clonedMap.keySet().retainAll(this.parameters.stream().map(ParameterDescriptor::getName).collect(Collectors.toSet()));
                    for (Map.Entry<String, Object> entry : clonedMap.entrySet()) {
                        cmdBuilder.append("--").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                    }
                } catch (IOException e) {
                    Logger.getLogger(ProcessingComponent.class.getName()).severe("Cannot persist script file");
                    throw new TemplateException(e);
                }
                break;
            case AGGREGATE:
                try {
                    final Path scriptPath = SessionStore.currentContext().getWorkspace().resolve("scripts");
                    FileUtilities.createDirectories(scriptPath);
                    String scriptFileName = this.template.getName().replace(" ", "_") + "-" + System.currentTimeMillis();
                    final boolean velocityTemplate = this.templateType == TemplateType.VELOCITY;
                    if (velocityTemplate) {
                        // OTB aggregation
                        scriptFileName += ".py";
                    } else {
                        // SNAP aggregation
                        scriptFileName += ".xml";
                        transformedTemplate = transformedTemplate.replace("[", "{").replace("]", "}");
                    }
                    final Path scriptFile = scriptPath.resolve(scriptFileName);
                    Files.write(scriptFile, transformedTemplate.getBytes());
                    String relScriptFile = ExecutionConfiguration.getWorkerContainerVolumeMap().getContainerWorkspaceFolder() +
                            "/" + FileUtilities.asUnixPath(scriptFile, true)
                                               .replace(FileUtilities.asUnixPath(Paths.get(SystemVariable.ROOT.value()), true), "");
                    cmdBuilder.append(relScriptFile).append("\n");
                    for (SourceDescriptor sourceDescriptor : this.sources) {
                        if (parameterValues != null && parameterValues.containsKey(sourceDescriptor.getName())) {
                            cmdBuilder.append("-").append(sourceDescriptor.getName()).append(velocityTemplate ? "\n" : "=");
                            cmdBuilder.append(parameterValues.get(sourceDescriptor.getName())).append("\n");
                        }
                    }
                    for (TargetDescriptor targetDescriptor : this.targets) {
                        if (parameterValues != null && parameterValues.containsKey(targetDescriptor.getName())) {
                            cmdBuilder.append("-").append(targetDescriptor.getName()).append("\n");
                            cmdBuilder.append(parameterValues.get(targetDescriptor.getName())).append("\n");
                        }
                    }
                } catch (IOException e) {
                    Logger.getLogger(ProcessingComponent.class.getName()).severe("Cannot persist aggregated template file");
                    throw new TemplateException(e);
                }
                break;
            case EXTERNAL:
            case EXECUTABLE:
            default:
                cmdBuilder.append(transformedTemplate).append("\n");
                break;
        }
        return cmdBuilder.toString();
    }

    private void validateParameter(final ParameterDescriptor descriptor, final Map<String, Object> values) {
        descriptor.validate(values.get(descriptor.getId()));
        final List<ParameterDependency> dependencies = descriptor.getDependencies();
        if (dependencies != null) {
            for (ParameterDependency dependency : dependencies) {
                final ParameterDescriptor dependent = getParameterDescriptors().stream()
                        .filter(p -> dependency.getReferencedParameterId().equals(p.id)).findFirst().orElse(null);
                if (dependent == null) {
                    throw new ValidationException(String.format("Parameter [%s] depends on parameter with id [%s] which was not found for this component",
                                                                descriptor.getName(), dependency.getReferencedParameterId()));
                }
                final Condition condition = dependency.getCondition();
                final String expectedValue = dependency.getExpectedValue();
                final Object referencedParameterValue = values.get(dependency.getReferencedParameterId());
                boolean isValid = true;
                switch (condition) {
                    case EQ:
                        isValid = expectedValue != null ? expectedValue.equals(String.valueOf(referencedParameterValue)) :
                                referencedParameterValue == null;
                        break;
                    case NEQ:
                        isValid = expectedValue != null ? !expectedValue.equals(String.valueOf(referencedParameterValue)) :
                                referencedParameterValue != null;
                        break;
                    case LT:
                        isValid = expectedValue != null && expectedValue.compareTo(String.valueOf(referencedParameterValue)) < 0;
                        break;
                    case LTE:
                        isValid = expectedValue != null && expectedValue.compareTo(String.valueOf(referencedParameterValue)) <= 0;
                        break;
                    case GT:
                        isValid = expectedValue != null && expectedValue.compareTo(String.valueOf(referencedParameterValue)) > 0;
                        break;
                    case GTE:
                        isValid = expectedValue != null && expectedValue.compareTo(String.valueOf(referencedParameterValue)) >= 0;
                        break;
                    case IN:
                        isValid = expectedValue != null && dependency.expectedValues().contains(expectedValue);
                        break;
                    case NOTIN:
                        isValid = expectedValue == null || !dependency.expectedValues().contains(expectedValue);
                        break;
                }
                if (!isValid) {
                    throw new ValidationException(String.format("Parameter [%s] is dependent on parameter [%s]. Expected value: [%s]. Found: [%s]",
                                                                descriptor.getName(), dependent.getName(),
                                                                expectedValue, referencedParameterValue));
                }
            }
        }
    }

    private void removeEmptyParameter(ParameterDescriptor descriptor) {
        if (this.template != null) {
            String templateContents = this.template.getContents();
            int idx;
            switch (this.templateType) {
                case XSLT:
                case JAVASCRIPT:
                    idx = templateContents.indexOf(descriptor.getLabel());
                    int beforeSeparator, afterSeparator;
                    // idx should not be -1 for Velocity templates, but it may be for XSLT ones
                    if (idx == -1) {
                        idx = templateContents.indexOf(descriptor.getName());
                        beforeSeparator = templateContents.lastIndexOf('\n', idx);
                        afterSeparator = templateContents.indexOf('\n', idx);
                        this.template.setContents(templateContents.substring(0, beforeSeparator) + templateContents.substring(afterSeparator), false);
                        idx = templateContents.indexOf(descriptor.getName(), idx + 1);
                        beforeSeparator = templateContents.lastIndexOf('\n', idx);
                        afterSeparator = templateContents.indexOf('\n', idx);
                        this.template.setContents(templateContents.substring(0, beforeSeparator) + templateContents.substring(afterSeparator), false);
                    } else {
                        beforeSeparator = templateContents.lastIndexOf('\n', idx);

                        afterSeparator = templateContents.indexOf('\n', idx);
                        this.template.setContents(templateContents.substring(0, beforeSeparator) + templateContents.substring(afterSeparator), false);
                    }
                    break;
                case JSON:
                    idx = templateContents.indexOf("$" + descriptor.getName());
                    int sepIdx = templateContents.indexOf(",", idx);
                    int columnIdx = templateContents.lastIndexOf(":", idx);
                    int beforeIdx = templateContents.lastIndexOf("\"", columnIdx - 1);
                    beforeIdx = templateContents.lastIndexOf("\"", beforeIdx - 1);
                    this.template.setContents(templateContents.substring(0, beforeIdx) +
                                              templateContents.substring(sepIdx + 1), false);
                    break;
                case VELOCITY:
                default:
                    final String[] lines = templateContents.split("\n");
                    final StringBuilder builder = new StringBuilder();
                    for (String line : lines) {
                        if (!line.contains("$" + descriptor.getName()) && !line.contains(descriptor.getLabel())) {
                            builder.append(line).append("\n");
                        }
                    }
                    this.template.setContents(builder.toString(), false);
                    break;
            }
        }
    }
}
