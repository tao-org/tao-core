package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/**
 * GDAL TranslateOptions JNI driver class
 *
 * @author Adrian Drăghici
 */
public class TranslateOptions {

    /**
     * The name of JNI GDAL TranslateOptions class
     */
    private static final String CLASS_NAME = "org.gdal.gdal.TranslateOptions";

    private Object jniTranslateOptionsInstance;

    /**
     * Creates new instance for this driver
     *
     * @param jniTranslateOptionsInstance the JNI GDAL TranslateOptions class instance
     */
    public TranslateOptions(Object jniTranslateOptionsInstance) {
        this.jniTranslateOptionsInstance = jniTranslateOptionsInstance;
    }

    /**
     * Creates new GDAL TranslateOptions class instance
     */
    public TranslateOptions(Vector options) {
        this.jniTranslateOptionsInstance = GDALReflection.fetchGDALLibraryClassInstance(CLASS_NAME, new Class[]{Vector.class}, new Object[]{options});
    }

    public Object getJniTranslateOptionsInstance() {
        return jniTranslateOptionsInstance;
    }

}
