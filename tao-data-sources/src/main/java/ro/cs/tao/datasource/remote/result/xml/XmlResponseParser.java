package ro.cs.tao.datasource.remote.result.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ro.cs.tao.datasource.remote.result.ParseException;
import ro.cs.tao.datasource.remote.result.ResponseParser;
import ro.cs.tao.eodata.EOProduct;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class XmlResponseParser implements ResponseParser<EOProduct> {

    private XmlResponseHandler handler;

    public void setHandler(XmlResponseHandler handler) { this.handler = handler; }

    @Override
    public List<EOProduct> parse(String content) throws ParseException {
        if (this.handler == null) {
            throw new ParseException("Handler not defined");
        }
        List<EOProduct> result;
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
