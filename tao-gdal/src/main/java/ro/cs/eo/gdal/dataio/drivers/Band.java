package ro.cs.eo.gdal.dataio.drivers;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;

/**
 * GDAL Band JNI driver class
 *
 * @author Adrian Drăghici
 */
public class Band extends GDALBase implements Closeable {

    /**
     * The name of JNI GDAL Band class
     */
    static final String CLASS_NAME = "org.gdal.gdal.Band";
    private static final Class<?> bandClass;

    static {
        bandClass = GDALReflection.fetchGDALLibraryClass(CLASS_NAME);
    }

    private final Object jniBandInstance;
    private final MethodHandle deleteHandle;
    private final MethodHandle getDataTypeHandle;
    private final MethodHandle getBlockXSizeHandle;
    private final MethodHandle getBlockYSizeHandle;
    private final MethodHandle getXSizeHandle;
    private final MethodHandle getYSizeHandle;
    private final MethodHandle getOverviewCountHandle;
    private final MethodHandle getRasterColorInterpretationHandle;
    private final MethodHandle getRasterColorTableHandle;
    private final MethodHandle getDescriptionHandle;
    private final MethodHandle getOverviewHandle;
    private final MethodHandle getOffsetHandle;
    private final MethodHandle getScaleHandle;
    private final MethodHandle getUnitTypeHandle;
    private final MethodHandle getNoDataValueHandle;
    private final MethodHandle getMaskBandHandle;
    private final MethodHandle getMaskFlagsHandle;
    private final MethodHandle readBlockDirectHandle;
    private final MethodHandle readRasterDirectHandle;
    private final MethodHandle readRasterByteHandle;
    private final MethodHandle readRasterShortHandle;
    private final MethodHandle readRasterIntHandle;
    private final MethodHandle readRasterFloatHandle;
    private final MethodHandle readRasterDoubleHandle;
    private final MethodHandle writeRasterByteHandle;
    private final MethodHandle writeRasterShortHandle;
    private final MethodHandle writeRasterIntHandle;
    private final MethodHandle writeRasterFloatHandle;
    private final MethodHandle writeRasterDoubleHandle;
    private final MethodHandle writeRasterDirectHandle;

    /**
     * Creates new instance for this driver
     *
     * @param jniBandInstance the JNI GDAL Band class instance
     */
    public Band(Object jniBandInstance) {
        this.jniBandInstance = jniBandInstance;
        try {
            deleteHandle = createHandle(bandClass, "delete", void.class);
            getDataTypeHandle = createHandle(bandClass, "getDataType", int.class);
            getBlockXSizeHandle = createHandle(bandClass, "GetBlockXSize", int.class);
            getBlockYSizeHandle = createHandle(bandClass, "GetBlockYSize", int.class);
            getXSizeHandle = createHandle(bandClass, "GetXSize", int.class);
            getYSizeHandle = createHandle(bandClass, "GetYSize", int.class);
            getOverviewCountHandle = createHandle(bandClass, "GetOverviewCount", int.class);
            getRasterColorInterpretationHandle = createHandle(bandClass, "GetRasterColorInterpretation", int.class);
            getRasterColorTableHandle = createHandle(bandClass, "GetRasterColorTable", GDALReflection.fetchGDALLibraryClass("org.gdal.gdal.ColorTable"));
            getDescriptionHandle = createHandle(bandClass, "GetDescription", String.class);
            getOverviewHandle = createHandle(bandClass, "GetOverview", GDALReflection.fetchGDALLibraryClass("org.gdal.gdal.Band"), int.class);
            getOffsetHandle = createHandle(bandClass, "GetOffset", void.class, Double[].class);
            getScaleHandle = createHandle(bandClass, "GetScale", void.class, Double[].class);
            getUnitTypeHandle = createHandle(bandClass, "GetUnitType", String.class);
            getNoDataValueHandle = createHandle(bandClass, "GetNoDataValue", void.class, Double[].class);
            getMaskBandHandle = createHandle(bandClass, "GetMaskBand", GDALReflection.fetchGDALLibraryClass("org.gdal.gdal.Band"));
            getMaskFlagsHandle = createHandle(bandClass, "GetMaskFlags", int.class);
            readBlockDirectHandle = createHandle(bandClass, "ReadBlock_Direct", int.class, int.class, int.class, ByteBuffer.class);
            readRasterDirectHandle = createHandle(bandClass, "ReadRaster_Direct", int.class,
                                                  int.class, int.class, int.class, int.class, int.class, int.class, int.class, ByteBuffer.class);
            readRasterByteHandle = createHandle(bandClass, "ReadRaster", int.class,
                                                int.class, int.class, int.class, int.class, byte[].class);
            readRasterShortHandle = createHandle(bandClass, "ReadRaster", int.class,
                                                 int.class, int.class, int.class, int.class, short[].class);
            readRasterIntHandle = createHandle(bandClass, "ReadRaster", int.class,
                                               int.class, int.class, int.class, int.class, int[].class);
            readRasterFloatHandle = createHandle(bandClass, "ReadRaster", int.class,
                                                 int.class, int.class, int.class, int.class, float[].class);
            readRasterDoubleHandle = createHandle(bandClass, "ReadRaster", int.class,
                                                  int.class, int.class, int.class, int.class, double[].class);
            writeRasterByteHandle = createHandle(bandClass, "WriteRaster", int.class,
                                                 int.class, int.class, int.class, int.class, byte[].class);
            writeRasterShortHandle = createHandle(bandClass, "WriteRaster", int.class,
                                                  int.class, int.class, int.class, int.class, short[].class);
            writeRasterIntHandle = createHandle(bandClass, "WriteRaster", int.class,
                                                int.class, int.class, int.class, int.class, int[].class);
            writeRasterFloatHandle = createHandle(bandClass, "WriteRaster", int.class,
                                                  int.class, int.class, int.class, int.class, float[].class);
            writeRasterDoubleHandle = createHandle(bandClass, "WriteRaster", int.class,
                                                   int.class, int.class, int.class, int.class, double[].class);
            writeRasterDirectHandle = createHandle(bandClass, "WriteRaster_Direct", int.class,
                                                   int.class, int.class, int.class, int.class, int.class, ByteBuffer.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls the JNI GDAL Band class delete() method
     */
    public void delete() {
        invoke(deleteHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class getDataType() method
     *
     * @return the JNI GDAL Band class getDataType() method result
     */
    public Integer getDataType() {
        return (Integer) invoke(getDataTypeHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetBlockXSize() method
     *
     * @return the JNI GDAL Band class GetBlockXSize() method result
     */
    public Integer getBlockXSize() {
        return (Integer) invoke(getBlockXSizeHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetBlockYSize() method
     *
     * @return the JNI GDAL Band class GetBlockYSize() method result
     */
    public Integer getBlockYSize() {
        return (Integer) invoke(getBlockYSizeHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetXSize() method
     *
     * @return the JNI GDAL Band class GetXSize() method result
     */
    public Integer getXSize() {
        return (Integer) invoke(getXSizeHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetYSize() method
     *
     * @return the JNI GDAL Band class GetYSize() method result
     */
    public Integer getYSize() {
        return (Integer) invoke(getYSizeHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetOverviewCount() method
     *
     * @return the JNI GDAL Band class GetOverviewCount() method result
     */
    public Integer getOverviewCount() {
        return (Integer) invoke(getOverviewCountHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetRasterColorInterpretation() method
     *
     * @return the JNI GDAL Band class GetRasterColorInterpretation() method result
     */
    public Integer getRasterColorInterpretation() {
        return (Integer) invoke(getRasterColorInterpretationHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetRasterColorTable() method
     *
     * @return the JNI GDAL Band class GetRasterColorTable() method result
     */
    public ColorTable getRasterColorTable() {
        Object jniColorTable = invoke(getRasterColorTableHandle, this.jniBandInstance);
        return jniColorTable != null ? new ColorTable(jniColorTable) : null;
    }

    /**
     * Calls the JNI GDAL Band class GetDescription() method
     *
     * @return the JNI GDAL Band class GetDescription() method result
     */
    public String getDescription() {
        return (String) invoke(getDescriptionHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetOverview(int i) method
     *
     * @param i the JNI GDAL Band class GetOverview(int i) method 'i' argument
     * @return the JNI GDAL Band class GetOverview(int i) method result
     */
    public Band getOverview(int i) {
        Object newJNIBandInstance = invoke(getOverviewHandle, this.jniBandInstance, i);
        return newJNIBandInstance != null ? new Band(newJNIBandInstance) : null;
    }

    /**
     * Calls the JNI GDAL Band class GetOffset(Double[] val) method
     *
     * @param val the JNI GDAL Band class GetOffset(Double[] val) method 'val' argument
     */
    public void getOffset(Double[] val) {
        invoke(getOffsetHandle, this.jniBandInstance, (Object) val);
    }

    /**
     * Calls the JNI GDAL Band class GetScale(Double[] val) method
     *
     * @param val the JNI GDAL Band class GetScale(Double[] val) method 'val' argument
     */
    public void getScale(Double[] val) {
        invoke(getScaleHandle, this.jniBandInstance, (Object) val);
    }

    public void getMinimum(Double[] val) {
        GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetMinimum", null, this.jniBandInstance, new Class[]{Double[].class}, new Object[]{val});
    }

    public void getMaximum(Double[] val) {
        GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetMaximum", null, this.jniBandInstance, new Class[]{Double[].class}, new Object[]{val});
    }

    public void computeMinMax(double[] val) {
        GDALReflection.callGDALLibraryMethod(CLASS_NAME, "ComputeRasterMinMax", null, this.jniBandInstance, new Class[]{double[].class, int.class}, new Object[]{val, 1});
    }

    /**
     * Calls the JNI GDAL Band class GetUnitType() method
     *
     * @return the JNI GDAL Band class GetUnitType() method result
     */
    public String getUnitType() {
        return (String) invoke(getUnitTypeHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class GetNoDataValue(Double[] val) method
     *
     * @param val the JNI GDAL Band class GetNoDataValue(Double[] val) method 'val' argument
     */
    public void getNoDataValue(Double[] val) {
        invoke(getNoDataValueHandle, this.jniBandInstance, (Object) val);
    }

    /**
     * Calls the JNI GDAL Band class GetMaskBand() method
     *
     * @return the JNI GDAL Band class GetMaskBand() method result
     */
    public Band getMaskBand() {
        Object newJNIBandInstance = invoke(getMaskBandHandle, this.jniBandInstance);
        return newJNIBandInstance != null ? new Band(newJNIBandInstance) : null;
    }

    /**
     * Calls the JNI GDAL Band class GetMaskFlags() method
     *
     * @return the JNI GDAL Band class GetMaskFlags() method result
     */
    public Integer getMaskFlags() {
        return (Integer) invoke(getMaskFlagsHandle, this.jniBandInstance);
    }

    /**
     * Calls the JNI GDAL Band class ReadBlock_Direct(int nXBlockOff, int nYBlockOff, ByteBuffer nioBuffer) method
     *
     * @param nXBlockOff the JNI GDAL Band class ReadBlock_Direct(int nXBlockOff, int nYBlockOff, ByteBuffer nioBuffer) method 'nXBlockOff' argument
     * @param nYBlockOff the JNI GDAL Band class ReadBlock_Direct(int nXBlockOff, int nYBlockOff, ByteBuffer nioBuffer) method 'nYBlockOff' argument
     * @param nioBuffer  the JNI GDAL Band class ReadBlock_Direct(int nXBlockOff, int nYBlockOff, ByteBuffer nioBuffer) method 'nioBuffer' argument
     * @return the JNI GDAL Band class ReadBlock_Direct(int nXBlockOff, int nYBlockOff, ByteBuffer nioBuffer) method result
     */
    public Integer readBlockDirect(int nXBlockOff, int nYBlockOff, ByteBuffer nioBuffer) {
        return (Integer) invoke(readBlockDirectHandle, this.jniBandInstance, nXBlockOff, nYBlockOff, nioBuffer);
    }

    /**
     * Calls the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method
     *
     * @param xoff      the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method 'xoff' argument
     * @param yoff      the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method 'yoff' argument
     * @param xsize     the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method 'xsize' argument
     * @param ysize     the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method 'ysize' argument
     * @param bufxsize  the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method 'buf_xsize' argument
     * @param bufysize  the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method 'buf_ysize' argument
     * @param buftype   the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method 'buf_type' argument
     * @param nioBuffer the JNI GDAL Band class ReadBlock_Direct(int nXBlockOff, int nYBlockOff, ByteBuffer nioBuffer) method 'nioBuffer' argument
     * @return the JNI GDAL Band class ReadRaster_Direct(int xoff, int yoff, int xsize, int ysize, int buf_xsize, int buf_ysize, int buf_type, ByteBuffer nioBuffer) method result
     */
    public Integer readRasterDirect(int xoff, int yoff, int xsize, int ysize, int bufxsize, int bufysize, int buftype, ByteBuffer nioBuffer) {
        return (Integer) invoke(readRasterDirectHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, bufxsize, bufysize, buftype, nioBuffer);
    }

    public byte[] readRasterByte(int xoff, int yoff, int xsize, int ysize) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect((xsize - xoff) * (ysize - yoff));
        int retval = readRasterDirect(xoff, yoff, xsize, ysize, xsize - xoff, ysize - yoff, GDALConstConstants.gdtByte(), buffer);
        return buffer.array();
    }
    public short[] readRasterShort(int xoff, int yoff, int xsize, int ysize) {
        //return (Integer) invoke(readRasterShortHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, array);
        final int bufSize = (xsize - xoff) * (ysize - yoff);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bufSize * 2);
        int retval = readRasterDirect(xoff, yoff, xsize, ysize, xsize - xoff, ysize - yoff, GDALConstConstants.gdtUint16(), buffer);
        final short[] result = new short[bufSize];
        buffer.asShortBuffer().get(result);
        buffer.clear();
        return result;
    }
    public int[] readRasterInt(int xoff, int yoff, int xsize, int ysize) {
        //return (Integer) invoke(readRasterIntHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, array);
        final int bufSize = (xsize - xoff) * (ysize - yoff);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bufSize * 4);
        int retval = readRasterDirect(xoff, yoff, xsize, ysize, xsize - xoff, ysize - yoff, GDALConstConstants.gdtInt32(), buffer);
        final int[] result = new int[bufSize];
        buffer.asIntBuffer().get(result);
        buffer.clear();
        return result;
    }

    public float[] readRasterFloat(int xoff, int yoff, int xsize, int ysize) {
        //return (Integer) invoke(readRasterFloatHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, array);
        final int bufSize = (xsize - xoff) * (ysize - yoff);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bufSize * 4);
        int retval = readRasterDirect(xoff, yoff, xsize, ysize, xsize - xoff, ysize - yoff, GDALConstConstants.gdtFloat32(), buffer);
        final float[] result = new float[bufSize];
        buffer.asFloatBuffer().get(result);
        buffer.clear();
        return result;
    }

    public double[] readRasterDouble(int xoff, int yoff, int xsize, int ysize) {
        //return (Integer) invoke(readRasterDoubleHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, array);
        final int bufSize = (xsize - xoff) * (ysize - yoff);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bufSize * 8);
        int retval = readRasterDirect(xoff, yoff, xsize, ysize, xsize - xoff, ysize - yoff, GDALConstConstants.gdtFloat64(), buffer);
        final double[] result = new double[bufSize];
        buffer.asDoubleBuffer().get(result);
        buffer.clear();
        return result;
    }

    /**
     * Calls the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method
     *
     * @param xoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method 'xoff' argument
     * @param yoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method 'yoff' argument
     * @param xsize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method 'xsize' argument
     * @param ysize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method 'ysize' argument
     * @param bufType the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method 'bufType' argument
     * @param array   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method 'array' argument
     * @return the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) method result
     */
    public Integer writeRaster(int xoff, int yoff, int xsize, int ysize, int bufType, byte[] array) {
        return (Integer) invoke(writeRasterByteHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, bufType, array);
    }

    /**
     * Calls the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method
     *
     * @param xoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method 'xoff' argument
     * @param yoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method 'yoff' argument
     * @param xsize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method 'xsize' argument
     * @param ysize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method 'ysize' argument
     * @param bufType the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method 'bufType' argument
     * @param array   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method 'array' argument
     * @return the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) method result
     */
    public Integer writeRaster(int xoff, int yoff, int xsize, int ysize, int bufType, short[] array) {
        return (Integer) invoke(writeRasterShortHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, bufType, array);
    }

    /**
     * Calls the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method
     *
     * @param xoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method 'xoff' argument
     * @param yoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method 'yoff' argument
     * @param xsize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method 'xsize' argument
     * @param ysize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method 'ysize' argument
     * @param bufType the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method 'bufType' argument
     * @param array   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method 'array' argument
     * @return the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) method result
     */
    public Integer writeRaster(int xoff, int yoff, int xsize, int ysize, int bufType, int[] array) {
        return (Integer) invoke(writeRasterIntHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, bufType, array);
    }

    /**
     * Calls the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method
     *
     * @param xoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method 'xoff' argument
     * @param yoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method 'yoff' argument
     * @param xsize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method 'xsize' argument
     * @param ysize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method 'ysize' argument
     * @param bufType the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method 'bufType' argument
     * @param array   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method 'array' argument
     * @return the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) method result
     */
    public Integer writeRaster(int xoff, int yoff, int xsize, int ysize, int bufType, float[] array) {
        return (Integer) invoke(writeRasterFloatHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, bufType, array);
    }

    /**
     * Calls the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method
     *
     * @param xoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method 'xoff' argument
     * @param yoff    the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method 'yoff' argument
     * @param xsize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method 'xsize' argument
     * @param ysize   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method 'ysize' argument
     * @param bufType the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method 'bufType' argument
     * @param array   the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method 'array' argument
     * @return the JNI GDAL Band class WriteRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) method result
     */
    public Integer writeRaster(int xoff, int yoff, int xsize, int ysize, int bufType, double[] array) {
        return (Integer) invoke(writeRasterDoubleHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, bufType, array);
    }

    public Integer writeRasterDirect(int xoff, int yoff, int xsize, int ysize, int bufType, ByteBuffer buffer) {
        return (Integer) invoke(writeRasterDirectHandle, this.jniBandInstance, xoff, yoff, xsize, ysize, bufType, buffer);
    }

    /**
     * Calls the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method
     *
     * @param approxOk the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method 'approxOk' argument
     * @param force    the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method 'force' argument
     * @param min      the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method 'min' argument
     * @param max      the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method 'max' argument
     * @param mean     the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method 'mean' argument
     * @param stddev   the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method 'stddev' argument
     * @return the JNI GDAL Band class GetStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) method result
     */
    public Integer getStatistics(boolean approxOk, boolean force, double[] min, double[] max, double[] mean, double[] stddev) {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetStatistics", Integer.class, this.jniBandInstance, new Class[]{boolean.class, boolean.class, double[].class, double[].class, double[].class, double[].class}, new Object[]{approxOk, force, min, max, mean, stddev});
    }

    /**
     * Calls the JNI GDAL Band class GetHistogramGetHistogram(int[] buckets) method
     *
     * @param buckets the JNI GDAL Band class GetHistogramGetHistogram(int[] buckets)) method 'buckets' argument
     * @return the JNI GDAL Band class GetHistogramGetHistogram(int[] buckets) method result
     */
    public Integer getHistogram(int[] buckets) {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetHistogram", Integer.class, this.jniBandInstance, new Class[]{int[].class}, new Object[]{buckets});
    }

    /**
     * Calls the JNI GDAL Band class GetDefaultRAT() method
     *
     * @return the JNI GDAL Band class GetDefaultRAT() method result
     */
    public RasterAttributeTable getDefaultRAT() {
        Object jniRasterAttributeTableInstance = GDALReflection.callGDALLibraryMethod(CLASS_NAME, "GetDefaultRAT", Object.class, this.jniBandInstance, new Class[]{}, new Object[]{});
        if (jniRasterAttributeTableInstance != null) {
            return new RasterAttributeTable(jniRasterAttributeTableInstance);
        }
        return null;
    }

    /**
     * Calls the JNI GDAL Band class Checksum() method
     *
     * @return the JNI GDAL Band class Checksum() method result
     */
    public Integer checksum() {
        return GDALReflection.callGDALLibraryMethod(CLASS_NAME, "Checksum", Integer.class, this.jniBandInstance, new Class[]{}, new Object[]{});
    }

    @Override
    public void close() throws IOException {
        delete();
    }
}
