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

package ro.cs.tao.services.model;

import java.util.Map;

public class FileObject {
    private String relativePath;
    private boolean isFolder;
    private long size;
    private String productName;
    private Map<String, String> attributes;

    public FileObject(String relativePath, boolean isFolder, long size) {
        this.relativePath = relativePath;
        this.isFolder = isFolder;
        this.size = this.isFolder ? 0 : size;
    }

    public String getRelativePath() { return relativePath; }

    public boolean isFolder() { return isFolder; }

    public long getSize() { return size; }

    public String getProductName() { return productName; }

    public void setProductName(String productName) { this.productName = productName; }

    public Map<String, String> getAttributes() { return attributes; }

    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
}
