/*
 * Copyright (C) 2017 CS ROMANIA
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

package ro.cs.tao.datasource.opensearch.model;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.remote.result.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DescriptionParser {

    public OpenSearchService parse(String content) throws ParseException {
        Handler handler = new Handler();
        OpenSearchService result;
        InputSource inputSource = new InputSource(new StringReader(content));
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(inputSource, handler);
            result = handler.getResult();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ParseException(e.getMessage());
        }
        return result;
    }

    private class Handler extends DefaultHandler {
        private OpenSearchService service;
        private OpenSearchEndpoint currentEndpoint;
        private String currentParameter;
        private List<String> currentOptions;
        private StringBuilder buffer;

        public OpenSearchService getResult() { return service; }

        @Override
        public void startDocument() throws SAXException {
            this.service = new OpenSearchService();
            this.buffer = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.indexOf(":") > 0) {
                qName = qName.substring(qName.indexOf(":") + 1);
            }
            buffer.setLength(0);
            switch (qName) {
                case "Url":
                    this.currentEndpoint = new OpenSearchEndpoint();
                    this.currentEndpoint.setType(attributes.getValue("type"));
                    String url = attributes.getValue("template");
                    url = url.substring(0, url.indexOf("?"));
                    this.currentEndpoint.setUrl(url);
                    break;
                case "Parameter":
                    this.currentParameter = attributes.getValue("name");
                    break;
                case "Option":
                    if (this.currentOptions == null) {
                        this.currentOptions = new ArrayList<>();
                    }
                    this.currentOptions.add(attributes.getValue("value"));
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.indexOf(":") > 0) {
                qName = qName.substring(qName.indexOf(":") + 1);
            }
            switch (qName) {
                case "ShortName":
                    this.service.setShortName(buffer.toString());
                    break;
                case "Description":
                    this.service.setDescription(buffer.toString());
                    break;
                case "Tags":
                    this.service.setTags(buffer.toString());
                    break;
                case "Url":
                    if (this.currentEndpoint != null) {
                        this.service.addEndpoint(this.currentEndpoint);
                    }
                    break;
                case "Parameter":
                    if (this.currentOptions != null) {
                        this.currentEndpoint.addParameter(
                                new ParameterDescriptor(this.currentParameter,
                                                        String.class, null, false,
                                                        this.currentOptions.toArray(new Object[0])));
                        this.currentOptions = null;
                    }
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            buffer.append(new String(ch, start, length).replace("\n", ""));
        }
    }

}
