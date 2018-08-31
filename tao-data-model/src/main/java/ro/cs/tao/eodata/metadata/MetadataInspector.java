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
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
        private LocalDateTime aquisitionDate;
        private Long size;
        private int width;
        private int height;

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

        public LocalDateTime getAquisitionDate() { return aquisitionDate; }
        public void setAquisitionDate(LocalDateTime aquisitionDate) { this.aquisitionDate = aquisitionDate; }

        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }

        public EOProduct toProductDescriptor(Path productPath) throws URISyntaxException, IOException {
            EOProduct product = new EOProduct();
            String name = FileUtils.getFilenameWithoutExtension(productPath.toFile());
            product.setAcquisitionDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            product.setId(name);
            product.setName(name);
            product.setProductType(this.productType);
            product.setSensorType(SensorType.UNKNOWN);
            product.setFormatType(DataFormat.RASTER);
            product.setPixelType(this.pixelType);
            product.setGeometry(this.footprint);
            product.setCrs(this.crs);
            product.setWidth(this.width);
            product.setHeight(this.height);
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
            return product;
        }

        @Override
        public String toString() {
            return "Metadata{" +
                    "footprint='" + footprint + '\'' +
                    ", crs='" + crs + '\'' +
                    ", entryPoint=" + entryPoint +
                    ", pixelType=" + pixelType +
                    ", productType='" + productType + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }
}
