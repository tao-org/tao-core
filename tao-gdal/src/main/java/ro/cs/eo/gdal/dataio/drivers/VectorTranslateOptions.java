package ro.cs.eo.gdal.dataio.drivers;

import java.util.Vector;

/** Options for GDAL's VectorTranslate utility. */
public class VectorTranslateOptions extends GDALOptions {
    static final String CLASS_NAME = "org.gdal.gdal.VectorTranslateOptions";

    public VectorTranslateOptions(Vector<String> options) {
        super(options);
    }

    @Override
    protected String className() {
        return CLASS_NAME;
    }
}
