package ro.cs.tao.datasource.remote.result;

import ro.cs.tao.eodata.EOData;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface ResponseParser {

    List<EOData> parse(String content) throws ParseException;

}
