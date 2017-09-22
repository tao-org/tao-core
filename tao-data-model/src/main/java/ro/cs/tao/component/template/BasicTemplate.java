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

package ro.cs.tao.component.template;

import ro.cs.tao.component.template.engine.TemplateEngine;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Cosmin Cara
 */
@XmlRootElement
public class BasicTemplate extends Template {
    private static final String DEFAULT_NAME = "Command Template";

    private String name;
    private TemplateType templateType;
    private TemplateEngine engine;
    private String contents;

    public BasicTemplate() {
        this.name = DEFAULT_NAME;
    }

    public BasicTemplate(String name, TemplateType templateType) {
        this.name = name;
        this.templateType = templateType;
    }

    public BasicTemplate(String name, TemplateEngine templateEngine) {
        if (templateEngine == null) {
            throw new NullPointerException("Argument 'templateEngine' cannot be null");
        }
        this.engine = templateEngine;
        this.templateType = this.engine.getType();
        this.name = name != null ? name : DEFAULT_NAME;
    }

    @Override
    public String getName() {
        return this.name != null ? this.name : DEFAULT_NAME;
    }

    @Override
    public void setName(String value) {
        this.name = value;
    }

    @Override
    public void associateWith(TemplateEngine engine) throws TemplateException {
        if (engine == null) {
            throw new TemplateException("Null template engine");
        }
        if (this.templateType == null || engine.getType().equals(this.templateType)) {
            this.engine = engine;
        } else {
            throw new TemplateException("Wrong template engine type");
        }
    }

    @Override
    public String getContents() {
        return this.contents;
    }

    public void setContents(String text) throws TemplateException {
        setContents(text, false);
    }

    @Override
    public void setContents(String text, boolean shouldParse) throws TemplateException {
        if (text == null) {
            throw new TemplateException("Cannot parse null content");
        }
        text = text.replace("\r", "");
        if (shouldParse) {
            this.engine.parse(text);
        }
        this.contents = text;
    }

    @Override
    public TemplateType getTemplateType() {
        return templateType != null ? templateType : TemplateType.VELOCITY;
    }

    @Override
    public void setTemplateType(TemplateType value) {
        this.templateType = value;
        this.engine = null;
    }

    @Override
    public Template copy() {
        BasicTemplate copy = new BasicTemplate(DEFAULT_NAME, this.templateType);
        copy.contents = this.contents;
        copy.engine = this.engine;
        return copy;
    }

    @Override
    public void save() {
        // delegate to persistence layer
    }
}
