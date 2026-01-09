package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

abstract class GDALOptions {

    protected final Object jniInstance;

    public GDALOptions(Vector options) {
        this.jniInstance = GDALReflection.fetchGDALLibraryClassInstance(className(),
                                                                        new Class[] { Vector.class },
                                                                        new Object[] { options });
    }

    public GDALOptions(Object jniInstance) {
        this.jniInstance = jniInstance;
    }

    public Object getJniInstance() {
        return jniInstance;
    }

    protected abstract String className();
}
