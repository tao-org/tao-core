package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.util.TileExtent;
import ro.cs.tao.eodata.Polygon2D;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Map of Landsat8 tile extents. The initial map can be created from the official wrt_descending.shp converted to KML.
 *
 * @author Cosmin Cara
 */
public class Landsat8TileExtent extends TileExtent {
    private static final Landsat8TileExtent instance;

    static {
        instance = new Landsat8TileExtent();
    }

    public static Landsat8TileExtent getInstance() {
        return instance;
    }

    private Landsat8TileExtent() { super(); }

    @Override
    public void fromKml(BufferedReader bufferedReader) throws IOException {
        try {
            String line;
            String path = null, row = null;
            boolean inElement = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("<Placemark>")) {
                    inElement = true;
                } else {
                    if (inElement && line.contains("name=\"PATH\"")) {
                        int i = line.indexOf("name=\"PATH\"");
                        path = line.substring(i + 12, line.indexOf("</"));
                        path = ("000" + path).substring(path.length());
                    }
                    if (inElement && line.contains("name=\"ROW\"")) {
                        int i = line.indexOf("name=\"ROW\"");
                        row = line.substring(i + 11, line.indexOf("</"));
                        row = ("000" + row).substring(row.length());
                    }
                    if (inElement && !line.trim().startsWith("<")) {
                        String[] tokens = line.trim().split(" ");
                        Polygon2D polygon = new Polygon2D();
                        for (String point : tokens) {
                            String[] coords = point.split(",");
                            polygon.append(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
                        }
                        tiles.put(path + row, polygon.getBounds2D());
                        inElement = false;
                    }
                }
            }
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }
}
