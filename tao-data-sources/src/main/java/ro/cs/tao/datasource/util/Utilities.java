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
package ro.cs.tao.datasource.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for various operations.
 *
 * @author  Cosmin Cara
 */
public class Utilities {

    public static String join(Iterable collection, String separator) {
        StringBuilder result = new StringBuilder();
        if (collection != null) {
            boolean hasElements = false;
            for (Object aCollection : collection) {
                hasElements = true;
                result.append(aCollection != null ? aCollection.toString() : "null").append(separator);
            }
            if (hasElements) {
                result = new StringBuilder(result.substring(0, result.length() - separator.length()));
            }
        }
        return result.toString();
    }

    public static List<String> filter(List<String> input, String filter) {
        List<String> result = new ArrayList<>();
        if (input != null) {
            if (filter != null && filter.contains("|")) {
                final Set<String> filters = Arrays.stream(filter.split(Pattern.quote("|"))).collect(Collectors.toSet());
                result.addAll(input.stream()
                        .filter(i -> filters.stream().anyMatch(i::contains))
                        .collect(Collectors.toList()));
            } else {
                result.addAll(input.stream().filter(i -> filter == null || i.contains(filter)).collect(Collectors.toList()));
            }
        }
        return result;
    }

    public static String find(List<String> input, String filter, String psdVersion) {
        String value = null;
        String granuleIdentifier;
        switch (psdVersion) {
            case Constants.PSD_13 :
                for (String line : input) {
                    granuleIdentifier = getAttributeValue(line, "granuleIdentifier");
                    if (granuleIdentifier.contains(filter)) {
                        value = granuleIdentifier;
                        break;
                    }
                }
                break;
            case Constants.PSD_14:
                String datastripIdentifier;
                for (String line : input) {
                    granuleIdentifier = getAttributeValue(line, "granuleIdentifier");
                    if (granuleIdentifier.contains(filter)) {
                        datastripIdentifier = getAttributeValue(line, "datastripIdentifier");
                        value = granuleIdentifier.substring(13, 16) + "_" +
                                granuleIdentifier.substring(49, 55) + "_" +
                                granuleIdentifier.substring(41, 48) + "_" +
                                datastripIdentifier.substring(42, 57);
                        break;
                    }
                }
                break;
        }
        return value;
    }

    public static String formatTime(long millis) {
        return String.format("%02dh:%02dm:%02ds",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static String getAttributeValue(String xmlLine, String name) {
        String value = null;
        int idx = xmlLine.indexOf(name);
        if (idx > 0) {
            int start = idx + name.length() + 2;
            value = xmlLine.substring(start, xmlLine.indexOf("\"", start));
        }
        return value;
    }
}
