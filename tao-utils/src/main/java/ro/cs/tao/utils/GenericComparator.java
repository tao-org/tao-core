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

package ro.cs.tao.utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

public class GenericComparator implements Comparator<Object>  {

    private final Class objectClass;
    private final String[] fieldNames;
    private final boolean[] ascendingOrder;

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
    }

    @Override
    public int compare(Object o1, Object o2) {
        int retValue = -1;
        if (Byte.class.equals(this.objectClass)) {
            retValue = Byte.compare((Byte)o1, (Byte)o2);
        } else if (Short.class.equals(this.objectClass)) {
            retValue = Short.compare((Short)o1, (Short)o2);
        } else if (Integer.class.equals(this.objectClass)) {
            retValue = Integer.compare((Integer)o1, (Integer)o2);
        } else if (Long.class.equals(this.objectClass)) {
            retValue = Long.compare((Long)o1, (Long)o2);
        } else if (Float.class.equals(this.objectClass)) {
            retValue = Float.compare((Float)o1, (Float)o2);
        } else if (Double.class.equals(this.objectClass)) {
            retValue = Double.compare((Double)o1, (Double)o2);
        } else if (String.class.equals(this.objectClass)) {
            retValue = ((String)o1).compareTo((String)o2);
        } else if (Date.class.equals(this.objectClass)) {
            retValue = ((Date)o1).compareTo((Date)o2);
        } else if (LocalDate.class.equals(this.objectClass)) {
            retValue = ((LocalDate)o1).compareTo((LocalDate)o2);
        } else if (LocalDateTime.class.equals(this.objectClass)) {
            retValue = ((LocalDateTime)o1).compareTo((LocalDateTime)o2);
        } else {
            if (fieldNames != null && fieldNames.length > 0) {
                int index = 0;
                retValue = 0;
                while (retValue == 0) {
                    try {
                        Field field = this.objectClass.getDeclaredField(fieldNames[index]);
                        field.setAccessible(true);
                        retValue = compare(field.get(o1), field.get(o2)) * (ascendingOrder[index++] ? 1 : -1);
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
