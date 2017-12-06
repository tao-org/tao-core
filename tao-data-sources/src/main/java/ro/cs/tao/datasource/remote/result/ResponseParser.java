package ro.cs.tao.datasource.remote.result;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface ResponseParser<T> {

    List<T> parse(String content) throws ParseException;

    default String[] getExcludedAttributes() { return null; }

}
