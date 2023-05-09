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

/**
 * String utility class
 *
 * @author  Cosmin Cara
 * @since   1.0
 */
public class StringUtilities {

    /**
     * Convenience method to check if a string is null or empty
     * @param value     The string to check
     */
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
    /**
     * Pads a string to the left with the given number of pad values
     * @param value     The string to be padded
     * @param length    The length of padding
     * @param padValue  The value to pad with
     */
    public static String padLeft(String value, int length, String padValue) {
        return value.length() < length ? String.format("%" + length + "s", value).replace(" ", padValue) : value;
    }
    /**
     * Pads a string to the right with the given number of pad values
     * @param value     The string to be padded
     * @param length    The length of padding
     * @param padValue  The value to pad with
     */
    public static String padRight(String value, int length, String padValue) {
        return value.length() < length ? String.format("%-" + length + "s", value).replace(" ", padValue) : value;
    }

    public static String toFirstCaps(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
