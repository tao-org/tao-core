/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.datasource.common.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ro.cs.tao.eodata.EOData;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class XmlResponseParser {

    private static final Logger logger = Logger.getLogger(XmlResponseParser.class.getName());

    public static List<EOData> parse(String xmlString, XmlResponseHandler handler) {
        List<EOData> result = null;
        InputSource inputSource = new InputSource(new StringReader(xmlString));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, handler);
            result = handler.getResults();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.warning(e.getMessage());
        }
        return result;
    }
}
