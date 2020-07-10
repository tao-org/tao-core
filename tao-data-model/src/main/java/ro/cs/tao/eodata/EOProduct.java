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

import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.eodata.enums.Visibility;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
    private Date processingDate;
    private URI quicklookLocation;
    private Set<String> refs;
    private String satelliteName;

    //region Getters and setters
    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public Date getAcquisitionDate() { return acquisitionDate; }

    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public Date getProcessingDate() { return processingDate; }

    public void setProcessingDate(Date processingDate) { this.processingDate = processingDate; }

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

    public String getQuicklookLocation() {
        return quicklookLocation != null ? quicklookLocation.toString() : null;
    }

    public void setQuicklookLocation(String location) throws URISyntaxException {
        if (location != null) {
            this.quicklookLocation = new URI(location);
        }
    }

    public String getSatelliteName() { return satelliteName; }

    public void setSatelliteName(String satelliteName) { this.satelliteName = satelliteName; }

    public long getApproximateSize() {
        return approximateSize;
    }

    public void setApproximateSize(long approximateSize) {
        this.approximateSize = approximateSize;
    }

    public Set<String> getRefs() { return refs; }
    public void setRefs(Set<String> refs) { this.refs = refs; }

    public void addReference(String userName) {
        if (this.refs == null) {
            this.refs = new HashSet<>();
        }
        this.refs.add(userName);
    }

    public void removeReference(String userName) {
        if (this.refs != null) {
            this.refs.remove(userName);
        }
    }

    //endregion
    //region equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EOProduct product = (EOProduct) o;
        return Objects.equals(getId(), product.getId()) &&
                Objects.equals(getName(), product.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
    //endregion
    @Override
    public Map<String, String> toAttributeMap() {
        Map<String, String> attributes = super.toAttributeMap();
        attributes.put("name", getName());
        attributes.put("sensorType", sensorType != null ? sensorType.name() : "n/a");
        attributes.put("pixelType", pixelType != null ? pixelType.name() : "n/a");
        attributes.put("productType", safeValue(productType));
        attributes.put("acquisitionDate", safeValue(acquisitionDate));
        attributes.put("width", safeValue(width));
        attributes.put("height", safeValue(height));
        attributes.put("visibility", (getVisibility() != null ? getVisibility() : Visibility.PRIVATE).name());
        return attributes;
    }
}
