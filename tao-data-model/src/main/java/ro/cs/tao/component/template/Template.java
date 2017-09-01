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
@XmlRootElement(name = "template")
public interface Template {

    /**
     * Returns the name (identifier) of the template
     */
    String getName();

    /**
     * Sets the name (identifier) of the template
     */
    void setName(String value);

    /**
     * Associates this template with an instance of a template engine
     * @param engine    The template engine to associate with
     * @throws TemplateException    If the engine doesn't support the type of this template
     */
    void associateWith(TemplateEngine engine) throws TemplateException;

    /**
     * Returns the contents of the template as text
     */
    String getContents();

    /**
     * Sets the contents of this template
     * @param text  The new template content
     * @param shouldParse   If <code>true</code>, the contents will be first parsed before assignment
     * @throws TemplateException    If the contents contain syntax errors.
     */
    void setContents(String text, boolean shouldParse) throws TemplateException;

    /**
     * Returns the type of this template.
     */
    TemplateType getType();

    /**
     * Sets the type of this template.
     */
    void setType(TemplateType value);

    /**
     * Produces a copy of this template.
     */
    Template copy();

    /**
     * Persists this template.
     */
    void save();
}
