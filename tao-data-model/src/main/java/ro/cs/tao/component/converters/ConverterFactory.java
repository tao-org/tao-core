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
package ro.cs.tao.component.converters;

import ro.cs.tao.component.ParameterDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for converters.
 *
 * @author Cosmin Cara
 */
public class ConverterFactory {

    private final Map<Class, Class<? extends ParameterConverter>> converters;

    public static ConverterFactory getInstance() { return new ConverterFactory(); }

    private ConverterFactory() {
        converters = new HashMap<>();
        converters.put(Boolean.class, BooleanConverter.class);
        converters.put(boolean.class, BooleanConverter.class);
        converters.put(Byte.class, ByteConverter.class);
        converters.put(byte.class, ByteConverter.class);
        converters.put(LocalDateTime.class, DateConverter.class);
        converters.put(Double.class, DoubleConverter.class);
        converters.put(double.class, DoubleConverter.class);
        converters.put(Float.class, FloatConverter.class);
        converters.put(float.class, FloatConverter.class);
        converters.put(Integer.class, IntegerConverter.class);
        converters.put(int.class, IntegerConverter.class);
        converters.put(Long.class, LongConverter.class);
        converters.put(long.class, LongConverter.class);
        converters.put(Short.class, ShortConverter.class);
        converters.put(short.class, ShortConverter.class);
        converters.put(String.class, StringConverter.class);
    }

    public void register(Class<? extends ParameterConverter> converter, Class forClass) {
        converters.put(forClass, converter);
    }

    public void unregister(Class forClass) {
        converters.remove(forClass);
    }

    public ParameterConverter create(ParameterDescriptor parameter) {
        Class parameterType = parameter.getDataType();
        return create(parameterType);
    }

    /**
     * Returns the registered (specialized) converter for the given class, or a {@link DefaultConverter} if there is
     * no such specialized converter.
     *
     * @param forClass  The object class.
     */
    public ParameterConverter create(Class forClass) {
        Class<? extends ParameterConverter> converterClass = converters.get(forClass);
        if (converterClass == null) {
            converterClass = DefaultConverter.class;
        }
        Constructor<? extends ParameterConverter> ctor;
        ParameterConverter instance = null;
        try {
            ctor = converterClass.getConstructor();
            instance = ctor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }
}