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

public interface MetadataInspector {
    Metadata getMetadata(Path productPath) throws IOException;

    class Metadata {
        private String footprint;
        private String crs;
        private URI entryPoint;
        private PixelType pixelType;
        private String productType;
        private int width;
        private int height;

        public String getFootprint() { return footprint; }
        public void setFootprint(String footprint) { this.footprint = footprint; }

        public String getCrs() { return crs; }
        public void setCrs(String crs) { this.crs = crs; }

        public URI getEntryPoint() { return entryPoint; }
        public void setEntryPoint(URI entryPoint) { this.entryPoint = entryPoint; }

        public PixelType getPixelType() { return pixelType; }
        public void setPixelType(PixelType pixelType) { this.pixelType = pixelType; }

        public String getProductType() { return productType; }
        public void setProductType(String productType) { this.productType = productType; }

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
            product.setLocation(productUri.toString());
            product.setApproximateSize(Files.size(productPath));
            if (this.entryPoint != null && !this.entryPoint.equals(productUri)) {
                product.setEntryPoint(this.entryPoint.toString());
            }
            return product;
        }
    }
}