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

package ro.cs.tao.datasource.remote.scihub;

import org.xml.sax.Attributes;
import ro.cs.tao.datasource.common.xml.XmlResponseHandler;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.eodata.serialization.DateAdapter;
import ro.cs.tao.eodata.serialization.GeometryAdapter;

import java.net.URISyntaxException;

/**
 * @author Cosmin Cara
 */
public class SciHubResponseHandler extends XmlResponseHandler {

    private String identifiedElement;

    SciHubResponseHandler(String recordElementName) {
        super(recordElementName);
    }

    @Override
    protected void handleStartElement(String qName, Attributes attributes) {
        if (this.recordElement.equals(qName)) {
            this.current.setSensorType(SensorType.OPTICAL);
            this.current.setPixelType(PixelType.UINT16);
            this.current.setWidth(-1);
            this.current.setHeight(-1);
        } else {
            final String attributeValue = attributes.getValue(0);
            switch (qName) {
                case "str":
                case "double":
                case "date":
                case "int":
                case "link":
                    this.identifiedElement = attributeValue;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void handleEndElement(String qName) {
        final String elementValue = buffer.toString();
        switch (qName) {
            case "id":
                if (this.current != null) {
                    this.current.setId(elementValue);
                }
                break;
            case "str":
                try {
                    switch (this.identifiedElement) {
                        case "identifier":
                            this.current.setName(elementValue);
                            break;
                        case "footprint":
                            this.current.setGeometry(new GeometryAdapter().marshal(elementValue));
                            break;
                        default:
                            this.current.addAttribute(this.identifiedElement, elementValue);
                            break;
                    }
                } catch (Exception e) {
                    logger.warning(e.getMessage());
                }
                break;
            case "double":
                try {
                    switch (this.identifiedElement) {
                        case "cloudcoverpercentage":
                            this.current.addAttribute(this.identifiedElement, elementValue);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    logger.warning(e.getMessage());
                }
                break;
            case "date":
                try {
                    switch (this.identifiedElement) {
                        case "beginPosition":
                            this.current.setAcquisitionDate(new DateAdapter().unmarshal(elementValue));
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    logger.warning(e.getMessage());
                }
                break;
            case "link":
                if (this.current != null && this.current.getLocation() == null) {
                    try {
                        this.current.setLocation(this.identifiedElement);
                    } catch (URISyntaxException e) {
                        logger.warning(e.getMessage());
                    }
                }
                break;
            default:
                break;
        }
    }
}
