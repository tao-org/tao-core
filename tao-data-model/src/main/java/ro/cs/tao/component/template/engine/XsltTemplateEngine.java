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
package ro.cs.tao.component.template.engine;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class XsltTemplateEngine extends TemplateEngine {
    @Override
    public TemplateType getTemplateType() {
        return TemplateType.XSLT;
    }

    @Override
    public void parse(Template template) throws TemplateException {
        if (template == null) {
            throw new TemplateException("null template");
        }
        parse(template.getContents());
    }

    @Override
    public void parse(String text) throws TemplateException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(new InputSource(new StringReader(text)), new DefaultHandler());
        } catch (Exception ex) {
            throw new TemplateException(ex);
        }
    }

    @Override
    public String transform(Template template, Map<String, Object> parameters) throws TemplateException {
        String result;
        try {
            Source stringSource = new StreamSource(new StringReader(template.getContents()));
            Transformer transformer = TransformerFactory.newInstance().newTransformer(stringSource);
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    if (entry.getValue() != null) {
                        transformer.setParameter(entry.getKey(), entry.getValue());
                    }
                }
            }
            transformer.setOutputProperty("method", "xml");
            transformer.setOutputProperty("indent", "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(), new StreamResult(writer));
            result = writer.toString();
        } catch (Exception ex) {
            throw new TemplateException(ex);
        }
        return result;
    }
}
