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

import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.enums.ProcessingComponentType;

import javax.persistence.AttributeConverter;

public class ProcessingComponentTypeConverter implements AttributeConverter<ProcessingComponentType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ProcessingComponentType attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public ProcessingComponentType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? EnumUtils.getEnumConstantByValue(ProcessingComponentType.class, dbData) : null;
    }
}