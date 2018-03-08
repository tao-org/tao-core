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

import org.apache.commons.lang.StringUtils;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "eoData")
public class EOProduct extends EOData implements Serializable {
    private SensorType sensorType;
    private Date acquisitionDate;
    private PixelType pixelType;
    private String productType;
    private int width;
    private int height;
    private long approximateSize;

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

    public void setProductType(String value) {
        if (value != null) {
            this.productType = StringUtils.capitalize(value.replace("-", ""));
        }
    }

    public long getApproximateSize() {
        return approximateSize;
    }

    public void setApproximateSize(long approximateSize) {
        this.approximateSize = approximateSize;
    }
}
