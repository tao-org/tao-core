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

import ro.cs.tao.component.constraints.Constraint;
import ro.cs.tao.component.constraints.ConstraintException;
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
 * Descriptor for an input of a component
 * @author Cosmin Cara
 */
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@XmlRootElement(name = "input")
public class SourceDescriptor extends StringIdentifiable {
    private static final String DEFAULT_NAME = "Input";
    private String parentId;
    private String name;
    private DataDescriptor dataDescriptor;
    private List<String> constraints;
    private int cardinality;
    private String referencedSourceDescriptorId;

    public SourceDescriptor() { }

    public SourceDescriptor(String identifier) {
        super(identifier);
        this.constraints = new ArrayList<>();
    }

    @Override
    public String defaultId() { return UUID.randomUUID().toString(); }
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

    public String getName() {
        if (name == null)
            name = DEFAULT_NAME;
        return name;
    }
    public void setName(String name) { this.name = name; }

    /**
     * Returns the cardinality of inputs.
     * If the value is 0, the inputs represent a list of data objects.
     */
    public int getCardinality() { return this.cardinality; }
    public void setCardinality(int value) { this.cardinality = value;}

    /**
     * Returns the data descriptor associated to this instance.
     */
    public DataDescriptor getDataDescriptor() {
        return dataDescriptor;
    }
    /**
     * Sets the data descriptor associated to this instance.
     *
     * @param descriptor  The data descriptors to be associated with this instance.
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
     * Returns the referenced source descriptor identifier.
     * Returns a non-null value only when the current descriptor belongs to a group component.
     */
    public String getReferencedSourceDescriptorId() {
        return referencedSourceDescriptorId;
    }

    public void setReferencedSourceDescriptorId(String referencedSourceDescriptorId) {
        this.referencedSourceDescriptorId = referencedSourceDescriptorId;
    }

    /**
     * Verifies if all the constraints defined on this instance are satisfied by the target descriptor.
     * This method only indicates the compatibility or not. For detailed messages use {@see checkCompatible}.
     *
     * @param other     The target descriptor
     */
    public boolean isCompatibleWith(TargetDescriptor other) {
        return other != null && this.dataDescriptor != null && other.getDataDescriptor() != null &&
                this.dataDescriptor.getFormatType().equals(other.getDataDescriptor().getFormatType()) &&
                (this.constraints == null || this.constraints.isEmpty() || this.constraints.stream().allMatch(c -> {
                    IOConstraint constraint = ConstraintFactory.create(c);
                    return constraint == null || constraint.check(this.dataDescriptor, other.getDataDescriptor());
                }));
    }
    /**
     * Verifies if all the constraints defined on this instance are satisfied by the target descriptor.
     * If inconsistencies or incompatibilities are found, exceptions are thrown.
     *
     * @param other     The target descriptor
     */
    public void checkCompatible(TargetDescriptor other) throws ConstraintException {
        if (other == null) {
            throw new ConstraintException("TargetDescriptor is not defined");
        }
        if (this.dataDescriptor == null) {
            throw new ConstraintException("SourceDescriptor is empty");
        }
        if (other.getDataDescriptor() == null) {
            throw new ConstraintException("TargetDescriptor is empty");
        }
        if (!this.dataDescriptor.getFormatType().equals(other.getDataDescriptor().getFormatType())) {
            throw new ConstraintException(String.format("Incompatible formats (%s, %s)",
                                                        this.dataDescriptor.getFormatType().friendlyName(),
                                                        other.getDataDescriptor().getFormatType().friendlyName()));
        }
        if (this.constraints != null && !this.constraints.isEmpty()) {
            for (String c : this.constraints) {
                IOConstraint constraint = ConstraintFactory.create(c);
                if (constraint != null && !constraint.check(this.dataDescriptor, other.getDataDescriptor())) {
                    throw new ConstraintException(String.format("Constraint [%s] not satisfied",
                                                                constraint.getClass().getAnnotation(Constraint.class).name()));
                }
            }

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SourceDescriptor that = (SourceDescriptor) o;
        return Objects.equals(parentId, that.parentId) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentId, name);
    }

    @Override
    public SourceDescriptor clone() {
        SourceDescriptor clone = new SourceDescriptor();
        clone.name = this.name;
        if (this.dataDescriptor != null) {
            clone.dataDescriptor = new DataDescriptor();
            clone.dataDescriptor.setSensorType(this.dataDescriptor.getSensorType());
            clone.dataDescriptor.setLocation(this.dataDescriptor.getLocation());
            clone.dataDescriptor.setCrs(this.dataDescriptor.getCrs());
            clone.dataDescriptor.setFormatType(this.dataDescriptor.getFormatType());
            clone.dataDescriptor.setDimension(this.dataDescriptor.getDimension());
            clone.dataDescriptor.setFormatName(this.dataDescriptor.getFormatName());
        }
        if (this.constraints != null) {
            clone.constraints = new ArrayList<>(this.constraints);
        }
        clone.cardinality = this.cardinality;
        clone.referencedSourceDescriptorId = this.referencedSourceDescriptorId;
        return clone;
    }
}
