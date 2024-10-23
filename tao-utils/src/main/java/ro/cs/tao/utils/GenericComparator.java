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

package ro.cs.tao.utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class GenericComparator implements Comparator<Object>  {

    private final Class objectClass;
    private final String[] fieldNames;
    private final boolean[] ascendingOrder;
    private final Map<String, Field> fields;

    public GenericComparator(Class objectClass, Map<String, Boolean> fieldsAndOrder) {
        this.objectClass = objectClass;
        if (fieldsAndOrder != null) {
            fieldNames = new String[fieldsAndOrder.size()];
            ascendingOrder = new boolean[fieldsAndOrder.size()];
            int idx = 0;
            for (String key : fieldsAndOrder.keySet()) {
                fieldNames[idx] = key;
                ascendingOrder[idx++] = fieldsAndOrder.get(key);
            }
        } else {
            fieldNames = new String[0];
            ascendingOrder = new boolean[0];
        }
        fields = new HashMap<>();
        Field[] list = this.objectClass.getDeclaredFields();
        for (Field f : list) {
            fields.put(f.getName(), f);
        }
        Class parent = this.objectClass.getSuperclass();
        while (!parent.equals(Object.class)) {
            list = parent.getDeclaredFields();
            if (list != null) {
                for (Field f : list) {
                    fields.put(f.getName(), f);
                }
            }
            parent = parent.getSuperclass();
        }
    }

    @Override
    public int compare(Object o1, Object o2) {
        int retValue = -1;
        if (o1 == null) {
            return retValue;
        }
        if (o2 == null) {
            return -retValue;
        }
        if (Byte.class.equals(o1.getClass()) || byte.class.equals(o1.getClass())) {
            retValue = Byte.compare((Byte)o1, (Byte)o2);
        } else if (Short.class.equals(o1.getClass()) || short.class.equals(o1.getClass())) {
            retValue = Short.compare((Short)o1, (Short)o2);
        } else if (Integer.class.equals(o1.getClass()) || int.class.equals(o1.getClass())) {
            retValue = Integer.compare((Integer)o1, (Integer)o2);
        } else if (Long.class.equals(o1.getClass()) || long.class.equals(o1.getClass())) {
            retValue = Long.compare((Long)o1, (Long)o2);
        } else if (Float.class.equals(o1.getClass()) || float.class.equals(o1.getClass())) {
            retValue = Float.compare((Float)o1, (Float)o2);
        } else if (Double.class.equals(o1.getClass()) || double.class.equals(o1.getClass())) {
            retValue = Double.compare((Double)o1, (Double)o2);
        } else if (String.class.equals(o1.getClass())) {
            retValue = ((String)o1).compareTo((String)o2);
        } else if (LocalDate.class.equals(o1.getClass())) {
            retValue = ((LocalDate)o1).compareTo((LocalDate)o2);
        } else if (LocalDateTime.class.equals(o1.getClass())) {
            retValue = ((LocalDateTime)o1).compareTo((LocalDateTime)o2);
        } else {
            if (fieldNames != null && fieldNames.length > 0) {
                int index = 0;
                retValue = 0;
                while (retValue == 0 && index < fieldNames.length) {
                    try {
                        Field field = fields.get(fieldNames[index]);
                        if (field == null) {
                            throw new NoSuchFieldException(fieldNames[index]);
                        }
                        if (field.canAccess(o1)) {
                            Object value1 = field.get(o1);
                            Object value2 = field.get(o2);
                            retValue = compare(value1, value2) * (ascendingOrder[index++] ? 1 : -1);
                        }
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(String.format("Class %s doesn't define field %s",
                                                                 this.objectClass.getSimpleName(), fieldNames[index - 1]));
                    }
                }
            }
        }
        return retValue;
    }
}
