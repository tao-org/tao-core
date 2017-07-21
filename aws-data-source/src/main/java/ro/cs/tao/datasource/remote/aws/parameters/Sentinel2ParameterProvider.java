package ro.cs.tao.datasource.remote.aws.parameters;

import ro.cs.tao.datasource.common.ParameterDescriptor;
import ro.cs.tao.datasource.common.ParameterProvider;
import ro.cs.tao.datasource.util.Polygon2D;

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
            put("sensor", new ParameterDescriptor("sensor", String.class, true));
            put("platformName", new ParameterDescriptor("platformName", String.class));
            put("beginPosition",  new ParameterDescriptor("beginPosition", Date.class));
            put("endPosition",  new ParameterDescriptor("endPosition", Date.class));
            put("footprint",  new ParameterDescriptor("footprint", Polygon2D.class));
            put("productType",  new ParameterDescriptor("productType", String.class));
            put("cloudcoverpercentage",  new ParameterDescriptor("cloudcoverpercentage", Double.class));
            put("relativeOrbitNumber",  new ParameterDescriptor("relativeOrbitNumber", Short.class));
        }});
    }
}
