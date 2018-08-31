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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

/**
 * @author Cosmin Cara
 */
public class GenericAdapter extends XmlAdapter<Object, String> {
    private Class clazz;

    public GenericAdapter(String className) {
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("No such class");
        }
    }

    @Override
    public String unmarshal(Object v) throws Exception {
        return !clazz.isAssignableFrom(Date.class) ? String.valueOf(v) : new DateAdapter().marshal((Date) v);
    }

    @Override
    public Object marshal(String v) throws Exception {
        if (clazz.isAssignableFrom(String.class)) {
            return v;
        } else  if (clazz.isAssignableFrom(Byte.class)) {
            return Byte.parseByte(v);
        } else if (clazz.isAssignableFrom(Short.class)) {
            return Short.parseShort(v);
        } else if (clazz.isAssignableFrom(Integer.class)) {
            return Integer.parseInt(v);
        } else if (clazz.isAssignableFrom(Long.class)) {
            return Long.parseLong(v);
        } else if (clazz.isAssignableFrom(Float.class)) {
            return Float.parseFloat(v);
        } else if (clazz.isAssignableFrom(Double.class)) {
            return Double.parseDouble(v);
        } else if (clazz.isAssignableFrom(Date.class)) {
            return new DateAdapter().unmarshal(v);
        } else {
            return null;
        }
    }
}
