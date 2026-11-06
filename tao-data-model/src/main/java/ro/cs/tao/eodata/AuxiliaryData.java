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

package ro.cs.tao.eodata;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import ro.cs.tao.component.StringIdentifiable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents auxiliary data with associated metadata such as location, description,
 * user information, and timestamps.
 *
 * This class is designed to store supplemental data that can be identified using
 * a string-based ID inherited from the {@link StringIdentifiable} class.
 *
 * The following properties are exposed:
 * - Location: Represents the geographical or logical location associated with the data.
 * - Description: Provides additional context or metadata about the data.
 * - User ID: Identifies the user associated with the data.
 * - Created: Tracks when the data was initially created.
 * - Modified: Tracks when the data was last updated.
 *
 * JAXB annotations are used for XML serialization and deserialization.
 */
@XmlRootElement(name = "auxData")
public class AuxiliaryData extends StringIdentifiable {
    private String location;
    private String description;
    private String userId;
    private LocalDateTime created;
    private LocalDateTime modified;

    @XmlElement(name = "location")
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @XmlElement(name = "Description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @XmlTransient
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @XmlElement(name = "created")
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    @XmlTransient
    public LocalDateTime getModified() { return modified; }
    public void setModified(LocalDateTime modified) { this.modified = modified; }

    public Map<String, String> toAttributeMap() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("description", description != null ? description : "n/a");
        attributes.put("user", userId != null ? userId : "n/a");
        attributes.put("created", created != null ? created.format(DateTimeFormatter.ISO_DATE_TIME) : "n/a");
        return attributes;
    }
}

