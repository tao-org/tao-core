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

package ro.cs.tao.eodata.metadata;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base class for XML metadata inspectors.
 *
 * @author Cosmin Cara
 */
public abstract class XmlMetadataInspector implements MetadataInspector {

    private Document xmlDocument;
    private XPath xPath;
    protected Logger logger = Logger.getLogger(getClass().getName());

    public XmlMetadataInspector() { }

    protected void readDocument(Path documentPath) throws ParserConfigurationException, IOException, SAXException, XMLStreamException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (InputStream inputStream = Files.newInputStream(documentPath)) {
            this.xmlDocument = builder.parse(inputStream);
        }
        this.xPath = XPathFactory.newInstance().newXPath();
        try (InputStream inputStream = Files.newInputStream(documentPath)) {
            XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
            while (reader.hasNext()) {
                XMLEvent xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    this.xPath.setNamespaceContext(((StartElement) xmlEvent).getNamespaceContext());
                    break;
                }
            }
        }
    }

    protected String getValue(String xPathExpression) {
        String value = null;
        if (this.xPath != null) {
            try {
                value = (String) this.xPath.compile(xPathExpression).evaluate(this.xmlDocument, XPathConstants.STRING);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    protected List<String> getValues(String xPathExpression) {
        List<String> values = null;
        if (this.xPath != null) {
            try {
                NodeList nodes = (NodeList) this.xPath.compile(xPathExpression).evaluate(this.xmlDocument, XPathConstants.NODESET);
                if (nodes != null) {
                    values = new ArrayList<>();
                    final int length = nodes.getLength();
                    for (int i = 0; i < length; i++) {
                        values.add(nodes.item(i).getNodeValue());
                    }
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
        return values;
    }

    /*protected String getValue(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }
        return null;
    }

    protected String getValue(Element element, String parentTagName, String tagName) {
        NodeList list = element.getElementsByTagName(parentTagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
                for (int i = 0; i < subList.getLength(); i++) {
                    final Node node = subList.item(i);
                    if (tagName.equals(node.getNodeName())) {
                        return node.getNodeValue();
                    }
                }
            }
        }
        return null;
    }

    protected String getValue(Element element, String parentTagName, String parentAttribute, String attributeValue, String tagName) {
        NodeList list = element.getElementsByTagName(parentTagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
                for (int i = 0; i < subList.getLength(); i++) {
                    final Node node = subList.item(i);
                    NamedNodeMap attributes = ((Element) subList).getAttributes();
                    if (attributes != null) {
                        Node namedItem = attributes.getNamedItem(parentAttribute);
                        if (namedItem != null && attributeValue.equals(namedItem.getNodeValue())) {
                            NodeList childNodes = node.getChildNodes();
                            for (int j = 0; i < childNodes.getLength(); j++) {
                                if (tagName.equals(childNodes.item(j).getNodeName())) {
                                    return node.getNodeValue();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    protected String getAttributeValue(String tagName, String attribute, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null) {
                NamedNodeMap attributes = ((Element) subList).getAttributes();
                return attributes.getNamedItem(attribute).getNodeValue();
            }
        }
        return null;
    }

    protected String getValue(String namespace, String tagName, Element element) {
        NodeList list = element.getElementsByTagNameNS(namespace, tagName);
        if (list != null && list.getLength() > 0) {
            NodeList subList = list.item(0).getChildNodes();
            if (subList != null && subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }
        return null;
    }

    protected List<String> getValues(String tagName, Element element) {
        List<String> values = null;
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            values = new ArrayList<>();
            final int length = list.getLength();
            for (int i = 0; i < length; i++) {
                values.add(list.item(i).getNodeValue());
            }
        }
        return values;
    }

    protected List<String> getValues(String namespace, String tagName, Element element) {
        List<String> values = null;
        NodeList list = element.getElementsByTagNameNS(namespace, tagName);
        if (list != null && list.getLength() > 0) {
            values = new ArrayList<>();
            final int length = list.getLength();
            for (int i = 0; i < length; i++) {
                values.add(list.item(i).getNodeValue());
            }
        }
        return values;
    }

    protected List<String> getAttributeValues(String tagName, String attrName, Element element) {
        List<String> values = null;
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            values = new ArrayList<>();
            final int length = list.getLength();
            for (int i = 0; i < length; i++) {
                Node node = list.item(i);
                NamedNodeMap attributes = node.getAttributes();
                Node item = attributes.getNamedItem(attrName);
                if (item != null) {
                    values.add(item.getNodeValue());
                }
            }
        }
        return values;
    }

    protected List<String> getAttributeValues(String namespace, String tagName, String attrName, Element element) {
        List<String> values = null;
        NodeList list = element.getElementsByTagNameNS(namespace, tagName);
        if (list != null && list.getLength() > 0) {
            values = new ArrayList<>();
            final int length = list.getLength();
            for (int i = 0; i < length; i++) {
                Node node = list.item(i);
                NamedNodeMap attributes = node.getAttributes();
                Node item = attributes.getNamedItem(attrName);
                if (item != null) {
                    values.add(item.getNodeValue());
                }
            }
        }
        return values;
    }*/

}
