package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/**
 * GDAL WarpOptions JNI driver class
 *
 * @author Adrian Drăghici
 */
public class WarpOptions extends GDALOptions {

    /**
     * The name of JNI GDAL WarpOptions class
     */
    private static final String CLASS_NAME = "org.gdal.gdal.WarpOptions";

    /**
     * Creates new instance for this driver
     *
     * @param jniWarpOptionsInstance the JNI GDAL WarpOptions class instance
     */
    public WarpOptions(Object jniWarpOptionsInstance) {
        super(jniWarpOptionsInstance);
    }

    /**
     * Creates new GDAL WarpOptions class instance
     */
    public WarpOptions(Vector options) {
        super(options);
    }

    @Override
    protected String className() {
        return CLASS_NAME;
    }
}
