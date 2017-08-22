package ro.cs.tao.datasource.remote.aws.internal;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Parser for AWS XML responses
 *
 * @author  Cosmin Cara
 */
public class IntermediateParser {

    public static AwsResult parse(String text) {
        AwsResult result = null;
        try (InputStream inputStream = new ByteArrayInputStream(text.getBytes())) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            Handler handler = new Handler();
            parser.parse(inputStream, handler);
            result = handler.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static class Handler extends DefaultHandler {
        private AwsResult result;
        private boolean isCollection;
        private StringBuilder buffer;

        AwsResult getResult() {
            return result;
        }

        @Override
        public void startDocument() throws SAXException {
            try {
                result = new AwsResult();
                buffer = new StringBuilder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            buffer.append(new String(ch, start, length).replace("\n", ""));
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.indexOf(":") > 0) {
                qName = qName.substring(qName.indexOf(":") + 1);
            }
            if ("CommonPrefixes".equals(qName)) {
                isCollection = true;
            }
            buffer.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.indexOf(":") > 0) {
                qName = qName.substring(qName.indexOf(":") + 1);
            }
            switch (qName) {
                case "Name":
                    result.setName(buffer.toString());
                    break;
                case "Prefix":
                    if (isCollection) {
                        result.addPrefix(buffer.toString());
                    } else {
                        result.setPrefix(buffer.toString());
                    }
                    break;
                case "Marker":
                    result.setMarker(buffer.toString());
                    break;
                case "MaxKeys":
                    result.setMaxKeys(Integer.parseInt(buffer.toString()));
                    break;
                case "Delimiter":
                    result.setDelimiter(buffer.toString());
                    break;
                case "IsTruncated":
                    result.setTruncated(Boolean.parseBoolean(buffer.toString()));
                    break;
            }
            buffer.setLength(0);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            String error = e.getMessage();
            if (!error.contains("no grammar found")) {
                e.printStackTrace();
            }
        }
    }
}