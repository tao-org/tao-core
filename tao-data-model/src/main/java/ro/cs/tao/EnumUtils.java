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

/**
 * Utility class for working with Enum types that implement the TaoEnum interface.
 * Provides methods to retrieve enum constants or their associated names
 * based on their value or friendly name.
 */
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

    /**
     * Retrieves the name of an enum constant based on the specified value.
     * The enum type must implement the {@link TaoEnum} interface, which provides
     * a method to retrieve the associated value of each enum constant.
     *
     * @param <K>       The type of the value associated with the enum constant.
     * @param <T>       The enum type, which must extend {@link Enum} and implement {@link TaoEnum}.
     * @param enumType  The class of the enum type.
     * @param value     The value to look for among the enum constants.
     * @return The name of the enum constant matching the specified value,
     *                  or {@code null} if no matching constant is found.
     */
    public static <K, T extends Enum<T> & TaoEnum<K>> String getEnumConstantNameByValue(Class<T> enumType, final K value) {
        for (T type : enumType.getEnumConstants()) {
            Object typeValue = type.value();
            if (typeValue.equals(value)) {
                return type.name();
            }
        }
        return null;
    }

    /**
     * Retrieves an enum constant from the specified enum type based on the given integer value.
     * The enum type must implement the {@link TaoEnum} interface, which provides a method
     * to retrieve the associated value of each enum constant.
     *
     * @param <T>       The enum type, which must extend {@link Enum} and implement {@link TaoEnum}.
     * @param enumType  The class of the enum type.
     * @param value     The integer value to look for among the enum constants.
     * @return The enum constant matching the specified value, or {@code null}
     * if no matching constant is found.
     */
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

    /**
     * Retrieves an enumeration constant from the specified enum type that matches the given value.
     * The enum type must implement the {@link TaoEnum} interface, which defines a method
     * for retrieving the associated value of each enum constant.
     *
     * @param <K>       The type of the value associated with the enum constant.
     * @param <T>       The enum type, which must extend {@link Enum} and implement {@link TaoEnum}.
     * @param enumType  The class of the enum type.
     * @param value     The value to search for among the enum constants.
     * @return The enum constant corresponding to the specified value, or {@code null} if no matching constant is found.
     */
    public static <K, T extends Enum<T> & TaoEnum<K>> T getEnumConstantByValue(Class<T> enumType, final K value) {
        for (T type : enumType.getEnumConstants()) {
            Object typeValue = type.value();
            if (typeValue.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Retrieves an enum constant by its name from the specified enum type.
     * The enum type must extend {@link Enum} and implement the {@link TaoEnum} interface.
     *
     * @param <T>       The enum type, which must extend {@link Enum} and implement {@link TaoEnum}.
     * @param enumType  The class of the enum type from which the constant will be retrieved.
     * @param name      The name of the enum constant to retrieve.
     * @return The enum constant corresponding to the specified name.
     * @throws IllegalArgumentException if the specified name does not match any constant of the enum type.
     * @throws NullPointerException if the enumType or name is null.
     */
    public static <T extends Enum<T> & TaoEnum> T getEnumConstantByName(Class<T> enumType, final String name) {
        return Enum.valueOf(enumType, name);
    }

    /**
     * Retrieves an enum constant from the specified enum type based on its friendly name.
     * The enum type must implement the {@link TaoEnum} interface, which provides a method
     * for retrieving the friendly name of each enum constant.
     *
     * @param <T>       The enum type, which must extend {@link Enum} and implement {@link TaoEnum}.
     * @param enumType  The class of the enum type from which the constant will be retrieved.
     * @param name      The friendly name of the enum constant to retrieve.
     * @return The enum constant corresponding to the specified friendly name, or {@code null}
     *         if no matching constant is found.
     */
    public static <T extends Enum<T> & TaoEnum> T getEnumConstantByFriendlyName(Class<T> enumType, final String name) {
        for (T type : enumType.getEnumConstants()) {
            if (type.friendlyName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Retrieves an enum constant from the specified enum type using the provided string value.
     * The search is first attempted by the friendly name of the enum constant
     * and falls back to the standard name if no match is found.
     * The enum type must extend {@link Enum} and implement the {@link TaoEnum} interface.
     *
     * @param <T>       The enum type, which must extend {@link Enum} and implement {@link TaoEnum}.
     * @param enumType  The class of the enum type from which the constant will be retrieved.
     * @param value     The string value used to search for the enum constant, either by
     *                  friendly name or standard name.
     * @return The enum constant matching the specified value, or {@code null} if no match is found.
     */
    public static <T extends Enum<T> & TaoEnum> T getEnumConstantByString(Class<T> enumType, final String value) {
        T enumConstant = getEnumConstantByFriendlyName(enumType, value);
        if (enumConstant == null) {
            enumConstant = getEnumConstantByName(enumType, value);
        }
        return enumConstant;
    }
}
