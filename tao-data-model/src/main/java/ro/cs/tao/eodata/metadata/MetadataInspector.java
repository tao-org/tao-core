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

package ro.cs.tao.eodata.metadata;

import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.OrbitDirection;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Interface to be implemented by all simple metadata inspectors.
 *
 * @author Cosmin Cara
 */
public interface MetadataInspector {

    /**
     * Returns the "suitability" qualification of this inspector for the given product path.
     * @param productPath   The product path. If the product is complex, it is the wrapping folder
     * @return  <code>DecodeStatus.INTENDED</code> if this inspector is made specially for the kind of product;
     *          <code>DecodeStatus.SUITABLE</code> if this inspector is not made specially for this kind of product,
     *          but may extract some information from it;
     *          <code>DecodeStatus.UNABLE</code> if this inspector cannot extract any information for this kind of product.
     */
    DecodeStatus decodeQualification(Path productPath);

    /**
     * Parses the metadata for the given product path
     * @param productPath   The path of the product. If the product is complex, it is the wrapping folder.
     */
    Metadata getMetadata(Path productPath) throws IOException;

    class Metadata {
        private String productId;
        private String footprint;
        private String crs;
        private String entryPoint;
        private PixelType pixelType;
        private String productType;
        private SensorType sensorType;
        private LocalDateTime aquisitionDate;
        private Long size;
        private int width;
        private int height;
        private OrbitDirection orbitDirection;
        private Set<String> controlSums;
        private Map<String, Double> statistics;
        private Map<String, String> additionalAttributes;
        private int[] histogram;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getFootprint() { return footprint; }
        public void setFootprint(String footprint) { this.footprint = footprint; }

        public String getCrs() { return crs; }
        public void setCrs(String crs) { this.crs = crs; }

        public String getEntryPoint() { return entryPoint; }
        public void setEntryPoint(String entryPoint) { this.entryPoint = entryPoint; }

        public PixelType getPixelType() { return pixelType; }
        public void setPixelType(PixelType pixelType) { this.pixelType = pixelType; }

        public String getProductType() { return productType; }
        public void setProductType(String productType) { this.productType = productType; }

        public SensorType getSensorType() { return sensorType; }
        public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

        public LocalDateTime getAquisitionDate() { return aquisitionDate; }
        public void setAquisitionDate(LocalDateTime aquisitionDate) { this.aquisitionDate = aquisitionDate; }

        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }

        public OrbitDirection getOrbitDirection() { return orbitDirection; }
        public void setOrbitDirection(OrbitDirection orbitDirection) { this.orbitDirection = orbitDirection; }

        public Set<String> getControlSums() {
            if (controlSums == null) {
                controlSums = new HashSet<>();
            }
            return controlSums;
        }
        public void setControlSums(Set<String> controlSums) { this.controlSums = controlSums; }
        public void addControlSum(String sum) {
            getControlSums().add(sum);
        }

        public Map<String, Double> getStatistics() {
            if (statistics == null) {
                statistics = new HashMap<>();
            }
            return statistics;
        }
        public void setStatistics(Map<String, Double> statistics) { this.statistics = statistics; }
        public void addStatistic(String name, double value) { getStatistics().put(name, value); }

        public Map<String, String> getAdditionalAttributes() {
            if (additionalAttributes == null) {
                additionalAttributes = new HashMap<>();
            }
            return additionalAttributes;
        }
        public void setAdditionalAttributes(Map<String, String> additionalAttributes) { this.additionalAttributes = additionalAttributes; }
        public void addAttribute(String name, String value) { getAdditionalAttributes().put(name, value); }

        public int[] getHistogram() { return histogram; }
        public void setHistogram(int[] histogram) { this.histogram = histogram; }

        public EOProduct toProductDescriptor(Path productPath) throws URISyntaxException, IOException {
            EOProduct product = new EOProduct();
            String name = FileUtilities.getFilenameWithoutExtension(productPath.toFile());
            if (this.aquisitionDate != null) {
                product.setAcquisitionDate(Date.from(this.aquisitionDate.atZone(ZoneId.systemDefault()).toInstant()));
            }
            product.setId(name);
            product.setName(name);
            product.setProductType(this.productType);
            product.setSensorType(this.sensorType != null ? this.sensorType : SensorType.UNKNOWN);
            product.setFormatType(DataFormat.RASTER);
            product.setPixelType(this.pixelType);
            product.setGeometry(this.footprint);
            product.setCrs(this.crs);
            product.setWidth(this.width);
            product.setHeight(this.height);
            if (this.orbitDirection != null) {
                product.addAttribute("orbitdirection", this.orbitDirection.name());
            }
            URI productUri = productPath.toUri();
            if (Files.isDirectory(productPath)) {
                product.setLocation(productUri.toString());
            } else {
                product.setLocation(productPath.getParent().toUri().toString());
            }
            //product.setLocation(productUri.toString());
            product.setApproximateSize(Files.size(productPath));
            if (this.entryPoint != null) {
                if (this.entryPoint.equals(productPath.toString())) {
                    product.setEntryPoint(productPath.getFileName().toString());
                } else {
                    product.setEntryPoint(this.entryPoint);
                }
            }
            if (this.controlSums != null && this.controlSums.size() > 0) {
                product.addAttribute("controlSum", String.join(",", this.controlSums));
            }
            return product;
        }

        @Override
        public String toString() {
            return "Metadata{" +
                    "productId='" + productId + '\'' +
                    ", footprint='" + footprint + '\'' +
                    ", crs='" + crs + '\'' +
                    ", entryPoint='" + entryPoint + '\'' +
                    ", pixelType=" + pixelType +
                    ", productType='" + productType + '\'' +
                    ", sensorType=" + sensorType +
                    ", aquisitionDate=" + aquisitionDate +
                    ", size=" + size +
                    ", width=" + width +
                    ", height=" + height +
                    ", orbitDirection=" + orbitDirection +
                    ", controlSums=" + controlSums +
                    ", statistics=" + statistics +
                    '}';
        }
    }
}
