/*
 * Copyright (C) 2017 CS ROMANIA
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class GeometryAdapter extends XmlAdapter<Geometry, String> {

    @Override
    public String unmarshal(Geometry v) throws Exception {
        return v != null ? v.toText() : null;
    }

    @Override
    public Geometry marshal(String v) throws Exception {
        Geometry result = null;
        if (v != null && !v.isEmpty()) {
            WKTReader reader = new WKTReader();
            result = reader.read(v);
        }
        return result;
    }
}
