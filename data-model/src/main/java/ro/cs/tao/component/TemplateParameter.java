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

import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.engine.TemplateEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class TemplateParameter extends Parameter {
    private List<Parameter> parameters;
    private Template template;
    private TemplateEngine templateEngine;

    public TemplateParameter() {
        super();
    }

    public TemplateParameter(String name, List<Parameter> parameters, Template template, TemplateEngine templateEngine) {
        super(name);
        this.parameters = parameters;
        this.template = template;
        this.templateEngine = templateEngine;
    }

    public Parameter[] getParameters() {
        return parameters != null ?
                parameters.toArray(new Parameter[parameters.size()]) :
                null;
    }

    public void setParameters(Parameter[] parameters) {
        if (parameters != null) {
            if (this.parameters == null) {
                this.parameters = new ArrayList<>();
            }
            Collections.addAll(this.parameters, parameters);
        } else {
            this.parameters = new ArrayList<>();
        }
    }

    public void addParameter(Parameter parameter) {
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

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

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
        for (Parameter parameter : this.parameters) {
            if (!params.containsKey(parameter.getName())) {
                params.put(parameter.getName(), parameter.getDefaultValue());
            }
        }
        return this.templateEngine.transform(this.template, params);
    }

    @Override
    public String defaultName() {
        return "NewTemplateParameter";
    }

    @Override
    public Parameter copy() {
        TemplateParameter newParameter = new TemplateParameter();
        if (this.parameters != null) {
            newParameter.parameters = this.parameters.stream().map(p -> {
                Parameter copy = p.copy();
                copy.setName(p.getName());
                return copy;
            }).collect(Collectors.toList());
        }
        if (this.template != null) {
            newParameter.template = this.template.copy();
        }
        newParameter.templateEngine = this.templateEngine;
        return newParameter;
    }
}
