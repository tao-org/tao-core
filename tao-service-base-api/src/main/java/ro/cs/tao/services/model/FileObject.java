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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * Structure for holding information about a storage item.
 *
 * @author Cosmin Cara
 */
public class FileObject {
    private String protocol;
    private String relativePath;
    private boolean folder;
    private long size;
    private String productName;
    private String displayName;
    private LocalDateTime lastModified;
    private Map<String, String> attributes;

    public FileObject() {
    }

    public FileObject(String protocol, String relativePath, boolean isFolder, long size) {
        this.protocol = protocol;
        this.relativePath = relativePath;
        this.folder = isFolder;
        this.size = this.folder ? 0 : size;
        final int idx = relativePath.lastIndexOf('/', relativePath.length() - 2);
        this.displayName = idx > 0
                ? relativePath.substring(idx + 1)
                : relativePath.endsWith("/")
                    ? relativePath.substring(relativePath.startsWith("/") ? 1 : 0, relativePath.length() - 1)
                    : relativePath;
    }

    public FileObject(String protocol, String relativePath, boolean folder, long size, String displayName) {
        this.protocol = protocol;
        this.relativePath = relativePath;
        this.folder = folder;
        this.size = this.folder ? 0 : size;
        this.displayName = displayName;
    }
    /**
     * Returns the protocol (conventional prefix of the path) of this object
     */
    public String getProtocol() {
        return protocol;
    }
    /**
     * Returns the path of this object, relative to the repository root
     */
    public String getRelativePath() { return relativePath; }

    /**
     * Checks if this object represents a folder
     */
    public boolean isFolder() { return folder; }

    /**
     * Returns the size of this object if the object is a file. For folders, the size is 0.
     */
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

    /**
     * Returns the name of the product designated by this object (if any).
     */
    public String getProductName() { return productName; }

    public void setProductName(String productName) { this.productName = productName; }

    /**
     * Returns the friendly name of the object to be displayed in the visual interface
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the last modified timestamp of this object, as appearing in the storage.
     * If the storage service doesn't support this, returns null.
     */
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns additional attributes for this object (if any)
     */
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
                "protocol='" + protocol + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", folder=" + folder +
                ", size=" + size +
                ", productName='" + productName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", lastModified=" + lastModified +
                ", attributes=" + attributes +
                '}';
    }
}
