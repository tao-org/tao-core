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
package ro.cs.tao.eodata.util;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import ro.cs.tao.eodata.enums.PixelType;

/**
 * Utility class for converting one measure to another.
 *
 * @author Cosmin Cara
 */
public class Conversions {

    /**
     * Determines the PixelType enum value given the minimum and maximum values.
     * @param minValue  The minimum value
     * @param maxValue  The maximum value
     */
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

    /**
     * Converts the (lat,lon) values to (easting, northing) meter values
     * @param latitude  The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     * @return
     */
    public static double[] degreeToMeters(double latitude, double longitude) {
        double easting;
        double northing;
        int zone;
        char letter;
            zone = (int) Math.floor(longitude / 6 + 31);
            if (latitude < -72)
                letter = 'C';
            else if (latitude < -64)
                letter = 'D';
            else if (latitude < -56)
                letter = 'E';
            else if (latitude < -48)
                letter = 'F';
            else if (latitude < -40)
                letter = 'G';
            else if (latitude < -32)
                letter = 'H';
            else if (latitude < -24)
                letter = 'J';
            else if (latitude < -16)
                letter='K';
            else if (latitude < -8)
                letter='L';
            else if (latitude < 0)
                letter='M';
            else if (latitude < 8)
                letter='N';
            else if (latitude < 16)
                letter='P';
            else if (latitude < 24)
                letter='Q';
            else if (latitude < 32)
                letter='R';
            else if (latitude < 40)
                letter='S';
            else if (latitude < 48)
                letter='T';
            else if (latitude < 56)
                letter='U';
            else if (latitude < 64)
                letter='V';
            else if (latitude < 72)
                letter='W';
            else
                letter='X';
        final double radPerDeg = Math.PI / 180;
        easting=0.5*Math.log((1+Math.cos(latitude*radPerDeg)*Math.sin(longitude*radPerDeg-(6*zone-183)*radPerDeg))/(1-Math.cos(latitude*radPerDeg)*Math.sin(longitude*radPerDeg-(6*zone-183)*radPerDeg)))*0.9996*6399593.62/Math.pow((1+Math.pow(0.0820944379, 2)*Math.pow(Math.cos(latitude*radPerDeg), 2)), 0.5)*(1+ Math.pow(0.0820944379,2)/2*Math.pow((0.5*Math.log((1+Math.cos(latitude*radPerDeg)*Math.sin(longitude*radPerDeg-(6*zone-183)*radPerDeg))/(1-Math.cos(latitude*radPerDeg)*Math.sin(longitude*radPerDeg-(6*zone-183)*radPerDeg)))),2)*Math.pow(Math.cos(latitude*radPerDeg),2)/3)+500000;
        easting=Math.round(easting*100)*0.01;
        northing = (Math.atan(Math.tan(latitude*radPerDeg)/Math.cos((longitude*radPerDeg-(6*zone -183)*radPerDeg)))-latitude*radPerDeg)*0.9996*6399593.625/Math.sqrt(1+0.006739496742*Math.pow(Math.cos(latitude*radPerDeg),2))*(1+0.006739496742/2*Math.pow(0.5*Math.log((1+Math.cos(latitude*radPerDeg)*Math.sin((longitude*radPerDeg-(6*zone -183)*radPerDeg)))/(1-Math.cos(latitude*radPerDeg)*Math.sin((longitude*radPerDeg-(6*zone -183)*radPerDeg)))),2)*Math.pow(Math.cos(latitude*radPerDeg),2))+0.9996*6399593.625*(latitude*radPerDeg-0.005054622556*(latitude*radPerDeg+Math.sin(2*latitude*radPerDeg)/2)+4.258201531e-05*(3*(latitude*radPerDeg+Math.sin(2*latitude*radPerDeg)/2)+Math.sin(2*latitude*radPerDeg)*Math.pow(Math.cos(latitude*radPerDeg),2))/4-1.674057895e-07*(5*(3*(latitude*radPerDeg+Math.sin(2*latitude*radPerDeg)/2)+Math.sin(2*latitude*radPerDeg)*Math.pow(Math.cos(latitude*radPerDeg),2))/4+Math.sin(2*latitude*radPerDeg)*Math.pow(Math.cos(latitude*radPerDeg),2)*Math.pow(Math.cos(latitude*radPerDeg),2))/3);
        if (letter<'M')
            northing = northing + 10000000;
        northing=Math.round(northing*100)*0.01;
        return new double[] { easting, northing };
    }

    /**
     * Converts coordinates given in a certain projection to decimal WGS84 latitude and longitude values.
     *
     * @param crs   The coordinate referencing system code (EPSX:xxxx)
     * @param x     The x coordinate
     * @param y     The y coordinate
     */
    public static double[] utmToDegrees(String crs, double x, double y) throws TransformException, FactoryException {
        CoordinateReferenceSystem sourceCrs = CRS.decode(crs, true);
        CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:4326", true);
        MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs, true);
        DirectPosition2D srcDirectPosition2D = new DirectPosition2D(sourceCrs, x, y);
        DirectPosition2D destDirectPosition2D = new DirectPosition2D();
        mathTransform.transform(srcDirectPosition2D, destDirectPosition2D);
        return new double[] { destDirectPosition2D.x, destDirectPosition2D.y };
    }
}
