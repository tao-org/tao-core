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
package ro.cs.tao.datasource.remote.result.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.enums.DataFormat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base class for XML handlers.
 *
 * @author Cosmin Cara
 */
public abstract class XmlResponseHandler<T> extends DefaultHandler {
    private Class<T> resultClass;
    private List<T> results;
    private long resultCount;
    protected StringBuilder buffer;
    protected String recordElement;
    protected String countElement;
    protected T current;
    protected Logger logger = Logger.getLogger(XmlResponseHandler.class.getName());

    /**
     * Creates a new XML handler that can produce records of the given class.
     *
     * @param resultClass   The type of the result rows (or records).
     * @param recordElementName     The XML element that delimits records in the source XML.
     */
    public XmlResponseHandler(Class<T> resultClass, String recordElementName) {
        super();
        this.resultClass = resultClass;
        this.recordElement = recordElementName;
        this.resultCount = -1;
    }

    /**
     * Returns the list of results of the parsing operation.
     */
    List<T> getResults() {
        return results;
    }

    long getCount() { return countElement != null ? resultCount : -1; }

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
    public void characters(char[] ch, int start, int length) throws SAXException {
        buffer.append(new String(ch, start, length).replace("\n", ""));
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.indexOf(":") > 0) {
            qName = qName.substring(qName.indexOf(":") + 1);
        }
        buffer.setLength(0);
        if (this.recordElement.equals(qName)) {
            try {
                if (!Number.class.isAssignableFrom(this.resultClass)) {
                    this.current = this.resultClass.newInstance();
                } else {
                    this.current = this.resultClass.getConstructor(String.class).newInstance("0");
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new SAXException(e.getMessage());
            }
            if (EOData.class.isAssignableFrom(this.current.getClass())) {
                ((EOData) this.current).setFormatType(DataFormat.RASTER);
            }
        }
        handleStartElement(qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.indexOf(":") > 0) {
            qName = qName.substring(qName.indexOf(":") + 1);
        }
        if (this.countElement != null && this.countElement.equals(qName)) {
            this.resultCount = Long.parseLong(buffer.toString());
        } else {
            handleEndElement(qName);
            if (this.recordElement.equals(qName)) {
                if (this.current != null) {
                    this.results.add(this.current);
                }
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

    /**
     * Handles the start of an element.
     * @param qName     The element name, without any namespace.
     * @param attributes    The attributes collection of the element.
     */
    protected abstract void handleStartElement(String qName, Attributes attributes);

    /**
     * Handles the end of an element.
     * @param qName     The element name, without any namespace.
     */
    protected abstract void handleEndElement(String qName);
}
