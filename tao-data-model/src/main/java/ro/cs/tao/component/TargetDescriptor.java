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
package ro.cs.tao.component;

import ro.cs.tao.component.constraints.ConstraintFactory;
import ro.cs.tao.component.constraints.IOConstraint;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Descriptor of an output of a component
 * @author Cosmin Cara
 */
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@XmlRootElement(name = "output")
public class TargetDescriptor extends StringIdentifiable {
    private static final String DEFAULT_NAME = "Output";
    private String parentId;
    private String name;
    private DataDescriptor dataDescriptor;
    private List<String> constraints;
    private int cardinality = 1;

    public TargetDescriptor() { }

    public TargetDescriptor(String identifier) {
        super(identifier);
        this.constraints = new ArrayList<>();
    }
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

    @Override
    public String defaultId() { return UUID.randomUUID().toString(); }

    public String getName() {
        if (name == null)
            name = DEFAULT_NAME;
        return name;
    }
    public void setName(String name) { this.name = name; }

    /**
     * Returns the cardinality of outputs.
     * If the value is 0, the outputs represent a list of data objects.
     * By default, the cardinality of the outputs is 1.
     */
    public int getCardinality() { return this.cardinality; }
    public void setCardinality(int value) { this.cardinality = value;}
    /**
     * Returns the data associated to this instance.
     */
    public DataDescriptor getDataDescriptor() {
        return dataDescriptor;
    }
    /**
     * Sets the data associated to this instance.
     *
     * @param descriptor  The data to be associated with this instance.
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
        if (this.constraints == null) {
            this.constraints = new ArrayList<>();
        }
        this.constraints.add(constraint);
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    /**
     * Verifies if all the constraints defined on this instance are satisfied by the source descriptor.
     *
     * @param other     The source descriptor
     */
    public boolean isCompatibleWith(SourceDescriptor other) {
        return other != null &&
                (this.constraints == null || this.constraints.size() == 0 || this.constraints.stream().allMatch(c -> {
                    IOConstraint constraint = ConstraintFactory.create(c);
                    return constraint == null || constraint.check(other, this);
                }));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TargetDescriptor that = (TargetDescriptor) o;
        return Objects.equals(parentId, that.parentId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentId, name);
    }

    @Override
    public TargetDescriptor clone() {
        TargetDescriptor clone = new TargetDescriptor();
        clone.name = this.name;
        clone.dataDescriptor = new DataDescriptor();
        clone.dataDescriptor.setSensorType(this.dataDescriptor.getSensorType());
        clone.dataDescriptor.setLocation(this.dataDescriptor.getLocation());
        clone.dataDescriptor.setCrs(this.dataDescriptor.getCrs());
        clone.dataDescriptor.setFormatType(this.dataDescriptor.getFormatType());
        clone.dataDescriptor.setDimension(this.dataDescriptor.getDimension());
        if (this.constraints != null) {
            clone.constraints = new ArrayList<>(this.constraints);
        }
        clone.cardinality = this.cardinality;
        return clone;
    }
}
