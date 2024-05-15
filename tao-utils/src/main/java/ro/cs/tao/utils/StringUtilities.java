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

import java.util.regex.Pattern;

/**
 * String utility class
 *
 * @author  Cosmin Cara
 * @since   1.0
 */
public class StringUtilities {
    //a991206e-3ba2-457a-bbd6-ec38d2853e8c
    private static final Pattern guidPattern = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
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

    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        String toConvert = s;
        int len = s.length();
        if (len % 2 == 1) {
            toConvert = "0" + toConvert;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(toConvert.charAt(i), 16) << 4) + Character.digit(toConvert.charAt(i+1), 16));
        }
        return data;
    }

    public static boolean isGUID(String s) {
        return guidPattern.matcher(s).matches();
    }
}
