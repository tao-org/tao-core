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

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.serialization.ConstraintAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Arrays;

/**
 * Constraint that indicates that several data descriptors should have the same CRS.
 *
 * @author Cosmin Cara
 * @see DataDescriptor for CRS representation.
 */
@Constraint(name = "Same CRS")
@XmlJavaTypeAdapter(ConstraintAdapter.class)
public class CRSConstraint extends IOConstraint {
    @Override
    public boolean check(DataDescriptor... args) {
        return args != null && args.length > 0 &&
                Arrays.stream(args)
                        .allMatch(a -> {
                            CoordinateReferenceSystem first = null;
                            try {
                                first = CRS.decode(args[0].getCrs(), true);
                            } catch (FactoryException ignored) { }
                            return (first != null && CRS.equalsIgnoreMetadata(first, a.getCrs())) ||
                                    (first == null && a.getCrs() == null);
                        });
    }
}
