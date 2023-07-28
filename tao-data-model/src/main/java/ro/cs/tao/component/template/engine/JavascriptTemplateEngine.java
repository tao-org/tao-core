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
package ro.cs.tao.component.template.engine;

import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;

import javax.script.*;
import java.io.StringWriter;
import java.util.Map;

/**
 * Template engine for JavaScript templates.
 *
 * @author Cosmin Cara
 */
public class JavascriptTemplateEngine extends TemplateEngine {
    private final ScriptEngine scriptEngine;

    public JavascriptTemplateEngine() {
        this.scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    @Override
    public TemplateType getTemplateType() {
        return TemplateType.JAVASCRIPT;
    }

    @Override
    public void parse(Template template) throws TemplateException {
        parse(template.getContents());
    }

    @Override
    public void parse(String text) throws TemplateException {
        try {
            scriptEngine.eval(text);
        } catch (ScriptException ex) {
            throw new TemplateException(ex);
        }
    }

    @Override
    public String transform(Template template, Map<String, Object> parameters) throws TemplateException {
        String result;
        Bindings bindings = new SimpleBindings(parameters);
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        StringWriter writer = new StringWriter();
        ScriptContext context = scriptEngine.getContext();
        context.setWriter(writer);
        try {
            String contents = template.getContents();
            scriptEngine.eval(contents);
            result = writer.toString();
        } catch (ScriptException e) {
            throw new TemplateException(e);
        }
        return result;
    }
}
