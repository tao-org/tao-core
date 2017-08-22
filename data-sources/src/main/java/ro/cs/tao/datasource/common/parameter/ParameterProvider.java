package ro.cs.tao.datasource.common.parameter;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public interface ParameterProvider {
    Map<String, ParameterDescriptor> getSupportedParameters();
}
