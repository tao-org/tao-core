package ro.cs.eo.gdal.dataio.drivers;

public class RasterAttributeTable {

    /**
     * The name of JNI GDAL RasterAttributeTable class
     */
    private static final String CLASS_NAME = "org.gdal.gdal.RasterAttributeTable";

    private Object jniRasterAttributeTableInstance;

    /**
     * Creates new instance for this driver
     *
     * @param jniRasterAttributeTableInstance the JNI GDAL RasterAttributeTable class instance
     */
    public RasterAttributeTable(Object jniRasterAttributeTableInstance) {
        this.jniRasterAttributeTableInstance = jniRasterAttributeTableInstance;
    }

    /**
     * Calls the JNI GDAL RasterAttributeTable class GetColumnCount() method
     *
     * @return the JNI GDAL RasterAttributeTable class GetColumnCount() method result
     */
    public Integer getColumnCount() {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetColumnCount", Integer.class, this.jniRasterAttributeTableInstance, new Class[]{}, new Object[]{});
    }

    /**
     * Calls the JNI GDAL RasterAttributeTable class GetRowCount() method
     *
     * @return the JNI GDAL RasterAttributeTable class GetRowCount() method result
     */
    public Integer getRowCount() {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetRowCount", Integer.class, this.jniRasterAttributeTableInstance, new Class[]{}, new Object[]{});
    }

    /**
     * Calls the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method
     *
     * @param iRow the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method 'iRow' argument
     * @param iCol the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method 'iCol' argument
     * @return the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method result
     */
    public String getValueAsString(int iRow, int iCol) {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetValueAsString", String.class, this.jniRasterAttributeTableInstance, new Class[]{int.class, int.class}, new Object[]{iRow, iCol});
    }
}
