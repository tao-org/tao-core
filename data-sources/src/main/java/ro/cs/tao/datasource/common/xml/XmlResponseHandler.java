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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public abstract class XmlResponseHandler extends DefaultHandler {
    private List<EOData> results;
    protected StringBuilder buffer;
    protected String recordElement;
    protected EOProduct current;
    protected Logger logger = Logger.getLogger(XmlResponseHandler.class.getName());

    public XmlResponseHandler(String recordElementName) {
        super();
        this.recordElement = recordElementName;
    }

    List<EOData> getResults() {
        return results;
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            results = new ArrayList<>();
            buffer = new StringBuilder();
        } catch (Exception e) {
            logger.severe(e.getMessage());
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

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.indexOf(":") > 0) {
            qName = qName.substring(qName.indexOf(":") + 1);
        }
        buffer.setLength(0);
        if (this.recordElement.equals(qName)) {
            this.current = new EOProduct();
        }
        handleStartElement(qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.indexOf(":") > 0) {
            qName = qName.substring(qName.indexOf(":") + 1);
        }
        handleEndElement(qName);
        if (this.recordElement.equals(qName)) {
            if (this.current != null) {
                this.results.add(this.current);
            }
        }
        buffer.setLength(0);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        String error = e.getMessage();
        if (!error.contains("no grammar found")) {
            logger.warning(error);
        }
    }

    protected abstract void handleStartElement(String qName, Attributes attributes);

    protected abstract void handleEndElement(String qName);
}
