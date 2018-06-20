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

import ro.cs.tao.component.enums.ProcessingComponentVisibility;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for ProcessingComponentVisibility enum stored values
 *
 * @author Oana H.
 */
public class ProcessingComponentVisibilityConverter implements AttributeConverter<ProcessingComponentVisibility, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProcessingComponentVisibility attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public ProcessingComponentVisibility convertToEntityAttribute(Integer dbData) {
        return dbData != null ? ProcessingComponentVisibility.valueOf(ProcessingComponentVisibility.getEnumConstantNameByValue(dbData)) : null;
    }
}
