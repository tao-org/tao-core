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

import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.engine.TemplateEngine;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Descriptor of a template parameter. A template parameter is a special type of parameter that, in turn, can have its
 * own parameters. Usually, template parameters model a configuration collection (such as a GIPP file) and produce, when
 * compiled, a physical file.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "templateParameter")
public class TemplateParameterDescriptor extends ParameterDescriptor {
    private List<ParameterDescriptor> parameters;
    private Template template;
    private TemplateEngine templateEngine;

    public TemplateParameterDescriptor() {
        super();
    }

    public TemplateParameterDescriptor(String identifier, List<ParameterDescriptor> parameters, Template template, TemplateEngine templateEngine) {
        super(identifier);
        this.parameters = parameters;
        this.template = template;
        this.templateEngine = templateEngine;
    }

    @XmlElementWrapper(name = "parameters")
    public List<ParameterDescriptor> getParameters() {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        return this.parameters;
    }

    public void setParameters(List<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterDescriptor parameter) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.add(parameter);
    }

    public Template getTemplate() {
        if (this.template == null) {
            this.template = new BasicTemplate();
            if (this.templateEngine != null) {
                try {
                    this.template.associateWith(this.templateEngine);
                } catch (TemplateException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.template;
    }

    public void setTemplate(Template template) throws TemplateException {
        this.template = template;
        this.template.associateWith(this.templateEngine);
    }

    @XmlTransient
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    @XmlTransient
    public void setTemplateEngine(TemplateEngine templateEngine) throws TemplateException {
        this.templateEngine = templateEngine;
        if (this.template != null) {
            this.template.associateWith(this.templateEngine);
        }
    }

    public String transform(Map<String, Object> params) throws TemplateException {
        if (this.templateEngine == null) {
            throw new NullPointerException("Template engine not set");
        }
        if (params == null) {
            params = new HashMap<>();
        }
        for (ParameterDescriptor parameter : this.parameters) {
            if (!params.containsKey(parameter.getName())) {
                params.put(parameter.getName(), parameter.getDefaultValue());
            } else {
                removeEmptyParameter(parameter);
            }
        }
        return this.templateEngine.transform(this.template, params);
    }

    @Override
    public String defaultId() { return "NewTemplateParameter"; }

    @Override
    public ParameterDescriptor clone() throws CloneNotSupportedException {
        TemplateParameterDescriptor newParameter = (TemplateParameterDescriptor) super.clone();
        if (this.parameters != null) {
            newParameter.parameters = this.parameters.stream().map(p -> {
                ParameterDescriptor copy = null;
                try {
                    copy = p.clone();
                    copy.setName(p.getName());
                } catch (CloneNotSupportedException ignored) {}
                return copy;
            }).collect(Collectors.toList());
        }
        if (this.template != null) {
            newParameter.template = this.template.copy();
        }
        newParameter.templateEngine = this.templateEngine;
        return newParameter;
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
