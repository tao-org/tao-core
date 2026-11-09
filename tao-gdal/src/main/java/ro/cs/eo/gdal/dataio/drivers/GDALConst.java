package ro.cs.eo.gdal.dataio.drivers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    static final Map<String, Integer> constants = new ConcurrentHashMap<>();

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
        return constants.computeIfAbsent("GA_ReadOnly", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL CE_Failure constant
     *
     * @return the JNI GDAL CE_Failure constant
     */
    public static Integer ceFailure() {
        return constants.computeIfAbsent("CE_Failure", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL CE_None constant
     *
     * @return the JNI GDAL CE_None constant
     */
    public static Integer ceNone() {
        return constants.computeIfAbsent("CE_None", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }
}
