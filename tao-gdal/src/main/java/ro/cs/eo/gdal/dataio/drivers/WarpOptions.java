package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/**
 * GDAL WarpOptions JNI driver class
 *
 * @author Adrian Drăghici
 */
public class WarpOptions {

    /**
     * The name of JNI GDAL WarpOptions class
     */
    private static final String CLASS_NAME = "org.gdal.gdal.WarpOptions";

    private Object jniWarpOptionsInstance;

    /**
     * Creates new instance for this driver
     *
     * @param jniWarpOptionsInstance the JNI GDAL WarpOptions class instance
     */
    public WarpOptions(Object jniWarpOptionsInstance) {
        this.jniWarpOptionsInstance = jniWarpOptionsInstance;
    }

    /**
     * Creates new GDAL WarpOptions class instance
     */
    public WarpOptions(Vector options) {
        this.jniWarpOptionsInstance = GDALReflection.fetchGDALLibraryClassInstance(CLASS_NAME, new Class[]{Vector.class}, new Object[]{options});
    }

    public Object getJniWarpOptionsInstance() {
        return jniWarpOptionsInstance;
    }

}
