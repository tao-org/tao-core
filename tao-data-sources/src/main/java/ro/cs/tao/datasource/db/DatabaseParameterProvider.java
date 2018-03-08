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
package ro.cs.tao.datasource.db;

import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class DatabaseParameterProvider implements ParameterProvider {
    @Override
    public Map<String, Map<String, ParameterDescriptor>> getSupportedParameters() {
        //TODO: retrieve the parameters supported by the database query
        return null;
    }

    @Override
    public String[] getSupportedSensors() {
        //TODO: retrieve the sensors supported by the database query
        return new String[0];
    }

    @Override
    public Map<String, ProductFetchStrategy> getRegisteredProductFetchStrategies() {
        //TODO: implement the actual db record fetcher and register it here
        return null;
    }
}
