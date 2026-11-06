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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String utility class
 *
 * @author  Cosmin Cara
 * @since   1.0
 */
public class StringUtilities {
    // a991206e-3ba2-457a-bbd6-ec38d2853e8c
    private static final Pattern guidPattern = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    // name_3
    private static final Pattern numberedNamePattern = Pattern.compile("([A-Za-z0-9-]+)(?:_(\\d+))");
    private static final Pattern specialCharsPattern = Pattern.compile("[&+',`]");
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
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
        StringBuilder buffer = new StringBuilder(array.length << 4);
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
        final int missing = length - value.length();
        return missing <= 0 ? value : padValue.repeat(missing) + value;
    }
    /**
     * Pads a string to the right with the given number of pad values
     * @param value     The string to be padded
     * @param length    The length of padding
     * @param padValue  The value to pad with
     */
    public static String padRight(String value, int length, String padValue) {
        final int missing = length - value.length();
        return missing <= 0 ? value : value + padValue.repeat(missing);
    }

    /**
     * Converts the first character of the given string to uppercase and the rest to lowercase.
     * If the string is empty, it will return an empty string. This method assumes the input is not null.
     *
     * @param value the input string to be converted
     * @return the processed string with the first character in uppercase and the rest in lowercase
     */
    public static String toFirstCaps(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    /**
     * Converts an array of bytes to its corresponding hexadecimal string representation.
     * Each byte is converted to a two-character hexadecimal value.
     *
     * @param bytes The byte array to be converted. If null or empty, the method will return null.
     * @return A string containing the hexadecimal representation of the byte array,
     *         or null if the input byte array is null or empty.
     */
    public static String byteArrayToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            chars[i * 2] = HEX_CHARS[value >>> 4];
            chars[i * 2 + 1] = HEX_CHARS[value & 0x0f];
        }
        return new String(chars);
    }

    /**
     * Converts a hexadecimal string into its corresponding byte array representation.
     * The input string is processed two characters at a time, each representing
     * a hexadecimal value, and converted into a byte.
     * If the input string has an odd number of characters, it will be padded with a leading zero.
     *
     * @param s The hexadecimal string to be converted. It must consist of valid hexadecimal digits (0-9, A-F, a-f).
     * @return A byte array corresponding to the hexadecimal representation in the input string.
     *         Returns an empty byte array if the input string is empty or null.
     */
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

    /**
     * Determines if the provided string is a valid GUID (Globally Unique Identifier).
     * A GUID is a 128-bit value typically represented as a string in the standard format.
     *
     * @param s the string to be checked for GUID format; it must not be null.
     * @return true if the string matches the GUID format, false otherwise.
     */
    public static boolean isGUID(String s) {
        return guidPattern.matcher(s).matches();
    }

    /**
     * Counts the number of occurrences of a given substring within a larger string.
     * The method performs a case-sensitive search.
     * If either the substring or the larger string is null or empty, the method returns 0.
     *
     * @param substring the substring to look for; cannot be null or empty
     * @param inString the string in which the search is performed; cannot be null or empty
     * @return the number of occurrences of the substring within the larger string
     */
    public static int count(String substring, String inString) {
        if (isNullOrEmpty(substring) || isNullOrEmpty(inString)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        final int length = substring.length();
        while ((idx = inString.indexOf(substring, idx)) >= 0) {
            count++;
            idx += length;
        }
        return count;
    }

    /**
     * Inserts a substring into a given source string at the specified index.
     * If the source string is null or empty, the method returns the insertion string.
     * If the specified index exceeds the length of the source string, the insertion string
     * is appended to the end of the source string.
     *
     * @param source    The original string into which the insertion is performed.
     * @param index     The position at which the insertion string is added.
     * @param insertion The string to be inserted into the source string.
     * @return A new string with the insertion string added at the specified index,
     *         or the insertion string if the source is null or empty.
     */
    public static String insertAt(String source, int index, String insertion) {
        if (isNullOrEmpty(source)) return insertion;
        if (index >= source.length()) return source + insertion;
        return source.substring(0, index) + insertion + source.substring(index);
    }

    /**
     * Replaces special characters (&, +, ', `) in the given string with underscores (_)
     * and decodes the input string using US-ASCII encoding.
     *
     * @param value The input string to process. It must not be null.
     * @return A new string where special characters are replaced with underscores and
     *         the input string is decoded using US-ASCII encoding.
     * @throws NullPointerException if the input string is null.
     */
    public static String replaceSpecialCharacters(String value) {
        return specialCharsPattern.matcher(URLDecoder.decode(value, StandardCharsets.US_ASCII)).replaceAll("_");
    }

    /**
     * Increments a numeric suffix of the input string if the provided reference string
     * matches the input string and contains a numeric suffix as well. The numeric suffix
     * of the reference is added to the input string's base to generate the incremented value.
     * If the input string does not have a numeric suffix, one is added based on the reference.
     *
     * @param value The base string to be incremented. Must not be null or empty.
     * @param reference The reference string to compare with. Might contain a numeric suffix.
     * @return The incremented string if {@code value} matches {@code reference} and
     *         both contain numeric suffixes; otherwise, returns the input value unchanged.
     * @throws NullPointerException if {@code value} is null or empty.
     */
    public static String incrementIfDuplicate(final String value, final String reference) {
        if  (isNullOrEmpty(value)) {
            throw new NullPointerException("[value]");
        }
        if (isNullOrEmpty(reference)) return value;
        int refNumber;
        if (value.equals(reference)) {
            int valueNumber = 0;
            refNumber = 1;
            int idx = 0;
            char c;
            int multiplier = 1;
            while ((c =  value.charAt(value.length() - 1 - idx++)) > '0' && c <= '9') {
                valueNumber +=  (c - '0') * multiplier;
                multiplier *= 10;
            }
            idx = 0;
            multiplier = 1;
            while ((c =  reference.charAt(reference.length() - 1 - idx++)) > '0' && c <= '9') {
                refNumber +=  (c - '0') * multiplier;
                multiplier *= 10;
            }
            return (valueNumber == 0 ? value : value.replace("_" + valueNumber, "")) + "_" + refNumber;
        } else {
            Matcher matcher = numberedNamePattern.matcher(reference);
            if (matcher.find()) {
                refNumber = Integer.parseInt(matcher.group(2)) + 1;
                if (value.equals(matcher.group(1))) {
                    return value + "_" + (Integer.parseInt(matcher.group(2)) + 1);
                } else {
                    matcher = numberedNamePattern.matcher(value);
                    return matcher.find() ? matcher.group(1) + "_" + refNumber : value;
                }
            }
            return value;
        }
    }

    /**
     * Checks if a sequence of one or more strings is present in a list.
     * @param list      The list to be checked
     * @param elements  The sequence to search for
     * @return      <code>true</code> if the sequence is found, <code>false</code> otherwise.
     */
    public static boolean containsSequence(final List<String> list, String...elements) {
        if (list == null || list.isEmpty() || elements == null || elements.length == 0) return false;
        final int size = list.size();
        for (int i = 0; i < size; i++) {
            if (Objects.equals(list.get(i), elements[0])) {
                if (size - i < elements.length) return false;
                for (int j = 1; j < elements.length; j++) {
                    if (!Objects.equals(list.get(i + j), elements[j])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
