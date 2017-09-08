package ro.cs.tao.datasource.remote.result;

import ro.cs.tao.eodata.EOData;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface ResponseParser<T extends EOData> {

    List<T> parse(String content) throws ParseException;

    default String[] getExcludedAttributes() { return null; }

}
