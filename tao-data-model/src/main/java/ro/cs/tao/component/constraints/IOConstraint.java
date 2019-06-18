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
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;

/**
 * Base class for input/output constraints (restrictions to be applied to products entering or resulting from components.
 *
 * @author Cosmin Cara
 */
public abstract class IOConstraint {
    protected String value;

    /**
     * Verifies if this constraint is satisfied.
     * @param args  The descriptors to be checked.
     * @return  <code>true</code> if the constraint is satisfied, <code>false</code> otherwise.
     */
    public abstract boolean check(DataDescriptor... args);

    /**
     * Verifies thath the given descriptors are compatible. The compatibility is dictated by implementors that
     * override this method.
     * @param source    The source descriptor
     * @param target    The target descriptor.
     */
    public boolean check(SourceDescriptor source, TargetDescriptor target) {
        return true;
    }

    public String getValue() { return value; }
}
