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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.enums.DataFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor of an output of a component
 * @author Cosmin Cara
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@XmlRootElement(name = "output")
public class TargetDescriptor extends Identifiable {
    private static final String DEFAULT_NAME = "Output";
    private String parentId;
    private DataFormat dataType;
    private EOData data;
    private List<String> constraints;

    public TargetDescriptor() {
        this(DEFAULT_NAME);
    }

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
    public String defaultName() { return DEFAULT_NAME; }
    /**
     * Returns the data associated to this instance.
     */
    @XmlTransient
    @JsonIgnore
    public EOData getData() {
        return data;
    }
    /**
     * Sets the data associated to this instance.
     *
     * @param data  The data to be associated with this instance.
     */
    public void setData(EOData data) {
        this.data = data;
    }

    @XmlElement(name = "type")
    public DataFormat getDataType() { return dataType; }
    public void setDataType(DataFormat dataType) { this.dataType = dataType; }

    /**
     * Returns a list of constraints to be satisfied by the data of this instance.
     */
    @XmlElementWrapper(name = "constraints")
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
}
