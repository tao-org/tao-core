package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/**
 * GDAL BuildVRTOptions JNI driver class
 *
 * @author Adrian Drăghici
 */
public class RasterizeOptions extends GDALOptions {

    /**
     * The name of JNI GDAL BuildVRTOptions class
     */
    static final String CLASS_NAME = "org.gdal.gdal.RasterizeOptions";

    /**
     * Creates new instance for this driver
     *
     * @param jniBuildVRTOptionsInstance the JNI GDAL BuildVRTOptions class instance
     */
    public RasterizeOptions(Object jniBuildVRTOptionsInstance) {
        super(jniBuildVRTOptionsInstance);
    }

    /**
     * Creates new GDAL BuildVRTOptions class instance
     */
    public RasterizeOptions(Vector options) {
        super(options);
    }

    @Override
    protected String className() {
        return CLASS_NAME;
    }
}
