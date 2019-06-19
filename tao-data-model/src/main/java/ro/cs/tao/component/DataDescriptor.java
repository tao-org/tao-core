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
package ro.cs.tao.component;

import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.serialization.CRSAdapter;
import ro.cs.tao.serialization.GeometryAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Models the data of a component port (either source or target).
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataDescriptor")
public class DataDescriptor {
    private DataFormat formatType;
    private String formatName;
    private Geometry geometry;
    private CoordinateReferenceSystem crs;
    private SensorType sensorType;
    private Dimension dimension;
    private String location;

    /**
     * Returns the format type of the component port.
     * @see DataFormat for possible values.
     */
    public DataFormat getFormatType() { return formatType; }

    /**
     * Sets the format type of the component port.
     * @param formatType    The format type.
     * @see DataFormat for possible values.
     */
    public void setFormatType(DataFormat formatType) { this.formatType = formatType; }

    /**
     * Gets the format name of the component output. This is dependent on the application implementation.
     */
    public String getFormatName() { return formatName; }
    /**
     * Sets the format name of the component output
     */
    public void setFormatName(String formatName) { this.formatName = formatName; }

    /**
     * Returns the geometry (WKT) of the component port, if specified.
     */
    public String getGeometry() {
        try {
            return new GeometryAdapter().unmarshal(geometry);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the geometry for the component port.
     * @param geometryAsText    The WKT geometry.
     */
    public void setGeometry(String geometryAsText) {
        try {
            this.geometry = new GeometryAdapter().marshal(geometryAsText);
        } catch (Exception ignored) { }
    }

    /**
     * Returns the CRS (coordinate reference system) of the component port, if defined.
     * The CRS is returned as <code>EPSG:nnnnn</code>.
     */
    public String getCrs() {
        try {
            return new CRSAdapter().unmarshal(this.crs);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the CRS (coordinate reference system) of the component port
     * @param crsCode   The CRS code (in the form <code>EPSG:nnnn</code>).
     */
    public void setCrs(String crsCode) {
        try {
            this.crs = new CRSAdapter().marshal(crsCode);
        } catch (Exception ignored) { }
    }

    /**
     * Returns the sensor type of the component port.
     * @see SensorType for possible values.
     */
    public SensorType getSensorType() { return sensorType; }

    /**
     * Sets the sensor type of the component port.
     * @param sensorType    The sensor type.
     * @see SensorType for possible values.
     */
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    /**
     * Returns the dimensions (width, height) of the component port.
     */
    public Dimension getDimension() { return dimension; }

    /**
     * Sets the dimensions (width, height) of the component port.
     * @param dimension The dimension object.
     */
    public void setDimension(Dimension dimension) { this.dimension = dimension; }

    /**
     * Returns the location of the product described by the component port.
     */
    public String getLocation() { return this.location; }

    /**
     * Sets the location of the product described by the component port.
     * @param value The location, either as an URL or as a relative file system path. Absolute paths are not supported.
     */
    public void setLocation(String value) {
        if (value != null) {
            try {
                // if the value is a URL
                URI.create(value);
            } catch (Exception e) {
                try {
                    Path path = Paths.get(value);
                    // else it should be a relative path
                    if (path.isAbsolute()) {
                        throw new IllegalArgumentException(DataDescriptor.class.getSimpleName() + ": location must be relative");
                    }
                } catch (Exception other) {
                    Logger.getLogger(getClass().getName()).warning(String.format("Value '%s' should represent a path, but appears not to",
                                                                                 value));
                }
            }
        }
        this.location = value;
    }
}
