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

import ro.cs.tao.topology.ServiceStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for TemplateType enum stored values
 *
 * @author Oana H.
 */
public class ServiceStatusConverter implements AttributeConverter<ServiceStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceStatus attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public ServiceStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null ? ServiceStatus.valueOf(ServiceStatus.getEnumConstantNameByValue(dbData)) : null;
    }
}
