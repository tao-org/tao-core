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

import javax.persistence.AttributeConverter;

/**
 * Converter for <code>Class<?></code> stored values
 *
 */
public class DataTypeConverter implements AttributeConverter<Class<?>, String> {

    @Override
    public String convertToDatabaseColumn(Class<?> attribute) {
        return attribute != null ? attribute.getCanonicalName() : null;
    }

    @Override
    public Class<?> convertToEntityAttribute(String dbData) {
        Class<?> clasz = null;
        try {
            if (dbData != null) {
                if (!dbData.endsWith("[]")) {
                    clasz = ClassLoader.getSystemClassLoader().loadClass(dbData);
                } else {
                    switch (dbData) {
                        case "java.lang.Integer[]":
                            clasz = Integer[].class;
                            break;
                        case "java.lang.Float[]":
                            clasz = Float[].class;
                            break;
                        case "java.lang.Boolean[]":
                            clasz = Boolean[].class;
                            break;
                        case "java.lang.String[]":
                        default:
                            clasz = String[].class;
                            break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clasz;
    }
}
