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

import org.geotools.referencing.CRS;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Optional;

/**
 * @author Cosmin Cara
 */
public class CRSAdapter extends XmlAdapter<CoordinateReferenceSystem, String> {

    public CRSAdapter() { }

    public String unmarshal(CoordinateReferenceSystem v) throws Exception {
        if (v != null) {
            Optional<ReferenceIdentifier> identifier = v.getIdentifiers().stream().findFirst();
            return identifier.map(Object::toString).orElse(null);
        } else {
            return null;
        }
    }


    public CoordinateReferenceSystem marshal(String v) throws Exception {
        return v != null ? CRS.decode(v) : null;
    }
}
