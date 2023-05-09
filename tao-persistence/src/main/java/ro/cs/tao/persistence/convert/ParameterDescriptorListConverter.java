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
package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.component.ParameterDescriptor;

import javax.persistence.AttributeConverter;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class ParameterDescriptorListConverter implements AttributeConverter<List<ParameterDescriptor>, String> {

    @Override
    public String convertToDatabaseColumn(List<ParameterDescriptor> parameters) {
        try {
            return new ObjectMapper().writerFor(parameters.getClass()).writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public List<ParameterDescriptor> convertToEntityAttribute(String s) {
        try {
            return new ObjectMapper().readerForListOf(ParameterDescriptor.class).readValue(s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
