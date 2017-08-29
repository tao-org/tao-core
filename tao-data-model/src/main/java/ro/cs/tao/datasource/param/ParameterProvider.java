package ro.cs.tao.datasource.param;

import ro.cs.tao.datasource.ProductFetchStrategy;

import java.util.Map;

/**
 * Interface that provides capabilities of a data source (what sensors and
 * what parameters for each sensor)
 *
 * @author Cosmin Cara
 */
public interface ParameterProvider {
    /**
     * Returns the query parameters for all the sensors supported by this data source
     */
    Map<String, Map<String, ParameterDescriptor>> getSupportedParameters();

    /**
     * Returns the sensors supported by this data source
     */
    String[] getSupportedSensors();

    /**
     * Returns the fetchers associated with the supported sensors.
     */
    Map<String, ProductFetchStrategy> getRegisteredProductFetchStrategies();
}
