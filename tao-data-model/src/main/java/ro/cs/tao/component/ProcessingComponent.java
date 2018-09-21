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
import ro.cs.tao.security.SessionStore;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private String fileLocation;
    private String expandedFileLocation;
    private String workingDirectory;
    private TemplateType templateType;
    private TemplateEngine templateEngine;
    private Template template;
    private Set<Variable> variables;
    private List<ParameterDescriptor> parameters;
    private boolean multiThread;
    private ProcessingComponentVisibility visibility;
    private boolean active;
    private ProcessingComponentType componentType;
    private String owner;


    public ProcessingComponent() {
        super();
    }
    /**
     * Returns the binary/executable of the tool
     */
    public String getFileLocation() {
        return fileLocation;
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
        return expandedFileLocation;
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
        return workingDirectory;
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
        return template;
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
        return variables;
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
    public List<ParameterDescriptor> getParameterDescriptors() {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        return this.parameters;
    }

    /**
     * Sets the parameter descriptors of this component.
     */
    public void setParameterDescriptors(List<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

    /**
     * Check if the current component can run on multiple threads.
     */
    public boolean getMultiThread() {
        return multiThread;
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
        return visibility;
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
        return active;
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
        return templateType != null ? templateType : TemplateType.VELOCITY;
    }

    /**
     * Sets the template type supported by this component.
     */
    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    /**
     * Returns the template contents of the component.
     */
    @JsonGetter(value = "templatecontents")
    public String getTemplateContents(){
        return template != null ? template.getContents() : null;
    }
    /**
     * Sets the template contents of the component.
     */
    @JsonSetter(value = "templatecontents")
    public void setTemplateContents(String contents) {
        this.template = new BasicTemplate();
        template.setTemplateType(getTemplateType());
        template.setContents(contents, false);
        template.associateWith(getTemplateEngine());
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
     * Validates the parameter values agains the parameter descriptors.
     */
    public void validate(Map<String, Object> parameterValues) throws ValidationException {
        if (parameterValues != null) {
            final List<ParameterDescriptor> parameterDescriptors =
                    getParameterDescriptors().stream()
                            .filter(d -> parameterValues.containsKey(d.getId()))
                            .collect(Collectors.toList());
            for (ParameterDescriptor descriptor : parameterDescriptors) {
                descriptor.validate(parameterValues.get(descriptor.getId()));
                List<ParameterDependency> dependencies = descriptor.getDependencies();
                if (dependencies != null) {
                    for (ParameterDependency dependency : dependencies) {
                        ParameterDescriptor dependent = getParameterDescriptors().stream()
                                .filter(p -> dependency.getReferencedParameterId().equals(p.id)).findFirst().orElse(null);
                        if (dependent == null) {
                            throw new ValidationException(String.format("Parameter [%s] depends on parameter with id [%s] which was not found for this component",
                                                                        descriptor.getName(), dependency.getReferencedParameterId()));
                        }
                        Condition condition = dependency.getCondition();
                        String expectedValue = dependency.getExpectedValue();
                        Object referencedParameterValue = parameterValues.get(dependency.getReferencedParameterId());
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
        }
    }

    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }

    public ProcessingComponentType getComponentType() { return componentType; }
    public void setComponentType(ProcessingComponentType componentType) { this.componentType = componentType; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

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
            newDescriptor.parameters = new ArrayList<>();
            for (ParameterDescriptor p : this.parameters) {
                newDescriptor.parameters.add(p.clone());
            }
        }
        newDescriptor.multiThread = this.multiThread;
        newDescriptor.active = this.active;
        newDescriptor.visibility = this.visibility;
        return newDescriptor;
    }

    public String buildExecutionCommand(Map<String, String> parameterValues, Map<String, String> variables) throws TemplateException {
        TemplateEngine templateEngine = getTemplateEngine();
        Map<String, Object> clonedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
            try {
                clonedMap.put(entry.getKey(),
                              Paths.get(URI.create(entry.getValue())).toString());
            } catch (Exception ex) {
                clonedMap.put(entry.getKey(), entry.getValue());
            }
        }
        for (ParameterDescriptor parameterDescriptor : this.parameters) {
            if (!clonedMap.containsKey(parameterDescriptor.getName())) {
                if (parameterDescriptor.getDefaultValue() != null) {
                    clonedMap.put(parameterDescriptor.getName(), parameterDescriptor.getDefaultValue());
                } else {
                    removeEmptyParameter(parameterDescriptor);
                }
            }
        }
        if (variables != null) {
            for (Map.Entry<String, String> variable : variables.entrySet()) {
                clonedMap.put(variable.getKey(), variable.getValue());
            }
        }
        StringBuilder cmdBuilder = new StringBuilder();
        if (this.expandedFileLocation != null) {
            cmdBuilder.append(this.expandedFileLocation).append("\n");
        } else {
            cmdBuilder.append(this.fileLocation).append("\n");
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
                    Files.createDirectories(scriptPath);
                    Path scriptFile = scriptPath.resolve(this.id + "-script");
                    Files.write(scriptFile, transformedTemplate.getBytes());
                    cmdBuilder.append(scriptFile.toString()).append("\n");
                    clonedMap.keySet().retainAll(this.parameters.stream().map(ParameterDescriptor::getName).collect(Collectors.toSet()));
                    for (Map.Entry<String, Object> entry : clonedMap.entrySet()) {
                        cmdBuilder.append("--").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                    }
                } catch (IOException e) {
                    Logger.getLogger(ProcessingComponent.class.getName()).severe("Cannot persist script file");
                    throw new TemplateException(e);
                }
                break;
            case EXECUTABLE:
            default:
                cmdBuilder.append(transformedTemplate).append("\n");
                break;
        }
        return cmdBuilder.toString();
    }

    private void removeEmptyParameter(ParameterDescriptor descriptor) {
        if (this.template != null) {
            String templateContents = this.template.getContents();
            int idx = templateContents.indexOf(descriptor.getLabel());
            int beforeSeparator = templateContents.lastIndexOf('\n', idx);
            int afterSeparator = templateContents.indexOf('\n', idx);
            this.template.setContents(templateContents.substring(0, beforeSeparator) + templateContents.substring(afterSeparator), false);
        }
    }
}
