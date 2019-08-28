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
package ro.cs.tao.eodata;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for Path2D instance, to be able to retrieve the number of points in the Path2D object.
 *
 * @author Cosmin Cara
 */
public class Polygon2D {
    //private static final Pattern polyPattern = Pattern.compile("POLYGON\\s?\\(\\(.*\\)\\)");
    private static final Pattern coordPattern = Pattern.compile("((?:-?(?:\\d*(?:\\.\\d+)?)) (?:-?(?:\\d*(?:\\.\\d+)?)))");

    private Path2D.Double[] polygons;
    private int numPoints;

    /**
     * Creates a polygon from a well-known text.
     * Only POLYGON, MULTIPOLYGON and POINT are supported.
     *
     * @param wkt   The text to parse.
     * @return      A closed polygon.
     */
    public static Polygon2D fromWKT(String wkt) {
        final Polygon2D polygon = new Polygon2D();
        try {
            final WKTReader reader = new WKTReader();
            final Geometry geometry = reader.read(wkt);
            if (wkt.startsWith("MULTIPOLYGON")) {
                final MultiPolygon mPolygon = (MultiPolygon) geometry;
                final int n = mPolygon.getNumGeometries();
                for (int i = 0; i < n; i++) {
                    final Geometry geometryN = mPolygon.getGeometryN(i);
                    final Coordinate[] coordinates = geometryN instanceof Polygon ?
                            ((Polygon) geometryN).getExteriorRing().getCoordinates() : geometryN.getCoordinates();
                    for (Coordinate coordinate : coordinates) {
                        polygon.append(i, coordinate.x, coordinate.y);
                    }
                }
            } else if (wkt.startsWith("POLYGON")) {
                final Coordinate[] coordinates = ((Polygon) geometry).getExteriorRing().getCoordinates();
                for (Coordinate coordinate : coordinates) {
                    polygon.append(coordinate.x, coordinate.y);
                }
            } else if (wkt.startsWith("POINT")) {
                Coordinate coordinate = geometry.getCoordinate();
                polygon.append(coordinate.x, coordinate.y);
            } else {
                // maybe we have only a list of coordinates, without being wrapped in a POLYGON((..))
                Matcher coordMatcher = coordPattern.matcher(wkt);
                while (coordMatcher.find()) {
                    String[] coords = coordMatcher.group().split(" ");
                    polygon.append(Double.parseDouble(coords[0].trim()), Double.parseDouble(coords[1].trim()));
                }
            }
        } catch (ParseException e) {
            Logger.getLogger(Polygon2D.class.getName()).warning(String.format("Cannot parse wkt [%s]. Reason: %s",
                                                                              wkt, e.getMessage()));
        }
        return polygon;
    }

    public static Polygon2D fromPath2D(Path2D.Double path) {
        if (path == null) {
            return null;
        }
        final Polygon2D polygon2D = new Polygon2D();
        final PathIterator pathIterator = path.getPathIterator(null);
        final double[] segment = new double[6];
        while (!pathIterator.isDone()) {
            pathIterator.currentSegment(segment);
            polygon2D.append(segment[0], segment[1]);
            pathIterator.next();
        }
        return polygon2D;
    }

    public Polygon2D() {
    }

    /**
     * Adds a point to the current polygon.
     * If this is not the first point, then it also adds a line between the previous point and the new one.
     *
     * @param x     The x coordinate
     * @param y     The y coordinate
     */
    public void append(double x, double y) {
        append(0, x, y);
    }

    public void append(int index, double x, double y) {
        if (polygons == null) {
            polygons = new Path2D.Double[index + 1];
            polygons[index] = new Path2D.Double();
            polygons[index].moveTo(x, y);
        } else if (index >= polygons.length) {
            polygons = Arrays.copyOf(polygons, index + 1);
            polygons[index] = new Path2D.Double();
            polygons[index].moveTo(x, y);
        } else {
            polygons[index].lineTo(x, y);
        }
        numPoints++;
    }

    /**
     * Adds a list of points to the current polygon.
     * The list items are pairs of coordinates.
     *
     * @param points    The points to be added
     */
    public void append(List<double[]> points) {
        if (points != null) {
            for (double[] pair : points) {
                if (pair != null && pair.length == 2) {
                    append(pair[0], pair[1]);
                }
            }
        }
    }

    /**
     * Marks all the inner polygons as closed. Further addition of points is not possible.
     */
    public void ensureClosed() {
        if (polygons != null) {
            for (Path2D.Double polygon : polygons) {
                if (polygon != null) {
                    polygon.closePath();
                }
            }
        }
    }

    /**
     * Returns the number of points of the current polygon.
     *
     */
    public int getNumPoints() {
        return numPoints;
    }

    public List<Point2D> getPoints() {
        List<Point2D> points = null;
        if (polygons != null) {
            points = new ArrayList<>();
            for (Path2D.Double polygon : polygons) {
                PathIterator pathIterator = polygon.getPathIterator(null);
                while (!pathIterator.isDone()) {
                    double[] segment = new double[6];
                    pathIterator.currentSegment(segment);
                    points.add(new Point2D.Double(segment[0], segment[1]));
                    pathIterator.next();
                }
            }
        }
        return points;
    }

    /**
     * Converts the single inner polygon of this instance to a Path2D.Double instance
     */
    public Path2D.Double toPath2D() {
        Path2D.Double path = null;
        if (polygons != null) {
            if (polygons.length > 1) {
                throw new RuntimeException("Multi-polygons cannot be converted to single Path2D");
            }
            path = polygons[0];
        }
        return path;
    }

    /**
     * Produces a WKT representation of this polygon.
     * Default decimal precision is 4.
     */
    public String toWKT() {
        return toWKT(4);
    }
    /**
     * Produces a WKT representation of this polygon with a given decimal precision
     */
    public String toWKT(int precision) {
        StringBuilder format = new StringBuilder(".");
        int i = 0;
        while (i++ < precision) format.append("#");
        final DecimalFormat dfFormat = new DecimalFormat(format.toString(), DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        final StringBuilder buffer = new StringBuilder();
        final boolean isMulti = this.polygons.length > 1;
        final boolean isPoint = this.polygons.length == 1 && this.numPoints == 1;
        buffer.append(isPoint ? "POINT(" : isMulti ? "MULTIPOLYGON(((" : "POLYGON((");
        final double[] segment = new double[6];
        for (int j = 0; j < this.polygons.length; j++) {
            if (j > 0) {
                buffer.append("((");
            }
            final PathIterator pathIterator = polygons[j].getPathIterator(null);
            while (!pathIterator.isDone()) {
                pathIterator.currentSegment(segment);
                buffer.append(dfFormat.format(segment[0])).append(" ").append(dfFormat.format(segment[1])).append(",");
                pathIterator.next();
            }
            buffer.setLength(buffer.length() - 1);
            buffer.append(isPoint ? ")" : "))");
            if (isMulti) {
                buffer.append(",");
            }
        }
        if (isMulti) {
            buffer.setLength(buffer.length() - 1);
            buffer.append(")");
        }
        return buffer.toString();
    }
    /**
     * Produces an array of WKT representations of this polygon.
     * Default decimal precision is 4.
     */
    public String[] toWKTArray() {
        return toWKTArray(4);
    }
    /**
     * Produces an array of WKT representations of this polygon with a given decimal precision.
     */
    public String[] toWKTArray(int precision) {
        final String[] polygons = new String[this.polygons.length];
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder format = new StringBuilder(".");
        int i = 0;
        while (i++ < precision) format.append("#");
        final DecimalFormat dfFormat = new DecimalFormat(format.toString(), DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        final double[] segment = new double[6];
        for (int j = 0; j < this.polygons.length; j++) {
            buffer.append("POLYGON((");
            PathIterator pathIterator = this.polygons[j].getPathIterator(null);
            while (!pathIterator.isDone()) {
                pathIterator.currentSegment(segment);
                buffer.append(dfFormat.format(segment[0])).append(" ").append(dfFormat.format(segment[1])).append(",");
                pathIterator.next();
            }
            buffer.setLength(buffer.length() - 1);
            buffer.append("))");
            polygons[j] = buffer.toString();
            buffer.setLength(0);
        }

        return polygons;
    }

    public Rectangle2D getBounds2D() {
        if (polygons != null) {
            Rectangle2D rectangle = polygons[0].getBounds2D();
            for (int i = 1; i < polygons.length; i++) {
                rectangle = rectangle.createUnion(polygons[i].getBounds2D());
            }
            return rectangle;
        }
        return null;
    }

    public String toWKTBounds() {
        Rectangle2D bounds2D = getBounds2D();
        DecimalFormat dfFormat = new DecimalFormat(".######", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        return  "POLYGON((" +
                dfFormat.format(bounds2D.getMinX()) + " " + dfFormat.format(bounds2D.getMinY()) + "," +
                dfFormat.format(bounds2D.getMaxX()) + " " + dfFormat.format(bounds2D.getMinY()) + "," +
                dfFormat.format(bounds2D.getMaxX()) + " " + dfFormat.format(bounds2D.getMaxY()) + "," +
                dfFormat.format(bounds2D.getMinX()) + " " + dfFormat.format(bounds2D.getMaxY()) + "," +
                dfFormat.format(bounds2D.getMinX()) + " " + dfFormat.format(bounds2D.getMinY()) + "))";
    }

}
