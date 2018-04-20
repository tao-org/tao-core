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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringListAdapter extends XmlAdapter<List<String>, String> {
    @Override
    public String unmarshal(List<String> v) {
        if (v == null) return null;
        return "[" + String.join(",", v) + "]";
    }

    @Override
    public List<String> marshal(String v) {
        if (v == null) return null;
        List<String> list = new ArrayList<>();
        if (v.startsWith("[") && v.endsWith("]")) {
            v = v.substring(1, v.length() - 1);
            Collections.addAll(list, v.split(","));
        } else {
            list.add(v);
        }
        return list;
    }
}
