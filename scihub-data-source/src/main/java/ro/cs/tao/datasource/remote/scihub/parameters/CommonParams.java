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

package ro.cs.tao.datasource.remote.scihub.parameters;

import ro.cs.tao.datasource.common.QueryParameter;
import ro.cs.tao.datasource.util.Polygon2D;

import java.util.Date;

/**
 * @author Cosmin Cara
 */
public class CommonParams {
    public static QueryParameter PLATFORM_NAME = new QueryParameter(String.class, "platformName");
    public static QueryParameter BEGIN_POSITION = new QueryParameter(Date.class, "beginPosition");
    public static QueryParameter END_POSITION = new QueryParameter(Date.class, "endPosition");
    public static QueryParameter FOOTPRINT = new QueryParameter(Polygon2D.class, "footprint");
    public static QueryParameter PRODUCT_TYPE = new QueryParameter(String.class, "productType");
}
