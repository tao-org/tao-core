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
package ro.cs.tao.datasource;

/**
 * Flag class for describing the data source object intention (capability).
 * A data source can present any combination of these flags.
 *
 * @author Cosmin Cara
 */
public final class DataSourceCapability {
    /**
     * Flag indicating that the data source is intended for querying.
     */
    public static final int QUERY = 0x01;
    /**
     * Flag indicating that the data source is intended for downloads.
     */
    public static final int DOWNLOAD = 0x02;
    /**
     * Flag indicating that the data source is intended for uploads.
     */
    public static final int UPLOAD = 0x04;
}
