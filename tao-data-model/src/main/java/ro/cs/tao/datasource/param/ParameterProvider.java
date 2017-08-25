package ro.cs.tao.datasource.param;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public interface ParameterProvider {
    Map<String, ParameterDescriptor> getSupportedParameters();
}
