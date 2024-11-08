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
package ro.cs.tao.datasource.converters;

import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.Polygon2D;

/**
 * Converts a polygon into a WKT.
 *
 * @author Cosmin Cara
 */
public class PolygonParameterConverter extends DefaultParameterConverter {

    public PolygonParameterConverter(QueryParameter parameter) {
        super(parameter);
        if (!Polygon2D.class.equals(parameter.getType())) {
            throw new IllegalArgumentException("Incorrect parameter type");
        }
    }

    @Override
    public String stringValue() throws ConversionException {
        Polygon2D polygon2D = (Polygon2D) this.parameter.getValue();
        if (polygon2D != null && polygon2D.getNumPoints() > 0) {
            return polygon2D.getNumPoints() < 200 ? polygon2D.toWKT() : polygon2D.toWKTBounds();
        } else {
            return null;
        }
    }
}
