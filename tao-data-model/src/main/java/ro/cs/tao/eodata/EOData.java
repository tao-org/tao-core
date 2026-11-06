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

import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlTransient;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.ProductStatus;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.serialization.CRSAdapter;
import ro.cs.tao.serialization.GeometryAdapter;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

/**
 * The EOData class serves as an abstract representation of Earth Observation data and metadata.
 * It provides a structure for spatial data, attributes, and related information, ensuring
 * compatibility with serialization and identification functionalities.
 *
 * EOData is designed to support extensible models for Earth Observation data, allowing
 * applications to manage, query, and process geospatial datasets efficiently.
 *
 * Main responsibilities of this class include:
 * - Defining the spatial data (geometry) and its related attributes.
 * - Managing Coordinate Reference Systems (CRS) and spatial representation.
 * - Handling metadata and extended properties specific to various datasets.
 * - Supporting data identification via unique string identifiers.
 * - Enabling serialization for data persistence and retrieval.
 *
 * This class provides getter and setter methods for accessing and updating its state,
 * along with utility methods for managing attributes and file references.
 * Subclasses of EOData are expected to extend and specialize the behavior as needed.
 *
 * Inherits from:
 * - StringIdentifiable: Provides unique string-based identification for objects.
 * - Serializable: Ensures the object can be serialized for data storage or transfer.
 *
 * @author Cosmin Cara
 */
public abstract class EOData extends StringIdentifiable implements Serializable {

    private String name;
    private DataFormat formatType;
    private Geometry geometry;
    private List<Attribute> attributes;
    private String crs;
    private URI location;
    private String entryPoint;
    private Visibility visibility;
    private ProductStatus productStatus;
    @XmlTransient
    private Set<String> files;

    public EOData() { super(); }

    //region Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /*public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }*/

    public DataFormat getFormatType() { return formatType; }
    public void setFormatType(DataFormat type) { this.formatType = type; }

    public String getGeometry() {
        try {
            return new GeometryAdapter().unmarshal(geometry);
        } catch (Exception e) {
            return null;
        }
    }

    public Geometry geometryObject() { return this.geometry; }

    public void setGeometry(String geometryAsText) {
        try {
            this.geometry = new GeometryAdapter().marshal(geometryAsText);
            if (this.geometry instanceof MultiPolygon || this.geometry.getNumGeometries() > 1) {
                this.geometry = this.geometry.getEnvelope();
            }
        } catch (Exception ignored) { }
    }

    @XmlElementWrapper(name = "attributes")
    public List<Attribute> getAttributes() {
        return this.attributes;
    }
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String name, String value) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<>();
        }
        if (value != null) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
        }
        final String val = value;
        Attribute attr = new Attribute();
        attr.setName(name);
        attr.setValue(val);
        Attribute existing = this.attributes.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
        if (existing == null) {
            this.attributes.add(attr);
        } else {
            existing.setValue(value);
        }
    }

    public void addAttribute(Attribute attribute) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<>();
        }
        this.attributes.add(attribute);
    }

    public String getAttributeValue(String name) {
        Attribute attribute = null;
        if (this.attributes != null) {
            for(Attribute attr : this.attributes) {
                if(attr.getName().equalsIgnoreCase(name)) {
                    attribute = attr;
                    break;
                }
            }
        }
        return attribute != null ? attribute.getValue() : null;
    }

    public void removeAttribute(String name) {
        if (this.attributes != null) {
            this.attributes.removeIf(a -> a.getName().equals(name));
        }
    }

    public List<Attribute> filterAttributes(String name) {
        List<Attribute> filtered = null;
        if (this.attributes != null) {
            filtered = new ArrayList<>();
            for(Attribute attr : this.attributes) {
                if(attr.getName().contains(name)) {
                    filtered.add(attr);
                }
            }
        }
        return filtered;
    }

    public List<Attribute> excludeAttributes(String name) {
        List<Attribute> filtered = null;
        if (this.attributes != null) {
            filtered = new ArrayList<>();
            for(Attribute attr : this.attributes) {
                if(!attr.getName().contains(name)) {
                    filtered.add(attr);
                }
            }
        }
        return filtered;
    }

    public String getCrs() {
        return this.crs;
    }
    public void setCrs(String crsCode) {
        try {
            new CRSAdapter().marshal(crsCode);
            this.crs = crsCode;
        } catch (Exception ignored) {}
    }

    public String getLocation() { return location != null ? location.toString() : null; }
    public void setLocation(String value) throws URISyntaxException { this.location = new URI(value); }

    public String getEntryPoint() { return entryPoint; }
    public void setEntryPoint(String entryPoint) { this.entryPoint = entryPoint; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public ProductStatus getProductStatus() { return productStatus; }
    public void setProductStatus(ProductStatus productStatus) { this.productStatus = productStatus; }

    //endregion
    public Map<String, String> toAttributeMap() {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("id", safeValue(id));
        attributes.put("formatType", formatType != null ? formatType.name() : "n/a");
        attributes.put("geometry", safeValue(geometry));
        if (crs != null) {
            try {
                CoordinateReferenceSystem referenceSystem = new CRSAdapter().marshal(crs);
                ReferenceIdentifier systemName = referenceSystem.getName();
                attributes.put("crs", systemName.getCodeSpace() + ":" + systemName.getCode());
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).warning(e.getMessage());
            }
        } else {
            attributes.put("crs", "n/a");
        }
        if (this.attributes != null) {
            for (Attribute attribute : this.attributes) {
                attributes.put(attribute.getName(), attribute.getValue());
            }
        }
        return attributes;
    }

    @XmlTransient
    public Set<String> getFiles() {
        return files;
    }

    public void addFile(String file)  throws URISyntaxException {
        if (this.files == null) {
            this.files = new LinkedHashSet<>();
        }
        this.files.add(file);
    }

    String safeValue(Object value) {
        return value != null ? value.toString() : "n/a";
    }
}

