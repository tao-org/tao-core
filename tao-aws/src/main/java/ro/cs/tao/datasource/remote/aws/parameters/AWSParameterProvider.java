package ro.cs.tao.datasource.remote.aws.parameters;

import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.datasource.remote.aws.LandsatCollection;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class AWSParameterProvider implements ParameterProvider {

    private static final String[] sensors;
    private static final Map<String, Map<String, ParameterDescriptor>> parameters;

    static {
        sensors = new String[] { "Sentinel-2", "Landsat-8" };
        parameters = Collections.unmodifiableMap(
                new HashMap<String, Map<String, ParameterDescriptor>>() {{
                    put("Sentinel-2", new HashMap<String, ParameterDescriptor>() {{
                        put("platformName", new ParameterDescriptor("platformName", String.class, "Sentinel-2", true));
                        put("beginPosition",  new ParameterDescriptor("beginPosition", Date.class));
                        put("endPosition",  new ParameterDescriptor("endPosition", Date.class));
                        put("tileId", new ParameterDescriptor("tileId", String.class));
                        put("footprint",  new ParameterDescriptor("footprint", String.class));
                        put("productType",  new ParameterDescriptor("productType", String.class));
                        put("cloudcoverpercentage",  new ParameterDescriptor("cloudcoverpercentage", Double.class));
                        put("relativeOrbitNumber",  new ParameterDescriptor("relativeOrbitNumber", Short.class));
                    }});
                    put("Landsat-8", new HashMap<String, ParameterDescriptor>() {{
                        put("platformName", new ParameterDescriptor("platformName", String.class, "Landsat-8", true));
                        put("sensingStart", new ParameterDescriptor("sensingStart", Date.class));
                        put("sensingEnd", new ParameterDescriptor("sensingEnd", Date.class));
                        put("path", new ParameterDescriptor("path", String.class));
                        put("row", new ParameterDescriptor("row", String.class));
                        put("footprint",  new ParameterDescriptor("footprint", String.class));
                        put("cloudcoverpercentage", new ParameterDescriptor("cloudcoverpercentage", Double.class));
                        put("productType", new ParameterDescriptor("productType", String.class));
                        put("collection", new ParameterDescriptor("collection", String.class, LandsatCollection.COLLECTION_1.toString()));
                    }});
                }});
    }

    @Override
    public Map<String, Map<String, ParameterDescriptor>> getSupportedParameters() {
        return parameters;
    }

    @Override
    public String[] getSupportedSensors() {
        return sensors;
    }
}
