package ro.cs.eo.gdal.dataio.drivers;

/**
 * GDAL gdalconst JNI driver class
 *
 * @author Adrian Drăghici
 */
public class GDALConst {

    /**
     * The name of JNI GDAL gdalconst class
     */
    static final String CLASS_NAME = "org.gdal.gdalconst.gdalconst";

    /**
     * Creates new instance for this driver
     */
    private GDALConst() {
        //nothing to init
    }

    /**
     * Calls the JNI GDAL GA_ReadOnly constant
     *
     * @return the JNI GDAL GA_ReadOnly constant
     */
    public static Integer gaReadonly() {
        return GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, "GA_ReadOnly");
    }

    /**
     * Calls the JNI GDAL CE_Failure constant
     *
     * @return the JNI GDAL CE_Failure constant
     */
    public static Integer ceFailure() {
        return GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, "CE_Failure");
    }

    /**
     * Calls the JNI GDAL CE_None constant
     *
     * @return the JNI GDAL CE_None constant
     */
    public static Integer ceNone() {
        return GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, "CE_None");
    }
}
