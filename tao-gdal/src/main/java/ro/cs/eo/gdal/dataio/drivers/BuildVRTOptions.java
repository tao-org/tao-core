package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/**
 * GDAL BuildVRTOptions JNI driver class
 *
 * @author Adrian Drăghici
 */
public class BuildVRTOptions extends GDALOptions {

    /**
     * The name of JNI GDAL BuildVRTOptions class
     */
    static final String CLASS_NAME = "org.gdal.gdal.BuildVRTOptions";

    /**
     * Creates new instance for this driver
     *
     * @param jniBuildVRTOptionsInstance the JNI GDAL BuildVRTOptions class instance
     */
    public BuildVRTOptions(Object jniBuildVRTOptionsInstance) {
        super(jniBuildVRTOptionsInstance);
    }

    /**
     * Creates new GDAL BuildVRTOptions class instance
     */
    public BuildVRTOptions(Vector options) {
        super(options);
    }

    @Override
    protected String className() {
        return CLASS_NAME;
    }
}
