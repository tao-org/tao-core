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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class MapAdapter extends XmlAdapter<Map<String, String>, String> {
    @Override
    public String unmarshal(Map<String, String> v) throws Exception {
        if (v == null) return null;
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int count = v.size();
        int current = 0;
        for (Map.Entry<String, String> entry : v.entrySet()) {
            builder.append("\"").append(entry.getKey()).append("\"").append(":");
            builder.append("\"").append(entry.getValue()).append("\"");
            if (++current < count) {
                builder.append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public Map<String, String> marshal(String v) throws Exception {
        if (v == null) return null;
        Map<String, String> map = new HashMap<>();
        String[] entries = v.substring(1, v.length() - 1).split(",\"");
        int idx;
        for (int i = 0; i < entries.length; i++) {
            String entry = (i > 0 ? "\"" : "") + entries[i];
            idx = entry.indexOf(":");
            if (idx > 0) {
                final String key = entry.substring(0, idx);
                final String value = entry.substring(idx + 1);
                map.put(key.replace("\"", ""),
                        !"null".equals(value) ? value.replace("\"", "") : null);
            } else {
                Logger.getLogger(MapAdapter.class.getName()).warning(String.format("Cannot map input: %s", v));
            }
        }
        return map;
    }
}
