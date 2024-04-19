package ro.cs.eo.gdal.dataio.drivers;

/**
 * GDAL CoordinateTransformation JNI driver class
 *
 * @author Adrian Drăghici
 */
public class CoordinateTransformation {

    /**
     * The name of JNI GDAL CoordinateTransformation class
     */
    private static final String CLASS_NAME = "org.gdal.osr.CoordinateTransformation";

    private final Object jniCoordinateTransformationInstance;

    /**
     * Creates new instance for this driver
     *
     * @param jniCoordinateTransformationInstance the JNI GDAL CoordinateTransformation class instance
     */
    CoordinateTransformation(Object jniCoordinateTransformationInstance) {
        this.jniCoordinateTransformationInstance = jniCoordinateTransformationInstance;
    }

    /**
     * Calls the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) method
     *
     * @param src the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) 'src' argument
     * @param dst the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) method 'dst' argument
     * @return the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) method result
     */
    public static CoordinateTransformation createCoordinateTransformation(SpatialReference src, SpatialReference dst) {
        Object jniCoordinateTransformationInstance = GDALReflection.callGDALLibraryMethod(CLASS_NAME, "CreateCoordinateTransformation", Object.class, null, new Class[]{src.getJniSpatialReferenceInstance().getClass(), dst.getJniSpatialReferenceInstance().getClass()}, new Object[]{src.getJniSpatialReferenceInstance(), dst.getJniSpatialReferenceInstance()});
        if (jniCoordinateTransformationInstance != null) {
            return new CoordinateTransformation(jniCoordinateTransformationInstance);
        }
        return null;
    }

    /**
     * Calls the JNI GDAL CoordinateTransformation class TransformPoint(double x, double y) method
     *
     * @param x the JNI GDAL CoordinateTransformation class TransformPoint(double x, double y) method 'x' argument
     * @param y the JNI GDAL CoordinateTransformation class TransformPoint(double x, double y) method 'y' argument
     * @return the JNI GDAL CoordinateTransformation class TransformPoint(double x, double y) method result
     */
    public double[] transformPoint(double x, double y) {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "TransformPoint", double[].class, this.jniCoordinateTransformationInstance, new Class[]{double.class, double.class}, new Object[]{x, y});
    }
}
