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
package ro.cs.tao.workflow;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Represents a parameter with a name and value.
 * This class is designed to store parameter information as name-value pairs
 * and includes support for XML serialization using JAXB annotations.
 *
 * Instances of this class are immutable once created to ensure thread safety
 * and consistent behavior across different usage scenarios.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "parameter")
public class ParameterValue implements Serializable {
    private String parameterName;
    private String parameterValue;

    public ParameterValue() { }

    public ParameterValue(String parameterName, String parameterValue) {
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    @XmlElement(name = "name")
    public String getParameterName() { return parameterName; }
    public void setParameterName(String parameterName) { this.parameterName = parameterName; }

    @XmlElement(name = "value")
    public String getParameterValue() { return parameterValue; }
    public void setParameterValue(String parameterValue) { this.parameterValue = parameterValue; }
}

