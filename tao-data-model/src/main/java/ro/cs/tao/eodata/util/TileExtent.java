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

import ro.cs.tao.eodata.Polygon2D;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a mapping of tiles where each tile is identified by a unique code and is associated
 * with a geographical extent defined as a Path2D.Double object.
 * This class provides methods for reading tile extent data from input streams
 * and files, writing data to files, handling tile extents in KML format,
 * and performing geometric queries such as bounding boxes and intersection detection.
 * It is designed to be extended to provide implementation-specific details.
 *
 * @author Cosmin Cara
 */
public abstract class TileExtent {
    protected final Map<String, Path2D.Double> tiles;

    protected TileExtent() {
        tiles = new TreeMap<>();
    }

    /**
     * Reads and processes data from the provided input stream, updating the tile map with polygonal data.
     *
     * @param inputStream the input stream from which tile data is to be read
     * @throws IOException if an I/O error occurs while reading from the stream
     */
    public void read(InputStream inputStream) throws IOException {
        synchronized (tiles) {
            byte[] buffer = new byte[tileCodeSize()];
            double x;
            try (DataInputStream dis = new DataInputStream(inputStream)) {
                //noinspection InfiniteLoopStatement
                while (true) {
                    dis.read(buffer, 0, buffer.length);
                    Polygon2D polygon2D = new Polygon2D();
                    x = dis.readDouble();
                    while (Double.compare(Double.MAX_VALUE, x) != 0) {
                        polygon2D.append(x, dis.readDouble());
                        x = dis.readDouble();
                    }
                    tiles.put(new String(buffer), polygon2D.toPath2D());
                }
            } catch (EOFException ignored) { }
        }
    }

    /**
     * Writes the tile data, including tile identifiers and their associated geometric information
     * (in the form of path segments), to the specified file.
     *
     * @param file the file path where the tile data will be written
     * @throws IOException if an I/O error occurs during the write operation
     */
    public void write(Path file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file.toFile());
             DataOutputStream dos = new DataOutputStream(fos)) {
            for (Map.Entry<String, Path2D.Double> entry : tiles.entrySet()) {
                Path2D.Double rectangle = entry.getValue();
                dos.writeBytes(entry.getKey());
                PathIterator pathIterator = rectangle.getPathIterator(null);
                while (!pathIterator.isDone()) {
                    double[] segment = new double[6];
                    pathIterator.currentSegment(segment);
                    dos.writeDouble(segment[0]);
                    dos.writeDouble(segment[1]);
                    pathIterator.next();
                }
                dos.writeDouble(Double.MAX_VALUE);
            }
        }
    }

    /**
     * Reads and processes a KML file from the given file path.
     * This method attempts to open the file and delegate the reading
     * and parsing of the KML content to the {@code fromKml} method.
     *
     * @param file the path to the KML file to be read
     * @throws IOException if an I/O error occurs while reading the file
     */
    public void fromKmlFile(String file) throws IOException {
        Path kmlFile = Paths.get(file);
        if (Files.exists(kmlFile)) {
            fromKml(Files.newBufferedReader(kmlFile));
        }
    }

    /**
     * Reads and processes KML data from the given BufferedReader.
     * This method parses the provided input and updates the tile map
     * with the parsed geometric data.
     *
     * @param bufferedReader the BufferedReader from which the KML content is read
     * @throws IOException if an I/O error occurs while reading from the BufferedReader
     */
    public abstract void fromKml(BufferedReader bufferedReader) throws IOException;

    /**
     * Retrieves a list of tile identifiers currently stored in the tile map.
     * The resulting list contains the keys of the tile map, representing all available tiles.
     *
     * @return a list of all tile names in the tile map
     */
    public List<String> getTileNames() {
        synchronized (tiles) {
            return new ArrayList<>(this.tiles.keySet());
        }
    }

    /**
     * Returns the number of tiles contained in this map
     */
    public int getCount() {
        return tiles.size();
    }

    /**
     * Retrieves the geometric extent of the tile corresponding to the given tile code.
     *
     * @param tileCode the identifier of the tile whose extent is to be retrieved
     * @return the geometric extent of the tile as a {@code Path2D.Double}, or {@code null}
     *         if the tile code is not found
     */
    public Path2D.Double getTileExtent(String tileCode) {
        return tiles.get(tileCode);
    }

    /**
     * Computes the bounding box for the given list of tile identifiers
     * @param tileCodes     List of tile identifiers
     */
    public Rectangle2D boundingBox(Set<String> tileCodes) {
        Rectangle2D accumulator = null;
        synchronized (tiles) {
            if (tileCodes == null) {
                return null;
            }

            for (String code : tileCodes) {
                Path2D.Double tilePath2D = tiles.get(code);
                if (tilePath2D != null) {
                    Rectangle2D rectangle2D = tilePath2D.getBounds2D();
                    if (accumulator == null) {
                        accumulator = rectangle2D;
                    } else {
                        accumulator = accumulator.createUnion(rectangle2D);
                    }
                }
            }
        }
        return accumulator;
    }

    /**
     * Computes the list of tiles that intersect the given area of interest (rectangle).
     *
     * @param ulx   The upper left corner longitude (in degrees)
     * @param uly   The upper left corner latitude (in degrees)
     * @param lrx   The lower right corner longitude (in degrees)
     * @param lry   The lower right corner latitude (in degrees)
     */
    public Set<String> intersectingTiles(double ulx, double uly, double lrx, double lry) {
        return intersectingTiles(new Rectangle2D.Double(ulx, uly, ulx - lrx, uly - lry));
    }
    /**
     * Computes the list of tiles that intersect the given area of interest (rectangle).
     *
     * @param aoi   The area of interest bounding box
     */
    public Set<String> intersectingTiles(Rectangle2D aoi) {
        Set<String> tileCodes;
        synchronized (tiles) {
            tileCodes = tiles.entrySet().stream()
                    .filter(entry -> entry.getValue().intersects(aoi))
                    .map(Map.Entry::getKey).collect(Collectors.toSet());
        }
        return tileCodes;
    }
    /**
     * Computes the list of tiles that intersect the given area of interest (polygon).
     *
     * @param aoi   The area of interest polygon
     */
    public Set<String> intersectingTiles(Polygon2D aoi) {
        Set<String> tileCodes = new TreeSet<>();
        if (aoi != null && aoi.getNumPoints() > 0) {
            synchronized (tiles) {
                List<Point2D> points = aoi.getPoints();
                tileCodes.addAll(
                        tiles.entrySet().stream()
                                .filter(entry -> points.stream().anyMatch(p -> entry.getValue().contains(p)))
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toSet()));
            }
        }
        return tileCodes;
    }

    protected Rectangle2D boundingBox(Rectangle2D...rectangles) {
        if (rectangles == null) {
            return null;
        }
        if (rectangles.length == 1) {
            return rectangles[0];
        } else {
            Rectangle2D accumulator = rectangles[0];
            for (int i = 1; i < rectangles.length; i++) {
                accumulator.add(rectangles[i]);
            }
            return accumulator;
        }
    }

    protected abstract int tileCodeSize();

}
