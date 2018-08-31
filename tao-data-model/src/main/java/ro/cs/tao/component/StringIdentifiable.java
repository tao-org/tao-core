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

package ro.cs.tao.component;

/**
 * Specialized marker for identifiable objects that have the ID of string type.
 *
 * @author Cosmin Cara
 */
public abstract class StringIdentifiable implements Identifiable<String> {
    protected String id;

    public StringIdentifiable() { this.id = defaultId(); }

    public StringIdentifiable(String id) {
        this.id = id;
    }

    @Override
    public String getId() { return this.id; }

    @Override
    public void setId(String id) { this.id = id; }

    @Override
    public String defaultId() { return null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringIdentifiable that = (StringIdentifiable) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
