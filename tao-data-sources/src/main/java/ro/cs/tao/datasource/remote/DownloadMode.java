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

/**
 * The download behavior enumeration
 *
 * @author Cosmin Cara
 */
public enum DownloadMode {
    /**
     * Product will be downloaded from the remote site and the corresponding local product,
     * if exists, it will be overwritten
     */
    OVERWRITE,
    /**
     * Product will be downloaded from the remote site and, if a corresponding local product exists,
     * the download will be resumed from the current length of the local product
     */
    RESUME,
    /**
     * The product will be copied from a local (or shared) folder into the output folder.
     * No remote download will be performed.
     * This works only in conjunction with the --input command line parameter.
     */
    COPY,
    /**
     * Only a symlink to the product file system location, into the output folder, will be created.
     * No remote download will be performed.
     * This works only in conjunction with the --input command line parameter.
     */
    SYMLINK
}