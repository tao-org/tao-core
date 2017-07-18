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

import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public interface TemplateEngine {

    /**
     * Returns the type of templates supported by this template engine
     */
    TemplateType getType();

    /**
     * Parses the given template object.
     *
     * @param template  The template to parse.
     * @throws TemplateException    If the template doesn't follow the type syntax.
     */
    void parse(Template template) throws TemplateException;
    /**
     * Parses the given text according to this engine rules.
     *
     * @param text The text to parse.
     * @throws TemplateException    If the text doesn't follow the type syntax.
     */
    void parse(String text) throws TemplateException;

    /**
     * Transforms the given template into a string by injecting the provided parameters.
     *
     * @param template  The template to transform
     * @return  A string result of the transformation
     * @throws TemplateException    If the template has syntax errors.
     */
    String transform(Template template, Map<String, Object> parameters) throws TemplateException;

}
