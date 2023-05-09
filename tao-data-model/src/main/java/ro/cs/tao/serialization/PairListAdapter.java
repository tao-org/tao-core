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

import ro.cs.tao.utils.Tuple;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PairListAdapter extends XmlAdapter<List<Tuple<String, String>>, String> {
    @Override
    public String unmarshal(List<Tuple<String, String>> list) {
        if (list == null) return null;
        return "[" + list.stream().map(Tuple::toString).collect(Collectors.joining(",")) + "]";
    }

    @Override
    public List<Tuple<String, String>> marshal(String listString) {
        if (listString == null) return null;
        final List<Tuple<String, String>> list = new ArrayList<>();
        if (listString.startsWith("[") && listString.endsWith("]")) {
            listString = listString.substring(1, listString.length() - 1);
        }
        final String[] tuples = listString.split(",");
        for (String tuple : tuples) {
            if (tuple.startsWith("[") && tuple.endsWith("]")) {
                final int split = tuple.indexOf(',');
                if (split <= 0) {
                    throw new IllegalArgumentException("Not a pair");
                }
                list.add(new Tuple<>(tuple.substring(0, split).trim(), tuple.substring(split + 1).trim()));
            } else {
                throw new IllegalArgumentException("Not a pair list");
            }
        }
        return list;
    }
}
