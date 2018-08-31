/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.topology.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ro.cs.tao.topology.ToolInstallConfig;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Udroiu
 */
public class ToolInstallersConfigParser {
    private static final Logger logger = Logger.getLogger(ToolInstallersConfigHandler.class.getName());

    public static List<ToolInstallConfig> parse(InputStream is, ToolInstallersConfigHandler handler) {
        List<ToolInstallConfig> result = null;
        try {
            InputSource inputSource = new InputSource(is);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, handler);
            result = handler.getResults();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.warning(e.getMessage());
        }
        return result;

    }
    public static List<ToolInstallConfig> parse(String xmlString, ToolInstallersConfigHandler handler) {
        List<ToolInstallConfig> result = null;
        try {
            result = parse(new FileInputStream(xmlString), handler);
        } catch (IOException e) {
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
