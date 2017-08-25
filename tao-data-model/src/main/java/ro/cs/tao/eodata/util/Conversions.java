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
