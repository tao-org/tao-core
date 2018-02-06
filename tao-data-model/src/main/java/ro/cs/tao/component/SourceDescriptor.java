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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.component.constraints.ConstraintFactory;
import ro.cs.tao.component.constraints.IOConstraint;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor for an input of a component
 * @author Cosmin Cara
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@XmlRootElement(name = "input")
public class SourceDescriptor extends Identifiable {
    private static final String DEFAULT_NAME = "Input";
    private String parentId;
    private DataDescriptor dataDescriptor;
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
    /**
     * Returns the component that owns this instance
     */
    @XmlTransient
    public String getParentId() { return parentId; }
    /**
     * Sets the component that owns this instance
     *
     * @param parent    The owning component
     */
    public void setParentId(String parent) { this.parentId = parent; }
    /**
     * Returns the data associated to this instance.
     */
    public DataDescriptor getDataDescriptor() {
        return dataDescriptor;
    }
    /**
     * Sets the data associated to this instance.
     *
     * @param data  The data to be associated with this instance.
     */
    public void setDataDescriptor(DataDescriptor descriptor) {
        this.dataDescriptor = descriptor;
    }
    /**
     * Returns a list of constraints to be satisfied by the data of this instance.
     */
    @XmlElementWrapper(name = "constraints")
    @XmlElement(name = "constraint")
    public List<String> getConstraints() {
        return constraints;
    }
    /**
     * Adds a constraint for the data of this instance.
     *
     * @param constraint    The constraint class name.
     */
    public void addConstraint(String constraint) {
        this.constraints.add(constraint);
    }

    /**
     * Verifies if all the constraints defined on this instance are satisfied by the target descriptor.
     *
     * @param other     The target descriptor
     */
    public boolean isCompatibleWith(TargetDescriptor other) {
        return other != null && this.dataDescriptor != null && other.getDataDescriptor() != null &&
                this.dataDescriptor.getFormatType().equals(other.getDataDescriptor().getFormatType()) &&
                (this.constraints.size() == 0 || this.constraints.stream().allMatch(c -> {
                    IOConstraint constraint = ConstraintFactory.create(c);
                    return constraint == null || constraint.check(this.getDataDescriptor(), other.getDataDescriptor());
                }));
    }
}
