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
