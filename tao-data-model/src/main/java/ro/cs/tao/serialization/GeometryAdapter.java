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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class GeometryAdapter extends XmlAdapter<Geometry, String> {

    @Override
    public String unmarshal(Geometry v) {
        return v != null ? v.toText() : null;
    }

    @Override
    public Geometry marshal(String v) throws Exception {
        Geometry result = null;
        if (v != null && !v.isEmpty()) {
            try {
                Integer.parseInt(v.substring(0, 1));
                WKBReader reader = new WKBReader();
                result = reader.read(WKBReader.hexToBytes(v));
            } catch (NumberFormatException nfe) {
                WKTReader reader = new WKTReader();
                result = reader.read(v);
            }
        }
        return result;
    }

    public Geometry marshal(String v, int SRID) throws Exception {
        Geometry result = null;
        if (v != null && !v.isEmpty()) {
            try {
                Integer.parseInt(v.substring(0, 1));
                WKBReader reader = new WKBReader(new GeometryFactory(new PrecisionModel(), SRID));
                result = reader.read(WKBReader.hexToBytes(v));
            } catch (NumberFormatException nfe) {
                WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(), SRID));
                result = reader.read(v);
            }
        }
        return result;
    }
}
