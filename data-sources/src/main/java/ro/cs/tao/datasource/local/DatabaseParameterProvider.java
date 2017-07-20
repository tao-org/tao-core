package ro.cs.tao.datasource.local;

import ro.cs.tao.datasource.common.ParameterDescriptor;
import ro.cs.tao.datasource.common.ParameterProvider;

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
