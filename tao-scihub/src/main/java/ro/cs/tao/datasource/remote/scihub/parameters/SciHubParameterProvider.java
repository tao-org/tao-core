package ro.cs.tao.datasource.remote.scihub.parameters;

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
public final class SciHubParameterProvider implements ParameterProvider {

    private static final String[] sensors;
    private static final Map<String, Map<String, ParameterDescriptor>> parameters;

    static {
        sensors = new String[] { "Sentinel-1", "Sentinel-2" };
        parameters = Collections.unmodifiableMap(
                new HashMap<String, Map<String, ParameterDescriptor>>() {{
                        put("Sentinel-1", new HashMap<String, ParameterDescriptor>() {{
                            put("platformName", new ParameterDescriptor("platformName", String.class));
                            put("beginPosition", new ParameterDescriptor("beginPosition", Date.class));
                            put("endPosition", new ParameterDescriptor("endPosition", Date.class));
                            put("footprint", new ParameterDescriptor("footprint", Polygon2D.class));
                            put("productType", new ParameterDescriptor("productType", String.class));
                            put("polarisationMode", new ParameterDescriptor("polarisationMode", String.class));
                            put("sensorOperationalMode", new ParameterDescriptor("sensorOperationalMode", String.class));
                            put("relativeOrbitNumber", new ParameterDescriptor("relativeOrbitNumber", String.class));
                        }});
                        put("Sentinel-2", new HashMap<String, ParameterDescriptor>() {{
                            put("platformName", new ParameterDescriptor("platformName", String.class));
                            put("beginPosition", new ParameterDescriptor("beginPosition", Date.class));
                            put("endPosition", new ParameterDescriptor("endPosition", Date.class));
                            put("footprint", new ParameterDescriptor("footprint", Polygon2D.class));
                            put("productType", new ParameterDescriptor("productType", String.class));
                            put("cloudcoverpercentage", new ParameterDescriptor("cloudcoverpercentage", Double.class));
                            put("relativeOrbitNumber", new ParameterDescriptor("relativeOrbitNumber", Short.class));
                        }});
                }});
    }

    @Override
    public String[] getSupportedSensors() {
        return sensors;
    }

    @Override
    public Map<String, Map<String, ParameterDescriptor>> getSupportedParameters() {
        return parameters;
    }
}
