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
package ro.cs.tao.topology;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A service represents a mandatory software component that should be on a topology node.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "service")
public class ServiceDescription {
    private Integer id;
    private String name;
    private String version;
    private String description;

    public ServiceDescription() { }

    public ServiceDescription(String name, String version, String description) {
        this.name = name;
        this.version = version;
        this.description = description;
    }

    @XmlTransient
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "version")
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    @XmlElement(name = "description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}
