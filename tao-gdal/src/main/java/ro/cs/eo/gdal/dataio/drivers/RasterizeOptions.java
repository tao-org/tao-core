package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/**
 * GDAL BuildVRTOptions JNI driver class
 *
 * @author Adrian Drăghici
 */
public class RasterizeOptions {

    /**
     * The name of JNI GDAL BuildVRTOptions class
     */
    static final String CLASS_NAME = "org.gdal.gdal.RasterizeOptions";

    private final Object jniRasterizeOptionsInstance;

    /**
     * Creates new instance for this driver
     *
     * @param jniBuildVRTOptionsInstance the JNI GDAL BuildVRTOptions class instance
     */
    public RasterizeOptions(Object jniBuildVRTOptionsInstance) {
        this.jniRasterizeOptionsInstance = jniBuildVRTOptionsInstance;
    }

    /**
     * Creates new GDAL BuildVRTOptions class instance
     */
    public RasterizeOptions(Vector options) {
        this.jniRasterizeOptionsInstance = GDALReflection.fetchGDALLibraryClassInstance(CLASS_NAME, new Class[]{Vector.class}, new Object[]{options});
    }

    public Object getJniRasterizeOptionsInstance() {
        return jniRasterizeOptionsInstance;
    }

}
