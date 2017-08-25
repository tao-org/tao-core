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

import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.component.template.engine.EngineFactory;
import ro.cs.tao.component.template.engine.TemplateEngine;
import ro.cs.tao.component.validation.ValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class ProcessingComponent extends TaoComponent {

    private String fileLocation;
    private String workingDirectory;
    private TemplateType templateType;
    private TemplateEngine templateEngine;
    private Template template;
    private List<Variable> variables;
    private List<ParameterDescriptor> parameters;

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
            if (!getTemplateType().equals(template.getType())) {
                throw new TemplateException("Incompatible template type");
            }
            this.template = template;
            this.template.associateWith(getTemplateEngine());
        }
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public List<ParameterDescriptor> getParameterDescriptors() {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        return this.parameters;
    }

    public void setParameters(List<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

    public TemplateType getTemplateType() {
        return templateType != null ? templateType : TemplateType.VELOCITY;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
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
                            .filter(d -> parameterValues.containsKey(d.getName()))
                            .collect(Collectors.toList());
            for (ParameterDescriptor descriptor : parameterDescriptors) {
                descriptor.validate(parameterValues.get(descriptor.getName()));
            }
        }
    }

    @Override
    public String defaultName() {
        return "NewComponent";
    }

    @Override
    public ProcessingComponent copy() {
        ProcessingComponent newDescriptor = new ProcessingComponent();
        newDescriptor.label = this.label;
        newDescriptor.version = this.version;
        newDescriptor.description = this.description;
        newDescriptor.authors = this.authors;
        newDescriptor.copyright = this.copyright;
        newDescriptor.fileLocation = this.fileLocation;
        newDescriptor.workingDirectory = this.workingDirectory;
        newDescriptor.templateType = this.templateType;
        if (this.template != null) {
            newDescriptor.template = this.template.copy();
        }
        if (this.variables != null) {
            newDescriptor.variables = this.variables.stream().map(Variable::copy).collect(Collectors.toList());
        }
        if (this.parameters != null) {
            newDescriptor.parameters = this.parameters.stream().map(p -> {
                ParameterDescriptor parameter = p.copy();
                parameter.setName(p.getName());
                return p;
            }).collect(Collectors.toList());
        }
        if (this.sources != null) {
            newDescriptor.sources = Arrays.copyOf(this.sources, this.sources.length);
        }
        if (this.targets != null) {
            newDescriptor.targets = Arrays.copyOf(this.targets, this.targets.length);
        }
        return newDescriptor;
    }
}
