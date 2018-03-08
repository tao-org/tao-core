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

import com.vividsolutions.jts.geom.Coordinate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class PointAdapter extends XmlAdapter<Coordinate, String> {

    @Override
    public String unmarshal(Coordinate v) throws Exception {
        if (v != null) {
            String vs = v.toString();
            vs = vs.substring(1, vs.length() - 1);
            return vs;
        } else
            return null;
    }

    @Override
    public Coordinate marshal(String v) throws Exception {
        Coordinate result = null;
        if (v != null && !v.isEmpty()) {
            String[] tokens = v.split(",");
            switch (tokens.length) {
                case 2:
                    result = new Coordinate(Double.parseDouble(tokens[0]),
                                            Double.parseDouble(tokens[1]),
                                            0);
                    break;
                case 3:
                    result = new Coordinate(Double.parseDouble(tokens[0]),
                                            Double.parseDouble(tokens[1]),
                                            Double.parseDouble(tokens[2]));
                    break;
                default:
                    throw new SerializationException("Invalid string coordinate");
            }
        }
        return result;
    }
}
