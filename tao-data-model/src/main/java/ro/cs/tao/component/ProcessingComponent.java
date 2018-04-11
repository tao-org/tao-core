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
package ro.cs.tao.component;

import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.component.template.engine.EngineFactory;
import ro.cs.tao.component.template.engine.TemplateEngine;
import ro.cs.tao.component.validation.ValidationException;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A processing component acts as a descriptor for an external tool to be invoked.
 * @author Cosmin Cara
 */
@XmlRootElement(name = "processingComponent")
public class ProcessingComponent extends TaoComponent {

    private String containerId;
    private String fileLocation;
    private String workingDirectory;
    private TemplateType templateType;
    private TemplateEngine templateEngine;
    private Template template;
    private Set<Variable> variables;
    private List<ParameterDescriptor> parameters;
    private boolean multiThread;
    private ProcessingComponentVisibility visibility;
    private boolean active;

    public ProcessingComponent() {
        super();
    }
    /**
     * Returns the location of the tool (where the tool binaries should be found inside its container)
     */
    public String getFileLocation() {
        return fileLocation;
    }
    /**
     * Sets the location of the tool (where the tool binaries should be found on any machine)
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) throws TemplateException {
        if (template != null) {
            if (!getTemplateType().equals(template.getTemplateType())) {
                throw new TemplateException("Incompatible template type");
            }
            this.template = template;
            this.template.associateWith(getTemplateEngine());
        }
    }

    @XmlElementWrapper(name = "variables")
    public Set<Variable> getVariables() {
        return variables;
    }

    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }

    @XmlElementWrapper(name = "parameters")
    public List<ParameterDescriptor> getParameterDescriptors() {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        return this.parameters;
    }

    public void setParameterDescriptors(List<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

    public boolean getMultiThread() {
        return multiThread;
    }

    public void setMultiThread(boolean multiThread) {
        this.multiThread = multiThread;
    }

    public ProcessingComponentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ProcessingComponentVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @XmlTransient
    public TemplateType getTemplateType() {
        return templateType != null ? templateType : TemplateType.VELOCITY;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public String getTemplateContents(){
        return template != null ? template.getContents() : null;
    }

    public void setTemplateContents(String contents) {
        this.template = new BasicTemplate();
        template.setTemplateType(getTemplateType());
        template.setContents(contents, false);
        template.associateWith(getTemplateEngine());
    }

    public TemplateEngine getTemplateEngine() {
        if (this.templateEngine == null) {
            this.templateEngine = EngineFactory.createInstance(getTemplateType());
        }
        return this.templateEngine;
    }

    public void validate(Map<String, Object> parameterValues) throws ValidationException {
        if (parameterValues != null) {
            final List<ParameterDescriptor> parameterDescriptors =
                    getParameterDescriptors().stream()
                            .filter(d -> parameterValues.containsKey(d.getId()))
                            .collect(Collectors.toList());
            for (ParameterDescriptor descriptor : parameterDescriptors) {
                descriptor.validate(parameterValues.get(descriptor.getId()));
            }
        }
    }

    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }

    @Override
    public String defaultName() {
        return "NewComponent";
    }

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
        return newDescriptor;
    }

    public String buildExecutionCommand(Map<String, String> parameterValues) throws TemplateException {
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
            if (!clonedMap.containsKey(parameterDescriptor.getId()) &&
                    parameterDescriptor.getDefaultValue() != null) {
                clonedMap.put(parameterDescriptor.getId(), parameterDescriptor.getDefaultValue());
            }
        }
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("cmd.exe /c \"");
        cmdBuilder.append(this.fileLocation).append("\n");
        String cmdLine = templateEngine.transform(this.template, clonedMap);
        cmdBuilder.append(cmdLine == null || "null".equals(cmdLine) ? "" : cmdLine);
        cmdBuilder.append("\"");
        return cmdBuilder.toString();
    }

}
