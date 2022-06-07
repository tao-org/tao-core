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

import ro.cs.tao.datasource.param.JavaType;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

/**
 * @author Cosmin Cara
 */
public class GenericAdapter extends XmlAdapter<Object, String> {
    private Class clazz;

    public GenericAdapter(String className) {
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Maybe it is a primitive type
            JavaType javaType = JavaType.fromFriendlyName(className);
            if (javaType != null) {
                clazz = javaType.value();
            } else {
                throw new IllegalArgumentException("No such class");
            }
        }
    }

    @Override
    public String unmarshal(Object v) throws Exception {
        return !clazz.isAssignableFrom(LocalDateTime.class) ? String.valueOf(v) : new DateAdapter().marshal((LocalDateTime) v);
    }

    @Override
    public Object marshal(String v) throws Exception {
        if (clazz.isAssignableFrom(String.class)) {
            return v;
        } else  if (clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(byte.class)) {
            return Byte.parseByte(v);
        } else if (clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class)) {
            return Short.parseShort(v);
        } else if (clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class)) {
            return Integer.parseInt(v);
        } else if (clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class)) {
            return Long.parseLong(v);
        } else if (clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class)) {
            return Float.parseFloat(v);
        } else if (clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class)) {
            return Double.parseDouble(v);
        } else if (clazz.isAssignableFrom(LocalDateTime.class)) {
            return new DateAdapter().unmarshal(v);
        } else {
            return null;
        }
    }
}
