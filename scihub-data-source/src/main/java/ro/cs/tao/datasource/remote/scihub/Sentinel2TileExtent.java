package ro.cs.tao.datasource.remote.scihub;

import ro.cs.tao.datasource.common.TileExtent;
import ro.cs.tao.datasource.util.Polygon2D;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Cosmin Cara
 */
public class Sentinel2TileExtent extends TileExtent {

    private static final Sentinel2TileExtent instance;

    static {
        instance = new Sentinel2TileExtent();
    }

    public static Sentinel2TileExtent getInstance() {
        return instance;
    }

    private Sentinel2TileExtent() { super(); }

    @Override
    public void fromKml(BufferedReader bufferedReader) throws IOException {
        try {
            String line;
            String tileCode = null;
            boolean inElement = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("<Placemark>")) {
                    inElement = true;
                } else {
                    if (inElement && line.contains("<name>")) {
                        int i = line.indexOf("<name>");
                        tileCode = line.substring(i + 6, i + 11);
                    }
                    if (inElement && !line.trim().startsWith("<")) {
                        String[] tokens = line.trim().split(" ");
                        Polygon2D polygon = new Polygon2D();
                        for (String point : tokens) {
                            String[] coords = point.split(",");
                            polygon.append(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
                        }
                        tiles.put(tileCode, polygon.getBounds2D());
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
