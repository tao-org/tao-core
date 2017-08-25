package ro.cs.tao.datasource.remote.result.json;

import ro.cs.tao.eodata.EOData;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class JsonResponseParser<T extends EOData> {
    private static final Logger logger = Logger.getLogger(JsonResponseParser.class.getName());

    public List<T> parse(String content, JSonResponseHandler<T> handler) {
        List<T> result = null;
        try {
            result = handler.readValues(content);
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        return result;
    }

}
