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

import ro.cs.tao.component.constraints.ConstraintException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "link")
public class ComponentLink {
    private SourceDescriptor output;
    private TargetDescriptor input;

    public static boolean canConnect(TargetDescriptor input, SourceDescriptor output) {
        return input != null && output != null && output.isCompatibleWith(input);
    }

    public ComponentLink(TargetDescriptor input, SourceDescriptor output) throws ConstraintException {
        if (!canConnect(input, output)) {
            throw new ConstraintException("Source and target are not compatible");
        }
        this.input = input;
        this.output = output;
    }

    public SourceDescriptor getOutput() {
        return this.output;
    }

    public TargetDescriptor getInput() {
        return this.input;
    }

    public void follow() {
        this.output.setData(this.input.getData());
    }
}
