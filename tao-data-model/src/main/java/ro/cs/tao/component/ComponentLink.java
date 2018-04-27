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
package ro.cs.tao.component;

import ro.cs.tao.component.constraints.ConstraintException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models a link between an output of a component and an input of another component.
 * From a link perspective, the output of a component is the link input,
 * while the input of a component is the link output.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "link")
public class ComponentLink {
    private SourceDescriptor output;
    private TargetDescriptor input;
    private Long sourceNodeId;

    // default constructor needed for hibernate entity instantiation
    public ComponentLink(){}

    /**
     * Checks that the two arguments are compatible in order to create a link.
     *
     * @param input     The output of a component
     * @param output    The input of a component
     */
    public static boolean canConnect(TargetDescriptor input, SourceDescriptor output) {
        return input != null && output != null && output.isCompatibleWith(input);
    }

    public ComponentLink(long sourceNodeId, TargetDescriptor input, SourceDescriptor output) throws ConstraintException {
        if (!canConnect(input, output)) {
            throw new ConstraintException("Source and target are not compatible");
        }
        this.sourceNodeId = sourceNodeId;
        this.input = input;
        this.output = output;
    }
    /**
     * Returns the output of this link, which is a component input.
     */
    public SourceDescriptor getOutput() {
        return this.output;
    }
    public void setOutput(SourceDescriptor output) {
        this.output = output;
    }

    /**
     * Returns the input of this link, which is a component output.
     */
    public TargetDescriptor getInput() {
        return this.input;
    }
    public void setInput(TargetDescriptor input) {
        this.input = input;
    }

    public Long getSourceNodeId() {
        return sourceNodeId;
    }
    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComponentLink)) {
            return false;
        }
        ComponentLink other = (ComponentLink) obj;
        if (this == other) {
            return true;
        }
        if ((this.input != null && other.input == null) || (this.input == null && other.input != null) ||
                (this.output != null && other.output == null) || (this.output == null && other.output != null) ||
                (this.sourceNodeId != null && other.sourceNodeId == null) ||
                (this.sourceNodeId == null && other.sourceNodeId != null)) {
            return false;
        }
        return this.sourceNodeId.equals(other.sourceNodeId) &&
                (this.input.getId() != null && this.input.getId().equals(other.input.getId())) &&
                (this.output.getId() != null && this.output.getId().equals(other.output.getId()));
    }
}
