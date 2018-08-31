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

package ro.cs.tao.utils;

import java.time.Duration;

/**
 * Utility class for various operations (such as formatting) on date types.
 *
 * @author Cosmin Cara
 * @since 1.0
 */
public class DateUtils {

    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "--h--m--s";
        }
        long seconds = duration.getSeconds();
        return seconds < 3600 ? String.format("%02dm%02ds", seconds / 60, seconds % 60) :
                String.format("%02dh%02dm%02ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

}
