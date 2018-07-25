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
package ro.cs.tao.docker;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Descriptor for a Docker container
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "container")
public class Container {

    private String id;
    private String name;
    private String tag;
    private String applicationPath;
    private String logo;
    private List<Application> applications;

    @XmlElement(name = "id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

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
