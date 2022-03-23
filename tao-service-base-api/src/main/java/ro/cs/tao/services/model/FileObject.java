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
import java.util.TreeMap;

public class FileObject {
    private String protocol;
    private String relativePath;
    private boolean folder;
    private long size;
    private String productName;
    private String displayName;
    private Map<String, String> attributes;

    public FileObject() {
    }

    public FileObject(String protocol, String relativePath, boolean isFolder, long size) {
        this(protocol, relativePath, isFolder, size, relativePath);
    }

    public FileObject(String protocol, String relativePath, boolean folder, long size, String displayName) {
        this.protocol = protocol;
        this.relativePath = relativePath;
        this.folder = folder;
        this.size = this.folder ? 0 : size;
        this.displayName = displayName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRelativePath() { return relativePath; }

    public boolean isFolder() { return folder; }

    public long getSize() { return size; }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getProductName() { return productName; }

    public void setProductName(String productName) { this.productName = productName; }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getAttributes() { return attributes; }

    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public void addAttribute(String key, String value) {
        if (this.attributes == null) {
            this.attributes = new TreeMap<>();
        }
        this.attributes.put(key, value);
    }

    @Override
    public String toString() {
        return "FileObject{" +
                "relativePath='" + relativePath + '\'' +
                '}';
    }
}
