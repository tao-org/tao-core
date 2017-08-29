package ro.cs.tao.datasource;

import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Cosmin Cara
 */
public interface ProductFetchStrategy {
    Path fetch(EOProduct product) throws IOException;
}
