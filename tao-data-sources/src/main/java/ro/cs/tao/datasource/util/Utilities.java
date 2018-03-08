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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for various operations.
 *
 * @author  Cosmin Cara
 */
public class Utilities {

    public static String join(Iterable collection, String separator) {
        String result = "";
        if (collection != null) {
            boolean hasElements = false;
            for (Object aCollection : collection) {
                hasElements = true;
                result += (aCollection != null ? aCollection.toString() : "null") + separator;
            }
            if (hasElements) {
                result = result.substring(0, result.length() - separator.length());
            }
        }
        return result;
    }

    public static List<String> filter(List<String> input, String filter) {
        List<String> result = new ArrayList<>();
        if (input != null) {
            for (String item : input) {
                if (item.contains(filter)) {
                    result.add(item);
                }
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
