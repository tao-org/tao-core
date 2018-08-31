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
package ro.cs.tao.serialization;

import ro.cs.tao.eodata.Polygon2D;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter for {@link Polygon2D} objects.
 *
 * @author Cosmin Cara
 */
public class PolygonAdapter extends XmlAdapter<String, Polygon2D> {
    @Override
    public Polygon2D unmarshal(String v) {
        return v != null ? Polygon2D.fromWKT(v) : null;
    }

    @Override
    public String marshal(Polygon2D v) {
        return v != null ? v.toWKT() : null;
    }
}
