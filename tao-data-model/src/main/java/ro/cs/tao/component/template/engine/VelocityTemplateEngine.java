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

package ro.cs.tao.component.template.engine;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class VelocityTemplateEngine extends TemplateEngine {
    private static final String LINE_SEPARATOR = "\r\n|\n";

    private String macroTemplateContents;

    VelocityTemplateEngine() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("macros.vm")))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
                this.macroTemplateContents = builder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public TemplateType getTemplateType() {
        return TemplateType.VELOCITY;
    }

    @Override
    public void parse(Template template) throws TemplateException {
        parse(template.getContents());
    }

    @Override
    public void parse(String text) throws TemplateException {
        VelocityEngine veloEngine = new VelocityEngine();
        veloEngine.init();
        boolean evalResult;
        try {
            evalResult = veloEngine.evaluate(new VelocityContext(), new StringWriter(), "Eval", text);
        } catch (Exception inner) {
            throw new TemplateException(inner);
        }
        if (!evalResult) {
            throw new TemplateException("Template evaluation failed");
        }
    }

    @Override
    public String transform(Template template, Map<String, Object> parameters) throws TemplateException {
        try {
            VelocityEngine veloEngine = new VelocityEngine();
            VelocityContext veloContext = new VelocityContext();
            veloEngine.init();
            org.apache.velocity.Template veloTemplate = createTemplate(veloEngine, template);
            for (String key : parameters.keySet()) {
                veloContext.put(key, parameters.get(key));
            }
            StringWriter writer = new StringWriter();
            veloTemplate.merge(veloContext, writer);
            return writer.toString();
        } catch (Exception inner) {
            throw new TemplateException(inner);
        }
    }

    private org.apache.velocity.Template createTemplate(org.apache.velocity.app.VelocityEngine engine, Template internalTemplate) throws ParseException, IOException {
        org.apache.velocity.Template template;
        if (this.macroTemplateContents != null && !this.macroTemplateContents.isEmpty()) {
            RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
            String veloTemplate = this.macroTemplateContents + "\n" + internalTemplate.getContents();
            StringReader reader = new StringReader(veloTemplate);
            SimpleNode node = runtimeServices.parse(reader, internalTemplate.getName());
            template = new org.apache.velocity.Template();
            template.setRuntimeServices(runtimeServices);
            template.setData(node);
            template.initDocument();
        } else {
            template = engine.getTemplate(internalTemplate.getName());
        }
        return template;
    }
}
