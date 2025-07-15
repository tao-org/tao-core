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

import ro.cs.tao.component.constraints.ConstraintFactory;
import ro.cs.tao.component.constraints.IOConstraint;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The ConstraintAdapter is a custom adapter designed to handle the conversion between the
 * {@code IOConstraint} objects and their {@code String} representation in the context of
 * XML marshaling and unmarshaling. It extends the {@code XmlAdapter} class to facilitate
 * this transformation process.
 *
 * This class provides a bridge between the textual representation of constraints and their
 * corresponding object instances. The {@code unmarshal} method is responsible for converting
 * an {@code IOConstraint} instance into its string form, while the {@code marshal} method
 * converts a {@code String} back into an {@code IOConstraint} instance using the
 * {@code ConstraintFactory}.
 *
 * @author  Cosmin Cara
 */
public class ConstraintAdapter extends XmlAdapter<IOConstraint, String> {
    @Override
    public String unmarshal(IOConstraint v) throws Exception {
        return v == null ? null : v.toString();
    }

    @Override
    public IOConstraint marshal(String v) throws Exception {
        if (v == null)
            return null;
        int idx = v.indexOf('[');
        if (idx > 0) {
            return ConstraintFactory.create(v.substring(0, idx),
                                            v.substring(idx + 1, v.length() - 1));
        } else {
            return ConstraintFactory.create(v);
        }
    }
}
