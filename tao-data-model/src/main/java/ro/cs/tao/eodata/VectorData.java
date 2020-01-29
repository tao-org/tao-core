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
package ro.cs.tao.eodata;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "eoData")
public class VectorData extends EOData implements Serializable {

    private Set<String> refs;

    public Set<String> getRefs() { return refs; }
    public void setRefs(Set<String> refs) { this.refs = refs; }

    public void addReference(String userName) {
        if (this.refs == null) {
            this.refs = new HashSet<>();
        }
        this.refs.add(userName);
    }

    public void removeReference(String userName) {
        if (this.refs != null) {
            this.refs.remove(userName);
        }
    }
}
