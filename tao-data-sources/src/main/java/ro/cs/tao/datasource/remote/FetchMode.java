/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.datasource.remote;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * The download behavior enumeration
 *
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum FetchMode {
    /**
     * Product will be downloaded from the remote site and the corresponding local product,
     * if exists, it will be overwritten
     */
    @XmlEnumValue("1")
    OVERWRITE(1),
    /**
     * Product will be downloaded from the remote site and, if a corresponding local product exists,
     * the download will be resumed from the current length of the local product
     */
    @XmlEnumValue("2")
    RESUME(2),
    /**
     * The product will be copied from a local (or shared) folder into the output folder.
     * No remote download will be performed.
     * This works only in conjunction with the --input command line parameter.
     */
    @XmlEnumValue("3")
    COPY(3),
    /**
     * Only a symlink to the product file system location, into the output folder, will be created.
     * No remote download will be performed.
     * This works only in conjunction with the --input command line parameter.
     */
    @XmlEnumValue("4")
    SYMLINK(4);

    private final int value;
    FetchMode(int value) { this.value = value; }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }



    /**
     * Retrieve string enum token corresponding to the integer identifier
     * @param value the integer value identifier
     * @return the string token corresponding to the integer identifier
     */
    public static String getEnumConstantNameByValue(final int value)
    {
        for (FetchMode type : values())
        {
            if ((String.valueOf(value)).equals(type.toString()))
            {
                // return the name of the enum constant having the given value
                return type.name();
            }
        }
        return null;
    }
}