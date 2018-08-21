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

package ro.cs.tao.eodata.metadata;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for XML metadata inspectors.
 *
 * @author Cosmin Cara
 */
public abstract class XmlMetadataInspector implements MetadataInspector {

    protected static DocumentBuilder builder;

    static {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public XmlMetadataInspector() { }

    protected String getValue(String tagName, Element element) {
        NodeList list = element.getElementsByTagName(tagName);
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

}
