package ro.cs.tao.datasource.remote.scihub.parameters;

import ro.cs.tao.datasource.common.parameter.ParameterDescriptor;
import ro.cs.tao.datasource.util.Polygon2D;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class ParameterProvider implements ro.cs.tao.datasource.common.parameter.ParameterProvider {
    @Override
    public Map<String, ParameterDescriptor> getSupportedParameters() {
        return Collections.unmodifiableMap(new HashMap<String, ParameterDescriptor>() {{
            // Common
            put("platformName", new ParameterDescriptor("platformName", String.class));
            put("beginPosition",  new ParameterDescriptor("beginPosition", Date.class));
            put("endPosition",  new ParameterDescriptor("endPosition", Date.class));
            put("footprint",  new ParameterDescriptor("footprint", Polygon2D.class));
            put("productType",  new ParameterDescriptor("productType", String.class));
            // Sentinel-1
            put("polarisationMode",  new ParameterDescriptor("polarisationMode", String.class));
            put("sensorOperationalMode",  new ParameterDescriptor("sensorOperationalMode", String.class));
            put("relativeOrbitNumber",  new ParameterDescriptor("relativeOrbitNumber", String.class));
            // Sentinel-2
            put("cloudcoverpercentage",  new ParameterDescriptor("cloudcoverpercentage", Double.class));
            put("relativeOrbitNumber",  new ParameterDescriptor("relativeOrbitNumber", Short.class));
        }});
    }
}
