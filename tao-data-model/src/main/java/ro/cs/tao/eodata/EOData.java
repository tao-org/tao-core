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
package ro.cs.tao.eodata;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.serialization.CRSAdapter;
import ro.cs.tao.serialization.GeometryAdapter;

import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public abstract class EOData implements Serializable {

    private String id;
    private String name;
    private DataFormat formatType;
    private Geometry geometry;
    private List<Attribute> attributes;
    private CoordinateReferenceSystem crs;
    private URI location;
    private URI entryPoint;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataFormat getFormatType() { return formatType; }

    public void setFormatType(DataFormat type) { this.formatType = type; }

    public String getGeometry() {
        try {
            return new GeometryAdapter().unmarshal(geometry);
        } catch (Exception e) {
            return null;
        }
    }

    public void setGeometry(String geometryAsText) {
        try {
            this.geometry = new GeometryAdapter().marshal(geometryAsText);
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
        this.attributes.add(attr);
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
            for(Attribute attr : this.attributes)
            {
                if(attr.getName().equals(name))
                {
                    attribute = attr;
                    break;
                }
            }
        }
        return attribute != null ? attribute.getValue() : null;
    }

    public String getCrs() {
        try {
            return new CRSAdapter().unmarshal(this.crs);
        } catch (Exception e) {
            return null;
        }
    }

    public void setCrs(String crsCode) {
        try {
            this.crs = new CRSAdapter().marshal(crsCode);
        } catch (Exception ignored) { }
    }

    public String getLocation() { return location != null ? location.toString() : null; }

    public void setLocation(String value) throws URISyntaxException { this.location = new URI(value); }

    public String getEntryPoint() { return entryPoint != null ? entryPoint.toString() : null; }

    public void setEntryPoint(String entryPoint) throws URISyntaxException { this.entryPoint = new URI(entryPoint); }

}
