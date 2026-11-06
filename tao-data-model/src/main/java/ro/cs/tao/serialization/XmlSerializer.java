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
package ro.cs.tao.serialization;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

/**
 * Serializer implementation for XML serialization and deserialization.
 * This class extends the {@code BaseSerializer} abstract class and provides
 * functionality specific to XML data format. It uses the Jackson XML module
 * to handle serialization and deserialization of objects to and from XML strings.
 *
 * @param <T> The type of objects to be serialized and deserialized.
 *
 * @author Cosmin Cara
 */
class XmlSerializer<T> extends BaseSerializer<T> {

    XmlSerializer(Class<T> tClass) {
        super(tClass);
    }

    @Override
    protected ObjectMapper createMapper() {
        XmlMapper.Builder builder = XmlMapper.builder()
                .defaultUseWrapper(false)
                .addModule(new JakartaXmlBindAnnotationModule());
        if (this.formatOutput) {
            builder.enable(SerializationFeature.INDENT_OUTPUT);
        }
        builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING);
        builder.enable(EnumFeature.READ_ENUMS_USING_TO_STRING);
        return builder.build();
    }
}
