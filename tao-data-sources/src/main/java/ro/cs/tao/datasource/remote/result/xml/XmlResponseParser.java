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
 * Generic (base) parser for XML responses.
 *
 * @author Cosmin Cara
 */
public class XmlResponseParser<T> implements ResponseParser<T> {

    private XmlResponseHandler handler;

    /**
     * Assigns to this parser a specialized handler for the actual parsing operation.
     * @param handler   The XML handler
     */
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

    @Override
    public T parseValue(String content) throws ParseException {
        if (this.handler == null) {
            throw new ParseException("Handler not defined");
        }
        T result;
        InputSource inputSource = new InputSource(new StringReader(content));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, this.handler);
            List<T> results = this.handler.getResults();
            result = !results.isEmpty() ? results.get(0) : null;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ParseException(e.getMessage());
        }
        return result;
    }

    @Override
    public long parseCount(String content) throws ParseException {
        if (this.handler == null) {
            throw new ParseException("Handler not defined");
        }
        long result;
        InputSource inputSource = new InputSource(new StringReader(content));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, this.handler);
            result = this.handler.getCount();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ParseException(e.getMessage());
        }
        return result;
    }
}
