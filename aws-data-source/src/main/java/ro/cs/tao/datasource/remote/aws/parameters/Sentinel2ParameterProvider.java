package ro.cs.tao.datasource.remote.aws.parameters;

import ro.cs.tao.datasource.common.parameter.ParameterDescriptor;
import ro.cs.tao.datasource.common.parameter.ParameterProvider;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class Sentinel2ParameterProvider implements ParameterProvider {
    @Override
    public Map<String, ParameterDescriptor> getSupportedParameters() {
        return Collections.unmodifiableMap(new HashMap<String, ParameterDescriptor>() {{
            put("platformName", new ParameterDescriptor("platformName", String.class, true));
            put("beginPosition",  new ParameterDescriptor("beginPosition", Date.class));
            put("endPosition",  new ParameterDescriptor("endPosition", Date.class));
            put("tileId", new ParameterDescriptor("tileId", String.class));
            put("footprint",  new ParameterDescriptor("footprint", String.class));
            put("productType",  new ParameterDescriptor("productType", String.class));
            put("cloudcoverpercentage",  new ParameterDescriptor("cloudcoverpercentage", Double.class));
            put("relativeOrbitNumber",  new ParameterDescriptor("relativeOrbitNumber", Short.class));
        }});
    }
}
