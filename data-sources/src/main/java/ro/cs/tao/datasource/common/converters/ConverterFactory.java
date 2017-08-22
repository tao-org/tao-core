/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.datasource.common.converters;

import ro.cs.tao.datasource.common.parameter.QueryParameter;
import ro.cs.tao.datasource.util.Polygon2D;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class ConverterFactory {

    private final Map<Class, Class<? extends ParameterConverter>> converters;

    public static ConverterFactory getInstance() { return new ConverterFactory(); }

    private ConverterFactory() {
        converters = new HashMap<>();
        converters.put(Date.class, DateConverter.class);
        converters.put(Polygon2D.class, PolygonConverter.class);
    }

    public void register(Class<? extends ParameterConverter> converter, Class forClass) {
        converters.put(forClass, converter);
    }

    public void unregister(Class forClass) {
        converters.remove(forClass);
    }

    public ParameterConverter create(QueryParameter parameter) {
        Class parameterType = parameter.getType();
        Class<? extends ParameterConverter> converterClass = converters.get(parameterType);
        if (converterClass == null) {
            converterClass = DefaultConverter.class;
        }
        Constructor<? extends ParameterConverter> ctor;
        ParameterConverter instance = null;
        try {
            ctor = converterClass.getConstructor(QueryParameter.class);
            instance = ctor.newInstance(parameter);
        } catch (NoSuchMethodException | IllegalAccessException |
                 InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }
}
