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
package ro.cs.tao.workflow;

import ro.cs.tao.component.LongIdentifiable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class GraphObject extends LongIdentifiable {

    protected String name;
    protected LocalDateTime created;
    private List<ParameterValue> customValues;

    @Override
    public Long defaultId() { return 0L; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "created")
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    @XmlElementWrapper(name = "customValues")
    public List<ParameterValue> getCustomValues() { return customValues; }
    public void setCustomValues(List<ParameterValue> customValues) { this.customValues = customValues; }
    public void addCustomValue(String name, String value) {
        ParameterValue parameterValue = new ParameterValue();
        parameterValue.setParameterName(name);
        parameterValue.setParameterValue(value);
        if (this.customValues == null) {
            this.customValues = new ArrayList<>();
        }
        this.customValues.add(parameterValue);
    }
}
