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

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for converting between a list of strings and a single string
 * representation. This adapter is useful for XML serialization and
 * deserialization processes.
 *
 * The unmarshal method takes a list of strings and converts it into a single
 * concatenated string. The resulting string is enclosed in square brackets
 * and separates list elements with commas.
 *
 * The marshal method takes a single formatted string and converts it back
 * into a list of strings. If the string is enclosed in square brackets
 * and contains comma-separated values, those values are split into a list.
 * Otherwise, the string is placed as a single element in the list.
 *
 * This class extends the jakarta.xml.bind.annotation.adapters.XmlAdapter, making
 * it compatible with JAXB processing.
 */
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

