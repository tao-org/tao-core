package ro.cs.eo.gdal.dataio.drivers;

import java.lang.invoke.MethodHandle;

/**
 * GDAL CoordinateTransformation JNI driver class
 *
 * @author Adrian Drăghici
 */
public class CoordinateTransformation extends InstantiableGDALBase {

    /**
     * The name of JNI GDAL CoordinateTransformation class
     */
    private static final String CLASS_NAME = "org.gdal.osr.CoordinateTransformation";
    private static final Class<?> JNI_CLASS;

    static {
        JNI_CLASS = GDALReflection.fetchGDALLibraryClass(CLASS_NAME);
        try {
            createCoordinateTransformationHandle = createStaticHandle(JNI_CLASS, "CreateCoordinateTransformation", JNI_CLASS, SpatialReference.JNI_CLASS, SpatialReference.JNI_CLASS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle createCoordinateTransformationHandle;
    private final MethodHandle transformPointHandle;

    /**
     * Creates new instance for this driver
     *
     * @param jniCoordinateTransformationInstance the JNI GDAL CoordinateTransformation class instance
     */
    CoordinateTransformation(Object jniCoordinateTransformationInstance) {
        super(jniCoordinateTransformationInstance);
        try {
            transformPointHandle = createHandle(JNI_CLASS, "TransformPoint", double[].class, double.class, double.class);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) method
     *
     * @param src the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) 'src' argument
     * @param dst the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) method 'dst' argument
     * @return the JNI GDAL CoordinateTransformation class CreateCoordinateTransformation(SpatialReference src, SpatialReference dst) method result
     */
    public static CoordinateTransformation createCoordinateTransformation(SpatialReference src, SpatialReference dst) {
        Object jniInstance = invokeStatic(createCoordinateTransformationHandle, src.getJniInstance(), dst.getJniInstance());
        if (jniInstance != null) {
            return new CoordinateTransformation(jniInstance);
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
        return (double[]) this.invoke(transformPointHandle, this.jniInstance, x, y);
    }

    public void delete() {
        close();
    }

}
