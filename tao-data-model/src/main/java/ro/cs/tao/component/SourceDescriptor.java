/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.component;

import ro.cs.tao.component.constraints.Constraint;
import ro.cs.tao.eodata.EOData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class SourceDescriptor extends Identifiable {
    private static final String DEFAULT_NAME = "Input";
    private EOData data;
    private List<Constraint> constraints;

    public SourceDescriptor() {
        this(DEFAULT_NAME);
    }

    public SourceDescriptor(String name) {
        super(name);
        this.constraints = new ArrayList<>();
    }

    @Override
    public String defaultName() { return DEFAULT_NAME; }

    public EOData getData() {
        return data;
    }

    public void setData(EOData data) {
        this.data = data;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void addConstraint(Constraint constraint) {
        this.constraints.add(constraint);
    }

    public boolean isCompatibleWith(TargetDescriptor other) {
        return other != null && (this.constraints.size() == 0 ||
                this.constraints.stream().allMatch(c -> c.check(this, other)));
    }
}
