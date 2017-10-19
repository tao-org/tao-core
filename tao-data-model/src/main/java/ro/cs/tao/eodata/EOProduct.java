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
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.serialization.CRSAdapter;
import ro.cs.tao.serialization.GeometryAdapter;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "eoProduct")
public class EOProduct implements Serializable {
    private SensorType sensorType;
    private Date acquisitionDate;
    private PixelType pixelType;
    private String productType;
    private int width;
    private int height;
    private String id;
    private String name;
    private Geometry geometry;
    //private Map<String, Attribute> attributesMap = new HashMap<>();
    private List<Attribute> attributes;
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

    /*@XmlElementWrapper(name = "attributes")
    public List<Attribute> getAttributes() {
        return this.attributesMap != null ?
                new ArrayList<>(this.attributesMap.values()) : new ArrayList<>();
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributesMap = attributes != null ?
                attributes.stream().collect(Collectors.toMap(Attribute::getName, Function.identity())) :
                null;
    }*/

    @XmlElementWrapper(name = "attributes")
    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }


    /*public void addAttribute(String name, String value) {
        if (this.attributesMap == null) {
            this.attributesMap = new HashMap<>();
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
        this.attributesMap.put(name, attr);
    }*/

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

    /*public void addAttribute(Attribute attribute) {
        if (this.attributesMap == null) {
            this.attributesMap = new HashMap<>();
        }
        this.attributesMap.put(attribute.getName(), attribute);
    }*/

    public void addAttribute(Attribute attribute) {
        if (this.attributes == null) {
            this.attributes = new ArrayList<>();
        }
        this.attributes.add(attribute);
    }

    /*public String getAttributeValue(String name) {
        Attribute attribute = null;
        if (this.attributesMap != null) {
            attribute = this.attributesMap.get(name);
        }
        return attribute != null ? attribute.getValue() : null;
    }*/

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

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public Date getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public PixelType getPixelType() {
        return pixelType;
    }

    public void setPixelType(PixelType pixelType) {
        this.pixelType = pixelType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getProductType() { return productType; }

    public void setProductType(String value) { this.productType = value; }
}
