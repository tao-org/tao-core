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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.eodata.naming.NameExpressionParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Apache Velocity template engine.
 *
 * @author Cosmin Cara
 */
public class VelocityTemplateEngine extends TemplateEngine {
    private static final String LINE_SEPARATOR = "\r\n|\n";

    private static String macroTemplateContents;

    private final VelocityEngine veloEngine;
    private final StringResourceRepository repository;

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(VelocityTemplateEngine.class.getResourceAsStream("macros.vm")))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
                macroTemplateContents = builder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VelocityTemplateEngine() {
        veloEngine = new VelocityEngine();
        veloEngine.setProperty(Velocity.RESOURCE_LOADERS, "string");
        veloEngine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        veloEngine.setProperty("resource.loader.string.cache", true);
        veloEngine.setProperty("resource.loader.string.modification_check_interval", 60);
        veloEngine.init();
        repository = StringResourceLoader.getRepository();
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
            // Velocity can handle only one parameter per line
            // If the template contains concatenation of parameters, we need to replace them with actual values
            /*String contents = template.getContents();
            String[] lines = contents.split("\n");
            for (int i = 0; i < lines.length; i++) {
                int idx = lines[i].indexOf('$');
                idx = lines[i].indexOf('$', idx + 1);
                if (idx > 0) {
                    Set<String> keys = parameters.keySet();
                    for (String key : keys) {
                        lines[i] = lines[i].replace("$" + key, String.valueOf(parameters.get(key)));
                    }
                }
            }
            template.setContents(String.join("\n", lines), false);*/
            org.apache.velocity.Template veloTemplate = createTemplate(template);
            VelocityContext veloContext = new VelocityContext();
            for (String key : parameters.keySet()) {
                veloContext.put(key, parameters.get(key));
            }
            StringWriter writer = new StringWriter();
            veloTemplate.merge(veloContext, writer);
            // Use NameExpressionParser to expand any custom functions used in the template
            return NameExpressionParser.resolve(writer.toString());
        } catch (Exception inner) {
            throw new TemplateException(inner);
        }
    }

    private org.apache.velocity.Template createTemplate(Template internalTemplate) throws ParseException {
        org.apache.velocity.Template template;
        if (macroTemplateContents != null && !macroTemplateContents.isEmpty()) {
            RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
            String veloTemplate = macroTemplateContents + "\n" + internalTemplate.getContents();
            repository.putStringResource(internalTemplate.getName(), veloTemplate);
            StringReader reader = new StringReader(veloTemplate);
            SimpleNode node = runtimeServices.parse(reader, veloEngine.getTemplate(internalTemplate.getName()));
            template = new org.apache.velocity.Template();
            template.setRuntimeServices(runtimeServices);
            template.setData(node);
            template.initDocument();
        } else {
            repository.putStringResource(internalTemplate.getName(), internalTemplate.getContents());
            template = veloEngine.getTemplate(internalTemplate.getName());
        }
        return template;
    }
}
