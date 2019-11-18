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

package ro.cs.tao;

public class EnumUtils {

    /**
     * Retrieve string enum token corresponding to the integer identifier
     * @param value the integer value identifier
     * @return the string token corresponding to the integer identifier
     */
    public static <T extends Enum<T> & TaoEnum> String getEnumConstantNameByValue(Class<T> enumType, final int value) {
        for (T type : enumType.getEnumConstants()) {
            Object typeValue = type.value();
            if (typeValue instanceof Integer && typeValue.equals(value)) {
                return type.name();
            } else if (typeValue instanceof Short && ((Short) typeValue).intValue() == value) {
                return type.name();
            } else if (typeValue instanceof Byte && ((Byte) typeValue).intValue() == value) {
                return type.name();
            }
        }
        return null;
    }

    public static <T extends Enum<T> & TaoEnum> T getEnumConstantByValue(Class<T> enumType, final int value) {
        for (T type : enumType.getEnumConstants()) {
            Object typeValue = type.value();
            if (typeValue instanceof Integer && typeValue.equals(value)) {
                return type;
            } else if (typeValue instanceof Short && ((Short) typeValue).intValue() == value) {
                return type;
            } else if (typeValue instanceof Byte && ((Byte) typeValue).intValue() == value) {
                return type;
            }
        }
        return null;
    }

    public static <T extends Enum<T> & TaoEnum> T getEnumConstantByName(Class<T> enumType, final String name) {
        return Enum.valueOf(enumType, name);
    }

    public static <T extends Enum<T> & TaoEnum> T getEnumConstantByFriendlyName(Class<T> enumType, final String name) {
        for (T type : enumType.getEnumConstants()) {
            if (type.friendlyName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
