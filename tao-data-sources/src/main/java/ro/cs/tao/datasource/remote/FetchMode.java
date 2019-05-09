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
package ro.cs.tao.datasource.remote;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The download behavior enumeration
 *
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum FetchMode implements TaoEnum<Integer> {
    /**
     * Product will be downloaded from the remote site and the corresponding local product,
     * if exists, it will be overwritten
     */
    @XmlEnumValue("1")
    OVERWRITE(1, "Overwrite"),
    /**
     * Product will be downloaded from the remote site and, if a corresponding local product exists,
     * the download will be resumed from the current length of the local product
     */
    @XmlEnumValue("2")
    RESUME(2, "Resume"),
    /**
     * The product will be copied from a local (or shared) folder into the output folder.
     * No remote download will be performed.
     */
    @XmlEnumValue("3")
    COPY(3, "Copy"),
    /**
     * Only a symlink to the product file system location, into the output folder, will be created.
     * No remote download will be performed.
     */
    @XmlEnumValue("4")
    SYMLINK(4, "Symlink"),
    /**
     * No remote download will be performed. This mode behaves like the SYMLINK one, except no symlink
     * is created. Instead, only an existence check of the remote file(s) is performed and the path, if exists,
     * becomes the product location.
     */
    @XmlEnumValue("5")
    CHECK(5, "Existence check only");

    private final int value;
    private final String description;

    FetchMode(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}