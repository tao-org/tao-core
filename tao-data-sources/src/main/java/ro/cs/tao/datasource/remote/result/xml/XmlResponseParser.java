package ro.cs.tao.datasource.remote.result.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ro.cs.tao.datasource.remote.result.ParseException;
import ro.cs.tao.datasource.remote.result.ResponseParser;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class XmlResponseParser<T> implements ResponseParser<T> {

    private XmlResponseHandler handler;

    public void setHandler(XmlResponseHandler handler) { this.handler = handler; }

    @Override
    public List<T> parse(String content) throws ParseException {
        if (this.handler == null) {
            throw new ParseException("Handler not defined");
        }
        List<T> result;
        InputSource inputSource = new InputSource(new StringReader(content));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, this.handler);
            result = this.handler.getResults();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ParseException(e.getMessage());
        }
        return result;
    }
}
