package ro.cs.tao.topology.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ro.cs.tao.topology.ToolInstallConfig;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by cosmin on 8/8/2017.
 */
public class ToolInstallersConfigParser {
    private static final Logger logger = Logger.getLogger(ToolInstallersConfigHandler.class.getName());

    public static List<ToolInstallConfig> parse(String xmlString, ToolInstallersConfigHandler handler) {
        List<ToolInstallConfig> result = null;
        try {
            InputSource inputSource = new InputSource(new FileReader(xmlString));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, handler);
            result = handler.getResults();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.warning(e.getMessage());
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        List<ToolInstallConfig> tmpResults = ToolInstallersConfigParser.parse("c:\\temp\\DefaultToolInstallConfig.xml",
                new ToolInstallersConfigHandler("tool_install_configurations"));

    }
}
