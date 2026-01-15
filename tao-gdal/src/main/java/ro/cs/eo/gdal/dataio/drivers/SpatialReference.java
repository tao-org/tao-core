package ro.cs.eo.gdal.dataio.drivers;

import java.lang.invoke.MethodHandle;

/**
 * GDAL SpatialReference JNI driver class
 *
 * @author Adrian Drăghici
 */
public class SpatialReference extends InstantiableGDALBase {

    /**
     * The name of JNI GDAL SpatialReference class
     */
    static final String CLASS_NAME = "org.gdal.osr.SpatialReference";
    static final Class<?> JNI_CLASS;

    static {
        JNI_CLASS = GDALReflection.fetchGDALLibraryClass(CLASS_NAME);
    }

    private final MethodHandle importFromEPSGHandle;
    private final MethodHandle importFromWktHandle;
    private final MethodHandle exportToWktHandle;
    private final MethodHandle getAuthorityCodeHandle;
    private final MethodHandle getAuthorityNameHandle;
    private final MethodHandle setAxisMappingStrategyHandle;


    /**
     * Creates new instance for this driver
     *
     * @param jniSpatialReferenceInstance the JNI GDAL SpatialReference class instance
     */
    public SpatialReference(Object jniSpatialReferenceInstance) {
        super(jniSpatialReferenceInstance);
        try {
            importFromEPSGHandle = createHandle(JNI_CLASS, "ImportFromEPSG", int.class, int.class);
            importFromWktHandle = createHandle(JNI_CLASS, "ImportFromWkt", int.class, String.class);
            exportToWktHandle = createHandle(JNI_CLASS, "ExportToWkt", int.class, String[].class);
            getAuthorityCodeHandle = createHandle(JNI_CLASS, "GetAuthorityCode", String.class, String.class);
            getAuthorityNameHandle = createHandle(JNI_CLASS, "GetAuthorityName", String.class, String.class);
            setAxisMappingStrategyHandle = createHandle(JNI_CLASS, "SetAxisMappingStrategy", void.class, int.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object newInstance() {
        try {
            return JNI_CLASS.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates new GDAL SpatialReference class instance
     */
    public SpatialReference() {
        this(newInstance());
    }

    /**
     * Calls the JNI GDAL SpatialReference class ImportFromEPSG(int arg) method
     *
     * @param arg the JNI GDAL SpatialReference class ImportFromEPSG(int arg) method 'arg' argument
     * @return the JNI GDAL SpatialReference class ImportFromEPSG(int arg) method result
     */
    public Integer importFromEPSG(int arg) {
        return (Integer) invoke(importFromEPSGHandle, this.jniInstance, arg);
    }

    /**
     * Calls the JNI GDAL SpatialReference class ImportFromWkt(String ppszInput) method
     *
     * @param ppszInput the JNI GDAL SpatialReference class ImportFromWkt(String ppszInput) method 'ppszInput' argument
     * @return the JNI GDAL SpatialReference class ImportFromWkt(String ppszInput) method result
     */
    public Integer importFromWkt(String ppszInput) {
        return (Integer) invoke(importFromWktHandle, this.jniInstance, ppszInput);
    }

    /**
     * Calls the JNI GDAL SpatialReference class ExportToWkt(String[] argout) method
     *
     * @param argout the JNI GDAL SpatialReference class ExportToWkt(String[] argout) method 'argout' argument
     * @return the JNI GDAL SpatialReference class ExportToWkt(String[] argout) method result
     */
    public Integer exportToWkt(String[] argout){
        return (Integer) invoke(exportToWktHandle, this.jniInstance, argout);
    }

    /**
     * Calls the JNI GDAL SpatialReference class GetAuthorityCode(String target_key) method
     *
     * @param target_key the JNI GDAL SpatialReference class GetAuthorityCode(String target_key) method 'target_key' argument
     * @return the JNI GDAL SpatialReference class GetAuthorityCode(String target_key) method result
     */
    public String getAuthorityCode(String target_key){
        return  (String) invoke(getAuthorityCodeHandle, this.jniInstance, target_key);
    }

    /**
     * Calls the JNI GDAL SpatialReference class GetAuthorityName(String target_key) method
     *
     * @param target_key the JNI GDAL SpatialReference class GetAuthorityName(String target_key) method 'target_key' argument
     * @return the JNI GDAL SpatialReference class GetAuthorityName(String target_key) method result
     */
    public String getAuthorityName(String target_key){
        return   (String) invoke(getAuthorityNameHandle, this.jniInstance, target_key);
    }

    public void setAxisMappingStrategy(int strategy) {
        invoke(setAxisMappingStrategyHandle, this.jniInstance, strategy);
    }

    public void delete() {
        close();
    }

}
