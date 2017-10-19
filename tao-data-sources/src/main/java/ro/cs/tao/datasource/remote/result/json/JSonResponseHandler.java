package ro.cs.tao.datasource.remote.result.json;

import ro.cs.tao.datasource.remote.result.filters.AttributeFilter;
import ro.cs.tao.eodata.EOData;

import java.io.IOException;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface JSonResponseHandler<T extends EOData> {
    List<T> readValues(String content, AttributeFilter...filters) throws IOException;
}
