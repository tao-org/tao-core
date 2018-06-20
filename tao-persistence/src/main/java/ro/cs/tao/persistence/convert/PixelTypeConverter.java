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
package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.enums.PixelType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for PixelType enum stored values
 *
 * @author Oana H.
 */
public class PixelTypeConverter implements AttributeConverter<PixelType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PixelType attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public PixelType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? PixelType.valueOf(PixelType.getEnumConstantNameByValue(dbData)) : null;
    }
}
