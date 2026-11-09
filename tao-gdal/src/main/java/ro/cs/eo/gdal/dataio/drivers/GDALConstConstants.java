package ro.cs.eo.gdal.dataio.drivers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GDAL gdalconstConstants JNI driver class
 *
 * @author Adrian Drăghici
 */
public class GDALConstConstants {

    /**
     * The name of JNI GDAL gdalconstConstants class
     */
    static final String CLASS_NAME = "org.gdal.gdalconst.gdalconstConstants";
    static final Map<String, Integer> constants = new ConcurrentHashMap<>();

    /**
     * Creates new instance for this driver
     */
    private GDALConstConstants() {
        //nothing to init
    }

    /**
     * Fetches the JNI GDAL GDT_Byte constant
     *
     * @return the JNI GDAL GDT_Byte constant
     */
    public static Integer gdtByte() {
        return constants.computeIfAbsent("GDT_Byte", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_Int16 constant
     *
     * @return the JNI GDAL GDT_Int16 constant
     */
    public static Integer gdtInt16() {
        return constants.computeIfAbsent("GDT_Int16", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_UInt16 constant
     *
     * @return the JNI GDAL GDT_UInt16 constant
     */
    public static Integer gdtUint16() {
        return constants.computeIfAbsent("GDT_UInt16", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_Int32 constant
     *
     * @return the JNI GDAL GDT_Int32 constant
     */
    public static Integer gdtInt32() {
        return constants.computeIfAbsent("GDT_Int32", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_UInt32 constant
     *
     * @return the JNI GDAL GDT_UInt32 constant
     */
    public static Integer gdtUint32() {
        return constants.computeIfAbsent("GDT_UInt32", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_Float32 constant
     *
     * @return the JNI GDAL GDT_Float32 constant
     */
    public static Integer gdtFloat32() {
        return constants.computeIfAbsent("GDT_Float32", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_Float64 constant
     *
     * @return the JNI GDAL GDT_Float64 constant
     */
    public static Integer gdtFloat64() {
        return constants.computeIfAbsent("GDT_Float64", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_CInt16 constant
     *
     * @return the JNI GDAL GDT_CInt16 constant
     */
    public static Integer gdtCInt16() {
        return constants.computeIfAbsent("GDT_CInt16", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_CInt32 constant
     *
     * @return the JNI GDAL GDT_CInt32 constant
     */
    public static Integer gdtCInt32() {
        return constants.computeIfAbsent("GDT_CInt32", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_CFloat32 constant
     *
     * @return the JNI GDAL GDT_CFloat32 constant
     */
    public static Integer gdtCFloat32() {
        return constants.computeIfAbsent("GDT_CFloat32", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GDT_CFloat64 constant
     *
     * @return the JNI GDAL GDT_CFloat64 constant
     */
    public static Integer gdtCFloat64() {
        return constants.computeIfAbsent("GDT_CFloat64", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GMF_NODATA constant
     *
     * @return the JNI GDAL GMF_NODATA constant
     */
    public static Integer gmfNodata() {
        return constants.computeIfAbsent("GMF_NODATA", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GMF_PER_DATASET constant
     *
     * @return the JNI GDAL GMF_PER_DATASET constant
     */
    public static Integer gmfPerDataset() {
        return constants.computeIfAbsent("GMF_PER_DATASET", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GMF_ALPHA constant
     *
     * @return the JNI GDAL GMF_ALPHA constant
     */
    public static Integer gmfAlpha() {
        return constants.computeIfAbsent("GMF_ALPHA", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL GMF_ALL_VALID constant
     *
     * @return the JNI GDAL GMF_ALL_VALID constant
     */
    public static Integer gmfAllValid() {
        return constants.computeIfAbsent("GMF_ALL_VALID", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }

    /**
     * Calls the JNI GDAL CE_None constant
     *
     * @return the JNI GDAL CE_None constant
     */
    public static Integer ceNone() {
        return GDALConst.ceNone();
    }

    /**
     * Calls the JNI GDAL GCI_PaletteIndex constant
     *
     * @return the JNI GDAL GCI_PaletteIndex constant
     */
    public static Integer gciPaletteindex() {
        return constants.computeIfAbsent("GCI_PaletteIndex", k -> GDALReflection.fetchGDALLibraryConstant(CLASS_NAME, k));
    }
}
