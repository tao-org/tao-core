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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.serialization.CRSAdapter;
import ro.cs.tao.serialization.GeometryAdapter;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public abstract class EOData {

    private String id;
    private String name;
    private DataFormat formatType;
    private Geometry geometry;
    private Map<String, Attribute> attributes;
    private CoordinateReferenceSystem crs;
    private URI location;

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

    /**@XmlTransient
    @JsonIgnore
    public Geometry getPolygon() {
        return this.geometry;
    }**/

    public void setGeometry(String geometryAsText) {
        try {
            this.geometry = new GeometryAdapter().marshal(geometryAsText);
        } catch (Exception ignored) { }
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
            } else {
                this.attributes.clear();
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
        if (value != null) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
        }
        final String val = value;
        Attribute attr = new Attribute();
        attr.setName(name);
        attr.setValue(val);
        this.attributes.put(name, attr);
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

    /**
     *
     * @return map of attributes
     */
    public Map<String, String> getAttributesMap() {
        if (this.attributes == null)
        {
            return null;
        }

        Map<String, String> attributesMap = attributes.values().stream().collect(Collectors.toMap(Attribute::getName, Attribute::getValue));
        // remove entries having null values
        attributesMap.values().removeIf(Objects::isNull);

        attributesMap = attributesMap.entrySet()
          .stream()
          .filter(e -> e.getValue() != null && !e.getValue().equals("null"))
          .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        return attributesMap;
    }

    public void setAttributesMap(Map<String, String> attributes) {
        if (attributes != null) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }
            Map<String, Attribute> newAttributes = new HashMap<>();
            attributes.entrySet().stream().forEach(e -> newAttributes.put(e.getKey(),
              new Attribute() {{
                  setName(e.getKey());
                  setValue(e.getValue());
              }}));
            this.attributes.putAll(newAttributes.entrySet()
              .stream()
              .filter(e -> e.getValue().getValue() != null && !"null".equals(e.getValue().getValue()))
              .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())));
        } else {
            this.attributes = new HashMap<>();
        }
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

    public URI getLocation() { return location; }

    public void setLocation(String value) throws URISyntaxException { this.location = new URI(value); }
}
