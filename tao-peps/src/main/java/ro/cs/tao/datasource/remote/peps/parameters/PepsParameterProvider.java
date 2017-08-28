package ro.cs.tao.datasource.remote.peps.parameters;

import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.eodata.Polygon2D;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class PepsParameterProvider implements ParameterProvider {
    @Override
    public Map<String, ParameterDescriptor> getSupportedParameters() {
        return Collections.unmodifiableMap(new HashMap<String, ParameterDescriptor>() {{
            // Common
            put("collection", new ParameterDescriptor("collection", String.class, true));
            put("platform", new ParameterDescriptor("platform", String.class));
            put("instrument", new ParameterDescriptor("instrument", String.class));
            put("processingLevel",  new ParameterDescriptor("processingLevel", String.class));
            put("productType",  new ParameterDescriptor("productType", String.class));
            put("sensorMode",  new ParameterDescriptor("sensorMode", String.class));
            put("orbitDirection",  new ParameterDescriptor("orbitDirection", String.class));
            put("orbit",  new ParameterDescriptor("orbit", Short.class));
            put("relativeOrbitNumber",  new ParameterDescriptor("relativeOrbitNumber", Short.class));
            put("isNrt",  new ParameterDescriptor("isNrt", Boolean.class));

            put("startDate",  new ParameterDescriptor("startDate", Date.class, true));
            put("completionDate",  new ParameterDescriptor("completionDate", Date.class));
            put("box",  new ParameterDescriptor("box", Polygon2D.class, true));

            // Sentinel-1
            put("polarisation",  new ParameterDescriptor("polarisation", String.class));
            put("sensorMode",  new ParameterDescriptor("sensorMode", String.class));
            // Sentinel-2
            put("cloudCover",  new ParameterDescriptor("cloudCover", Double.class));

        }});
    }
}
