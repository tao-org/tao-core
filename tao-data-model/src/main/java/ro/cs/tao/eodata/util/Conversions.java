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
package ro.cs.tao.eodata.util;

import ro.cs.tao.eodata.enums.PixelType;

/**
 * @author Cosmin Cara
 */
public class Conversions {

    public static PixelType pixelTypeFromRange(long minValue, long maxValue) {
        PixelType result = PixelType.FLOAT32;
        if (minValue >= 0) {
            if (maxValue <= Byte.MAX_VALUE * 2 + 1) {
                result = PixelType.UINT8;
            } else if (maxValue <= Short.MAX_VALUE * 2 + 1) {
                result = PixelType.UINT16;
            } else if (maxValue <= (long)Integer.MAX_VALUE * 2 + 1) {
                result = PixelType.UINT32;
            }
        } else {
            if (maxValue <= Byte.MAX_VALUE) {
                result = PixelType.INT8;
            } else if (maxValue <= Short.MAX_VALUE) {
                result = PixelType.INT16;
            } else if (maxValue <= Integer.MAX_VALUE) {
                result = PixelType.INT32;
            }
        }
        return result;
    }
}
