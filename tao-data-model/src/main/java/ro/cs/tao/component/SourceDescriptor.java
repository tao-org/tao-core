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

import ro.cs.tao.component.constraints.ConstraintFactory;
import ro.cs.tao.eodata.EOData;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor for an input of a component
 * @author Cosmin Cara
 */
@XmlRootElement(name = "input")
public class SourceDescriptor extends Identifiable {
    private static final String DEFAULT_NAME = "Input";
    private ProcessingComponent parent;
    private EOData data;
    private List<String> constraints;

    public SourceDescriptor() {
        this(DEFAULT_NAME);
    }

    public SourceDescriptor(String identifier) {
        super(identifier);
        this.constraints = new ArrayList<>();
    }

    @Override
    public String defaultName() { return DEFAULT_NAME; }

    public ProcessingComponent getParent() { return parent; }
    public void setParent(ProcessingComponent parent) { this.parent = parent; }

    @XmlTransient
    public EOData getData() {
        return data;
    }

    public void setData(EOData data) {
        this.data = data;
    }

    @XmlElementWrapper(name = "constraints")
    public List<String> getConstraints() {
        return constraints;
    }

    public void addConstraint(String constraint) {
        this.constraints.add(constraint);
    }

    public boolean isCompatibleWith(TargetDescriptor other) {
        return other != null && (this.constraints.size() == 0 ||
                this.constraints.stream().allMatch(c -> ConstraintFactory.create(c).check(this, other)));
    }
}
