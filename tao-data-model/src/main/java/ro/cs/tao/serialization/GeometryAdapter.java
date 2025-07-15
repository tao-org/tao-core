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
 * Adapter for converting between {@link Geometry} objects and their string representations.
 * This adapter is used in XML serialization and deserialization processes.
 *
 * The {@code unmarshal} method converts a {@link Geometry} object into its string
 * representation. It utilizes the {@code toText()} method of the {@link Geometry} class
 * for this operation.
 *
 * The {@code marshal} methods create {@link Geometry} instances from string representations.
 * The string may represent either a WKB (Well-Known Binary, in hexadecimal format) or
 * a WKT (Well-Known Text) representation of the geometry. The methods attempt to distinguish
 * between the two formats based on the content of the string and parse them accordingly.
 *
 * If the string can be parsed as a WKB, the {@link WKBReader} is used. Otherwise, the
 * {@link WKTReader} is used to interpret the string as a WKT representation. Additionally,
 * the overloaded {@code marshal} method includes support for specifying the SRID (Spatial
 * Reference System Identifier) during the creation of the geometry object.
 *
 * Note: Exception handling is used to manage cases where the input string cannot be
 * successfully parsed into a valid {@link Geometry} object or is null/empty.
 *
 * @author  Cosmin Cara
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
