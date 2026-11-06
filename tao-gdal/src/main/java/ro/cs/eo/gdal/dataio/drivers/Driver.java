package ro.cs.eo.gdal.dataio.drivers;

import java.lang.invoke.MethodHandle;

/**
 * GDAL Driver JNI driver class
 *
 * @author Adrian Drăghici
 */
public class Driver extends InstantiableGDALBase {

    /**
     * The name of JNI GDAL Driver class
     */
    static final String CLASS_NAME = "org.gdal.gdal.Driver";
    private static final Class<?> driverClass;

    static {
        driverClass = GDALReflection.fetchGDALLibraryClass(CLASS_NAME);
    }
    
    private final MethodHandle getShortNameHandle;
    private final MethodHandle getLongNameHandle;
    private final MethodHandle create1Handle;
    private final MethodHandle create2Handle;
    private final MethodHandle createCopyHandle;
    private final MethodHandle delete2Handle;

    /**
     * Creates new instance for this driver
     *
     * @param jniInstance the JNI GDAL Driver class instance
     */
    Driver(Object jniInstance) {
        super(jniInstance);
        try {
            getShortNameHandle = createHandle(driverClass, "getShortName", String.class);
            getLongNameHandle = createHandle(driverClass, "getLongName", String.class);
            create1Handle = createHandle(driverClass, "Create", GDALReflection.fetchGDALLibraryClass(Dataset.CLASS_NAME),
                                         String.class, int.class, int.class, int.class, int.class);
            create2Handle = createHandle(driverClass, "Create", GDALReflection.fetchGDALLibraryClass(Dataset.CLASS_NAME),
                                         String.class, int.class, int.class, int.class, int.class, String[].class);
            createCopyHandle = createHandle(driverClass, "CreateCopy", GDALReflection.fetchGDALLibraryClass(Dataset.CLASS_NAME),
                                            String.class, GDALReflection.fetchGDALLibraryClass(Dataset.CLASS_NAME), String[].class);
            delete2Handle = createHandle(driverClass, "Delete", int.class, String.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls the JNI GDAL Driver class getShortName() method
     *
     * @return the JNI GDAL Driver class getShortName() method result
     */
    public String getShortName() {
        return (String) invoke(getShortNameHandle, this.jniInstance);
    }

    /**
     * Calls the JNI GDAL Driver class getLongName() method
     *
     * @return the JNI GDAL Driver class getLongName() method result
     */
    public String getLongName() {
        return (String) invoke(getLongNameHandle, this.jniInstance);
    }

    /**
     * Calls the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType) method
     *
     * @param utf8Path the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands) method 'utf8Path' argument
     * @param xsize    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands) method 'xsize' argument
     * @param ysize    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands) method 'ysize' argument
     * @param bands    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands) method 'bands' argument
     * @param eType    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType) method 'eType' argument
     * @return the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands) method result
     */
    public Dataset create(String utf8Path, int xsize, int ysize, int bands, int eType) {
        Object jniDatasetInstance = invoke(create1Handle, this.jniInstance, utf8Path, xsize, ysize, bands, eType);
        return jniDatasetInstance != null ? new Dataset(jniDatasetInstance) : null;
    }

    /**
     * Calls the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType) method
     *
     * @param utf8Path the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) method 'utf8Path' argument
     * @param xsize    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) method 'xsize' argument
     * @param ysize    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) method 'ysize' argument
     * @param bands    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) method 'bands' argument
     * @param eType    the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) method 'eType' argument
     * @param options  the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) method 'options' argument
     * @return the JNI GDAL Driver class Create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) method result
     */
    public Dataset create(String utf8Path, int xsize, int ysize, int bands, int eType, String[] options) {
        Object jniDatasetInstance = invoke(create2Handle, this.jniInstance, utf8Path, xsize, ysize, bands, eType, options);
        return jniDatasetInstance != null ? new Dataset(jniDatasetInstance) : null;
    }

    public Dataset createCopy(String name, Dataset src, String[] options) {
        Object jniDatasetInstance = invoke(createCopyHandle, this.jniInstance, name, src.getJniInstance(), options);
        return jniDatasetInstance != null ? new Dataset(jniDatasetInstance) : null;
    }

    /**
     * Calls the JNI GDAL Dataset class Delete(String utf8_path) method
     *
     * @param utf8_path the JNI GDAL Dataset class Delete(String utf8_path) method 'utf8_path' argument
     * @return the JNI GDAL Dataset class Delete(String utf8_path) method result
     */
    public Integer delete(String utf8_path){
        return (Integer) invoke(deleteHandle, this.jniInstance, utf8_path);
    }
}
