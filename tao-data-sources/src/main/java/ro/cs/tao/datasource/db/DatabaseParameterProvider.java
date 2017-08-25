package ro.cs.tao.datasource.db;

import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class DatabaseParameterProvider implements ParameterProvider {
    @Override
    public Map<String, ParameterDescriptor> getSupportedParameters() {
        return Collections.unmodifiableMap(new HashMap<String, ParameterDescriptor>() {{
            //TODO: add database parameters (i.e. search criteria) here
        }});
    }
}
