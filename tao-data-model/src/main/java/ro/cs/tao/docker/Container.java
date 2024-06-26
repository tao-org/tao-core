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
package ro.cs.tao.docker;

import ro.cs.tao.component.StringIdentifiable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Descriptor for a Docker container
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "container")
public class Container extends StringIdentifiable {

    private String name;
    private String description;
    private ContainerType type;
    private String tag;
    private String applicationPath;
    private String logo;
    private List<Application> applications;
    private Set<String> format;
    private String commonParameters;
    private String formatNameParameter;
    private String ownerId;
    private ContainerVisibility visibility;

    @Override
    public String defaultId() { return UUID.randomUUID().toString();}

    @XmlElement(name = "id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @XmlElement(name = "type")
    public ContainerType getType() { return type; }
    public void setType(ContainerType type) { this.type = type; }

    @XmlElement(name = "tag")
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    @XmlElement(name = "applicationPath")
    public String getApplicationPath() { return applicationPath; }
    public void setApplicationPath(String value) { applicationPath = value; }

    @XmlElementWrapper(name = "applications")
    public List<Application> getApplications() {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        return applications;
    }
    public void setApplications(List<Application> applications) { this.applications = applications; }
    public void addApplication(Application application) {
        getApplications().add(application);
    }

    @XmlElementWrapper(name = "formats")
    public Set<String> getFormat() {
        if (format == null) {
            format = new LinkedHashSet<>();
        }
        return format;
    }
    public void setFormat(Set<String> format) { this.format = format; }
    public void addFormat(String formatName) { getFormat().add(formatName); }

    public String getCommonParameters() { return commonParameters; }
    public void setCommonParameters(String commonParameters) { this.commonParameters = commonParameters; }

    public String getFormatNameParameter() { return formatNameParameter; }
    public void setFormatNameParameter(String formatNameParameter) { this.formatNameParameter = formatNameParameter; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public ContainerVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ContainerVisibility containerVisibility) {
        this.visibility = containerVisibility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(id, container.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
