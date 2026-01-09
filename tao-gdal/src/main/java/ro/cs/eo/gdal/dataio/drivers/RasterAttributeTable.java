package ro.cs.eo.gdal.dataio.drivers;

import java.lang.invoke.MethodHandle;

public class RasterAttributeTable extends InstantiableGDALBase {

    /**
     * The name of JNI GDAL RasterAttributeTable class
     */
    private static final String CLASS_NAME = "org.gdal.gdal.RasterAttributeTable";
    private static final Class<?> JNI_CLASS;

    private final MethodHandle getColumnCountHandle;
    private final MethodHandle getRowCountHandle;
    private final MethodHandle getValueAsStringHandle;

    static {
        JNI_CLASS = GDALReflection.fetchGDALLibraryClass(CLASS_NAME);
    }

    /**
     * Creates new instance for this driver
     *
     * @param jniRasterAttributeTableInstance the JNI GDAL RasterAttributeTable class instance
     */
    public RasterAttributeTable(Object jniRasterAttributeTableInstance) {
        super(jniRasterAttributeTableInstance);
        try {
            this.getColumnCountHandle = createHandle(JNI_CLASS, "GetColumnCount", int.class);
            this.getRowCountHandle = createHandle(JNI_CLASS, "GetRowCount", int.class);
            this.getValueAsStringHandle = createHandle(JNI_CLASS, "GetValueAsString", String.class, int.class, int.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls the JNI GDAL RasterAttributeTable class GetColumnCount() method
     *
     * @return the JNI GDAL RasterAttributeTable class GetColumnCount() method result
     */
    public Integer getColumnCount() {
        return (Integer) invoke(getColumnCountHandle, this.jniInstance);
    }

    /**
     * Calls the JNI GDAL RasterAttributeTable class GetRowCount() method
     *
     * @return the JNI GDAL RasterAttributeTable class GetRowCount() method result
     */
    public Integer getRowCount() {
        return (Integer) invoke(getRowCountHandle, this.jniInstance);
    }

    /**
     * Calls the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method
     *
     * @param iRow the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method 'iRow' argument
     * @param iCol the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method 'iCol' argument
     * @return the JNI GDAL RasterAttributeTable class GetValueAsString(int iRow, int iCol) method result
     */
    public String getValueAsString(int iRow, int iCol) {
        return (String) invoke(getValueAsStringHandle, this.jniInstance, iRow, iCol);
    }
}
