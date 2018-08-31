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
package ro.cs.tao.component.constraints;

import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.serialization.ConstraintAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Arrays;

/**
 * Constraint that indicates that several data descriptors should have the same dimension.
 *
 * @author Cosmin Cara
 * @see DataDescriptor for dimension representation.
 */
@Constraint(name = "Same dimensions")
@XmlJavaTypeAdapter(ConstraintAdapter.class)
public class DimensionConstraint extends IOConstraint {
    @Override
    public boolean check(DataDescriptor... args) {
        return args != null && args.length > 0 &&
                Arrays.stream(args)
                        .allMatch(a -> args[0].getDimension().equals(a.getDimension()));
    }
}
