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

package ro.cs.tao.eodata;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.eodata.serialization.CRSAdapter;
import ro.cs.tao.eodata.serialization.GeometryAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public abstract class EOData {

    private String id;
    private String name;
    private Format type;
    private Geometry geometry;
    private Map<String, Attribute> attributes;
    private CoordinateReferenceSystem crs;
    private URI location;

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "type")
    public Format getType() { return type; }

    public void setType(Format type) { this.type = type; }

    @XmlElement(name = "geometry")
    @XmlJavaTypeAdapter(GeometryAdapter.class)
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @XmlElementWrapper(name = "attributes")
    public Attribute[] getAttributes() {
        return attributes != null ?
                attributes.values().toArray(new Attribute[attributes.size()]) :
                null;
    }

    public void setAttributes(Attribute[] attributes) {
        if (attributes != null) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }
            for (Attribute attribute : attributes) {
                this.attributes.put(attribute.getName(), attribute);
            }
        } else {
            this.attributes = new HashMap<>();
        }
    }

    public void addAttribute(String name, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(name, new Attribute() {{
            setName(name);
            setValue(value);
        }});
    }

    public void addAttribute(Attribute attribute) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(attribute.getName(), attribute);
    }

    public String getAttributeValue(String name) {
        Attribute attribute = null;
        if (this.attributes != null) {
            attribute = this.attributes.get(name);
        }
        return attribute != null ? attribute.getValue() : null;
    }

    @XmlElement(name = "crs")
    @XmlJavaTypeAdapter(CRSAdapter.class)
    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    public void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    @XmlElement(name = "location")
    public URI getLocation() { return location; }

    public void setLocation(String value) throws URISyntaxException { this.location = new URI(value); }
}
