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
package ro.cs.tao.component;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.serialization.CRSAdapter;
import ro.cs.tao.serialization.GeometryAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.net.URI;
import java.nio.file.Paths;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataDescriptor")
public class DataDescriptor {
    private DataFormat formatType;
    private Geometry geometry;
    private CoordinateReferenceSystem crs;
    private SensorType sensorType;
    private Dimension dimension;
    private String location;

    public DataFormat getFormatType() { return formatType; }
    public void setFormatType(DataFormat formatType) { this.formatType = formatType; }

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

    public SensorType getSensorType() { return sensorType; }
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    public Dimension getDimension() { return dimension; }
    public void setDimension(Dimension dimension) { this.dimension = dimension; }

    public String getLocation() { return this.location; }
    public void setLocation(String value) {
        if (value != null) {
            try {
                // if the value is a URL
                URI.create(value);
            } catch (Exception e) {
                // else it should be a relative path
                if (Paths.get(value).isAbsolute()) {
                    throw new IllegalArgumentException(DataDescriptor.class.getSimpleName() + ": location must be relative");
                }
            }
        }
        this.location = value;
    }
}
