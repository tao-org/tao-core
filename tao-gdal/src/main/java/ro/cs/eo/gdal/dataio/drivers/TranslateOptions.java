package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/**
 * GDAL TranslateOptions JNI driver class
 *
 * @author Adrian Drăghici
 */
public class TranslateOptions extends GDALOptions {

    /**
     * The name of JNI GDAL TranslateOptions class
     */
    private static final String CLASS_NAME = "org.gdal.gdal.TranslateOptions";

    /**
     * Creates new instance for this driver
     *
     * @param jniTranslateOptionsInstance the JNI GDAL TranslateOptions class instance
     */
    public TranslateOptions(Object jniTranslateOptionsInstance) {
        super(jniTranslateOptionsInstance);
    }

    /**
     * Creates new GDAL TranslateOptions class instance
     */
    public TranslateOptions(Vector options) {
        super(options);
    }

    @Override
    protected String className() {
        return CLASS_NAME;
    }
}
