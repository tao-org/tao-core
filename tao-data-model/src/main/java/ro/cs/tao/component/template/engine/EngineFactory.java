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

import ro.cs.tao.component.template.TemplateType;

/**
 * Factory for creating template engines.
 *
 * @author Cosmin Cara
 */
public class EngineFactory {

    /**
     * Creates an instance of a template engine for the given template type.
     * @param templateType  The template type.
     */
    public static TemplateEngine createInstance(TemplateType templateType) {
        switch (templateType) {
            case JAVASCRIPT:
                return new JavascriptTemplateEngine();
            case XSLT:
                return new XsltTemplateEngine();
            case JSON:
                return new JsonEngine();
            case VELOCITY:
            default:
                return new VelocityTemplateEngine();
        }
    }

}
