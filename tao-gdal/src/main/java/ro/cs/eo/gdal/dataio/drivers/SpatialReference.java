package ro.cs.eo.gdal.dataio.drivers;

/**
 * GDAL SpatialReference JNI driver class
 *
 * @author Adrian Drăghici
 */
public class SpatialReference {

    /**
     * The name of JNI GDAL SpatialReference class
     */
    private static final String CLASS_NAME = "org.gdal.osr.SpatialReference";

    private Object jniSpatialReferenceInstance;

    /**
     * Creates new instance for this driver
     *
     * @param jniSpatialReferenceInstance the JNI GDAL SpatialReference class instance
     */
    public SpatialReference(Object jniSpatialReferenceInstance) {
        this.jniSpatialReferenceInstance = jniSpatialReferenceInstance;
    }

    /**
     * Creates new GDAL SpatialReference class instance
     */
    public SpatialReference() {
        this.jniSpatialReferenceInstance = GDALReflection.fetchGDALLibraryClassInstance(CLASS_NAME, new Class[0], new Object[0]);
    }

    public Object getJniSpatialReferenceInstance() {
        return jniSpatialReferenceInstance;
    }

    /**
     * Calls the JNI GDAL SpatialReference class ImportFromEPSG(int arg) method
     *
     * @param arg the JNI GDAL SpatialReference class ImportFromEPSG(int arg) method 'arg' argument
     * @return the JNI GDAL SpatialReference class ImportFromEPSG(int arg) method result
     */
    public Integer importFromEPSG(int arg) {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "ImportFromEPSG", Integer.class, this.jniSpatialReferenceInstance, new Class[]{int.class}, new Object[]{arg});
    }

    /**
     * Calls the JNI GDAL SpatialReference class ImportFromWkt(String ppszInput) method
     *
     * @param ppszInput the JNI GDAL SpatialReference class ImportFromWkt(String ppszInput) method 'ppszInput' argument
     * @return the JNI GDAL SpatialReference class ImportFromWkt(String ppszInput) method result
     */
    public Integer importFromWkt(String ppszInput) {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "ImportFromWkt", Integer.class, this.jniSpatialReferenceInstance, new Class[]{String.class}, new Object[]{ppszInput});
    }

    /**
     * Calls the JNI GDAL SpatialReference class ExportToWkt(String[] argout) method
     *
     * @param argout the JNI GDAL SpatialReference class ExportToWkt(String[] argout) method 'argout' argument
     * @return the JNI GDAL SpatialReference class ExportToWkt(String[] argout) method result
     */
    public Integer exportToWkt(String[] argout){
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "ExportToWkt", Integer.class, this.jniSpatialReferenceInstance, new Class[]{String[].class}, new Object[]{argout});
    }

    /**
     * Calls the JNI GDAL SpatialReference class GetAuthorityCode(String target_key) method
     *
     * @param target_key the JNI GDAL SpatialReference class GetAuthorityCode(String target_key) method 'target_key' argument
     * @return the JNI GDAL SpatialReference class GetAuthorityCode(String target_key) method result
     */
    public String getAuthorityCode(String target_key){
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetAuthorityCode", String.class, this.jniSpatialReferenceInstance, new Class[]{String.class}, new Object[]{target_key});
    }

    /**
     * Calls the JNI GDAL SpatialReference class GetAuthorityName(String target_key) method
     *
     * @param target_key the JNI GDAL SpatialReference class GetAuthorityName(String target_key) method 'target_key' argument
     * @return the JNI GDAL SpatialReference class GetAuthorityName(String target_key) method result
     */
    public String getAuthorityName(String target_key){
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetAuthorityName", String.class, this.jniSpatialReferenceInstance, new Class[]{String.class}, new Object[]{target_key});
    }

    public void setAxisMappingStrategy(int strategy) {
        GDALReflection.callGDALLibraryMethod(CLASS_NAME, "SetAxisMappingStrategy", Void.class, this.jniSpatialReferenceInstance, new Class[] { int.class }, new Object[] { strategy });
    }
}
