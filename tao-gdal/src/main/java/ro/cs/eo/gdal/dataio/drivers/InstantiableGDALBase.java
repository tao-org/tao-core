package ro.cs.eo.gdal.dataio.drivers;

import java.io.Closeable;
import java.lang.invoke.MethodHandle;

public abstract class InstantiableGDALBase extends GDALBase implements Closeable {

    protected final MethodHandle deleteHandle;

    public InstantiableGDALBase(Object jniInstance) {
        super(jniInstance);
        try {
            this.deleteHandle = createHandle(this.getJniInstance().getClass(), "delete", void.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        invoke(deleteHandle, this.jniInstance);
    }
}
