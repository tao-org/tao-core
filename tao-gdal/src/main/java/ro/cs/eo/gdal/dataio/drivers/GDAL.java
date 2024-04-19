package ro.cs.eo.gdal.dataio.drivers;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * GDAL gdal JNI driver class
 *
 * @author Adrian Drăghici
 */
public class GDAL extends GDALBase {

    /**
     * The name of JNI GDAL gdal class
     */
    static final String CLASS_NAME = "org.gdal.gdal.gdal";
    private static final Class<?> gdalClass;
    private static final GDAL instance;
    private final MethodHandle getDataTypeNameHandle;
    private final MethodHandle getDataTypeByNameHandle;
    private final MethodHandle openHandle;
    private final MethodHandle getColorInterpretationHandle;
    private final MethodHandle getDataTypeSizeHandle;
    private final MethodHandle getDriverByNameHandle;
    private final MethodHandle buildVRTHandle;
    private final MethodHandle rasterizeHandle;
    private final MethodHandle rasterize2Handle;
    private final MethodHandle getLastErrorHandle;

    static {
        gdalClass = GDALReflection.fetchGDALLibraryClass(CLASS_NAME);
        try {
            instance = new GDAL();
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates new instance for this driver
     */
    private GDAL() throws NoSuchMethodException, IllegalAccessException {
        getDataTypeNameHandle = createStaticHandle(gdalClass, "GetDataTypeName", String.class, int.class);
        getDataTypeByNameHandle = createStaticHandle(gdalClass, "GetDataTypeByName", int.class, String.class);
        final Class<?> gdalDatasetClass = GDALReflection.fetchGDALLibraryClass(Dataset.CLASS_NAME);
        openHandle = createStaticHandle(gdalClass, "Open", gdalDatasetClass, String.class, int.class);
        getColorInterpretationHandle = createStaticHandle(gdalClass, "GetColorInterpretationName", String.class, int.class);
        getDataTypeSizeHandle = createStaticHandle(gdalClass, "GetDataTypeSize", int.class, int.class);
        getDriverByNameHandle = createStaticHandle(gdalClass, "GetDriverByName",
                                                   GDALReflection.fetchGDALLibraryClass(Driver.CLASS_NAME),
                                                   String.class);
        final Object datasetArray = Array.newInstance(gdalDatasetClass, 0);
        buildVRTHandle = createStaticHandle(gdalClass, "BuildVRT",
                                            gdalDatasetClass,
                                            String.class, datasetArray.getClass(), GDALReflection.fetchGDALLibraryClass(BuildVRTOptions.CLASS_NAME));
        rasterizeHandle = createStaticHandle(gdalClass, "Rasterize",
                                             int.class,
                                             gdalDatasetClass,
                                             gdalDatasetClass,
                                             GDALReflection.fetchGDALLibraryClass(RasterizeOptions.CLASS_NAME));
        rasterize2Handle = createStaticHandle(gdalClass, "Rasterize",
                                             gdalDatasetClass,
                                             String.class,
                                             gdalDatasetClass,
                                             GDALReflection.fetchGDALLibraryClass(RasterizeOptions.CLASS_NAME));
        getLastErrorHandle = createStaticHandle(gdalClass, "GetLastErrorMsg", String.class);

    }

    /**
     * Calls the JNI GDAL gdal class UseExceptions() method
     */
    public static void useExceptions() {
        GDALReflection.callGDALLibraryMethod(CLASS_NAME, "UseExceptions", null, null, new Class[]{}, new Object[]{});
    }

    /**
     * Calls the JNI GDAL gdal class GetLastErrorMsg() method
     */
    public static String getLastErrorMsg() {
        return (String) invokeStatic(instance.getLastErrorHandle);
    }

    /**
     * Calls the JNI GDAL gdal class AllRegister() method
     */
    public static void allRegister() {
        try {
            Method method = gdalClass.getMethod("AllRegister");
            method.invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls the JNI GDAL gdal class GetDataTypeName(int gdalDataType) method
     *
     * @param gdalDataType the JNI GDAL gdal class GetDataTypeName(int gdalDataType) method 'gdalDataType' argument
     * @return the JNI GDAL gdal class GetDataTypeName(int gdalDataType) method result
     */
    public static String getDataTypeName(int gdalDataType) {
        return (String) invokeStatic(instance.getDataTypeNameHandle, gdalDataType);
    }

    /**
     * Calls the JNI GDAL gdal class GetDataTypeByName(String pszDataTypeName) method
     *
     * @param pszDataTypeName the JNI GDAL gdal class GetDataTypeByName(String pszDataTypeName) method 'arg' pszDataTypeName
     * @return the JNI GDAL gdal class GetDataTypeByName(String pszDataTypeName) method result
     */
    public static Integer getDataTypeByName(String pszDataTypeName) {
        return (Integer) invokeStatic(instance.getDataTypeByNameHandle, pszDataTypeName);
    }

    /**
     * Calls the JNI GDAL gdal class Open(String utf8Path, int eAccess) method
     *
     * @param utf8Path the JNI GDAL gdal class Open(String utf8Path, int eAccess) method 'utf8Path' argument
     * @param eAccess  the JNI GDAL gdal class Open(String utf8Path, int eAccess) method 'eAccess' argument
     * @return the JNI GDAL gdal class Open(String utf8Path, int eAccess) method result
     */
    public static Dataset open(String utf8Path, int eAccess) {
        Object jniDatasetInstance = invokeStatic(instance.openHandle, utf8Path, eAccess);
        return jniDatasetInstance != null ? new Dataset(jniDatasetInstance) : null;
    }

    /**
     * Calls the JNI GDAL gdal class GetColorInterpretationName(int eColorInterp) method
     *
     * @param eColorInterp the JNI GDAL gdal class GetColorInterpretationName(int eColorInterp) method 'eColorInterp' argument
     * @return the JNI GDAL gdal class GetColorInterpretationName(int eColorInterp) method result
     */
    public static String getColorInterpretationName(int eColorInterp) {
        return (String) invokeStatic(instance.getColorInterpretationHandle, eColorInterp);
    }

    /**
     * Calls the JNI GDAL gdal class GetDataTypeSize(int eDataType) method
     *
     * @param eDataType the JNI GDAL gdal class GetDataTypeSize(int eDataType) method 'eDataType' argument
     * @return the JNI GDAL gdal class GetDataTypeSize(int eDataType) method result
     */
    public static Integer getDataTypeSize(int eDataType) {
        return (Integer) invokeStatic(instance.getDataTypeSizeHandle, eDataType);
    }

    /**
     * Calls the JNI GDAL gdal class GetDriverByName(String name) method
     *
     * @param name the JNI GDAL gdal class GetDriverByName(String name) method 'name' argument
     * @return the JNI GDAL gdal class GetDriverByName(String name) method result
     */
    public static Driver getDriverByName(String name) {
        Object jniDriverInstance = invokeStatic(instance.getDriverByNameHandle, name);
        return jniDriverInstance != null ? new Driver(jniDriverInstance) : null;
    }

    /**
     * Calls the JNI GDAL gdal class Warp(String dest, Dataset[] object_list_count, WarpOptions warpAppOptions) method
     *
     * @param dest            the JNI GDAL gdal class Warp(String dest, Dataset[] object_list_count, WarpOptions warpAppOptions) method 'dest' argument
     * @param objectListCount the JNI GDAL gdal class Warp(String dest, Dataset[] object_list_count, WarpOptions warpAppOptions) method 'objectListCount' argument
     * @param warpAppOptions  the JNI GDAL gdal class Warp(String dest, Dataset[] object_list_count, WarpOptions warpAppOptions) method 'warpAppOptions' argument
     * @return the JNI GDAL gdal class Warp(String dest, Dataset[] object_list_count, WarpOptions warpAppOptions) method result
     */
    public static Dataset warp(String dest, Dataset[] objectListCount, WarpOptions warpAppOptions) {
        Object objectListCountJni = Array.newInstance(objectListCount[0].getJniDatasetInstance().getClass(), objectListCount.length);

        for (int i = 0; i < objectListCount.length; i++) {
            Array.set(objectListCountJni, i, objectListCount[i].getJniDatasetInstance());
        }
        Object jniDatasetInstance;
        synchronized (GDAL.class) {
            jniDatasetInstance = GDALReflection.callGDALLibraryMethod(CLASS_NAME, "Warp", Object.class, null, new Class[]{dest.getClass(), objectListCountJni.getClass(), warpAppOptions.getJniWarpOptionsInstance().getClass()}, new Object[]{dest, objectListCountJni, warpAppOptions.getJniWarpOptionsInstance()});
        }
        if (jniDatasetInstance != null) {
            return new Dataset(jniDatasetInstance);
        }
        return null;
    }

    /**
     * Calls the JNI GDAL gdal class BuildVRT(String dest, Dataset[] object_list_count, BuildVRTOptions options) method
     *
     * @param dest            the JNI GDAL gdal class BuildVRT(String dest, Dataset[] object_list_count, BuildVRTOptions options) method 'dest' argument
     * @param objectListCount the JNI GDAL gdal class BuildVRT(String dest, Dataset[] object_list_count, BuildVRTOptions options) method 'object_list_count' argument
     * @param options         the JNI GDAL gdal class BuildVRT(String dest, Dataset[] object_list_count, BuildVRTOptions options) method 'options' argument
     * @return the JNI GDAL gdal class BuildVRT(String dest, Dataset[] object_list_count, BuildVRTOptions options) method result
     */
    public static Dataset buildVRT(String dest, Dataset[] objectListCount, BuildVRTOptions options) {
        Object objectListCountJni = Array.newInstance(objectListCount[0].getJniDatasetInstance().getClass(), objectListCount.length);
        for (int i = 0; i < objectListCount.length; i++) {
            Array.set(objectListCountJni, i, objectListCount[i].getJniDatasetInstance());
        }
        Object jniDatasetInstance = invokeStatic(instance.buildVRTHandle, dest, objectListCountJni, options.getJniBuildVRTOptionsInstance());
        if (jniDatasetInstance != null) {
            return new Dataset(jniDatasetInstance);
        }
        return null;
    }

    public static Dataset rasterize(Dataset src, Dataset dst, RasterizeOptions options) {
        Object jniDatasetInstance = invokeStatic(instance.rasterizeHandle, dst, src, options.getJniRasterizeOptionsInstance());
        if (jniDatasetInstance != null) {
            return new Dataset(jniDatasetInstance);
        }
        return null;
    }

    public static Dataset rasterize(String dst, Dataset src, RasterizeOptions options) {
        Object jniDatasetInstance = invokeStatic(instance.rasterize2Handle, dst, src, options.getJniRasterizeOptionsInstance());
        if (jniDatasetInstance != null) {
            return new Dataset(jniDatasetInstance);
        }
        return null;
    }

    /**
     * Calls the JNI GDAL gdal class Translate(String dest, Dataset dataset, TranslateOptions translateOptions) method
     *
     * @param dest             the JNI GDAL gdal class Translate(String dest, Dataset dataset, TranslateOptions translateOptions) method 'dest' argument
     * @param dataset          the JNI GDAL gdal class Translate(String dest, Dataset dataset, TranslateOptions translateOptions) method 'dataset' argument
     * @param translateOptions the JNI GDAL gdal class Translate(String dest, Dataset dataset, TranslateOptions translateOptions) method 'translateOptions' argument
     * @return the JNI GDAL gdal class Translate(String dest, Dataset dataset, TranslateOptions translateOptions) method result
     */
    public static Dataset translate(String dest, Dataset dataset, TranslateOptions translateOptions) {
        Object jniDatasetInstance = GDALReflection.callGDALLibraryMethod(CLASS_NAME, "Translate", Object.class, null, new Class[]{dest.getClass(), dataset.getJniDatasetInstance().getClass(), translateOptions.getJniTranslateOptionsInstance().getClass()}, new Object[]{dest, dataset.getJniDatasetInstance(), translateOptions.getJniTranslateOptionsInstance()});
        if (jniDatasetInstance != null) {
            return new Dataset(jniDatasetInstance);
        }
        return null;
    }

}
