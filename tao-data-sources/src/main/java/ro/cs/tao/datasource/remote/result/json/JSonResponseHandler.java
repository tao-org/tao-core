package ro.cs.tao.datasource.remote.result.json;

import ro.cs.tao.datasource.remote.result.filters.AttributeFilter;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface JSonResponseHandler {
    List<EOProduct> readValues(String content, AttributeFilter...filters) throws IOException;
}
