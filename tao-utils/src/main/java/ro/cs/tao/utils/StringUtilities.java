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

package ro.cs.tao.utils;

public class StringUtilities {

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Converts a string array to a json representation
     * @param array The array to be converted
     * @return  The json array
     */
    public static String toJson(String[] array) {
        if (array == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (String item : array) {
            buffer.append("\"").append(item).append("\"").append(",");
        }
        buffer.setLength(buffer.length() - 1);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Converts a json array to a string array
     * @param json  The json to be converted
     * @return  The string array
     */
    public static String[] fromJsonArray(String json) {
        if (json == null) {
            return null;
        }
        if (json.startsWith("[") && json.endsWith("]")) {
            return json.substring(1, json.length() - 1).split(",");
        } else {
            return new String[] { json };
        }
    }
}
