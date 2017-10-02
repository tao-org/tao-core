/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.component;

import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.component.template.engine.EngineFactory;
import ro.cs.tao.component.template.engine.TemplateEngine;
import ro.cs.tao.component.validation.ValidationException;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "processingComponent")
public class ProcessingComponent extends TaoComponent {

    private String fileLocation;
    private String workingDirectory;
    private TemplateType templateType;
    private String templateName;
    private TemplateEngine templateEngine;
    private Template template;
    private List<Variable> variables;
    private List<ParameterDescriptor> parameters;
    private Boolean multiThread;
    private ProcessingComponentVisibility visibility;

    public ProcessingComponent() {
        super();
    }

    public String getFileLocation() {
        return fileLocation;
    }

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
    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
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

    public Boolean getMultiThread() {
        return multiThread;
    }

    public void setMultiThread(Boolean multiThread) {
        this.multiThread = multiThread;
    }

    public ProcessingComponentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ProcessingComponentVisibility visibility) {
        this.visibility = visibility;
    }

    @XmlTransient
    public TemplateType getTemplateType() {
        return templateType != null ? templateType : TemplateType.VELOCITY;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    @XmlTransient
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName){
        this.templateName = templateName;
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
            newDescriptor.variables = new ArrayList<>();
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
        clonedMap.putAll(parameterValues);
        String cmdLine = templateEngine.transform(this.template, clonedMap);
        return cmdLine == null || "null".equals(cmdLine) ? "" : cmdLine;
    }

}
