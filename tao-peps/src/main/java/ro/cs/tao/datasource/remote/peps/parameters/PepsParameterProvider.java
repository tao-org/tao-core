package ro.cs.tao.datasource.remote.peps.parameters;

import ro.cs.tao.config.ConfigurationManager;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.datasource.remote.peps.Collection;
import ro.cs.tao.datasource.remote.peps.download.PepsDownloadStrategy;
import ro.cs.tao.eodata.Polygon2D;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class PepsParameterProvider implements ParameterProvider {

    private static final String[] sensors;
    private static final Map<String, Map<String, ParameterDescriptor>> parameters;
    private static final Map<String, ProductFetchStrategy> productFetchers;

    static {
        sensors = new String[] { "Sentinel-1", "Sentinel-2" };
        parameters = Collections.unmodifiableMap(
                new HashMap<String, Map<String, ParameterDescriptor>>() {{
                    put("Sentinel-1", new HashMap<String, ParameterDescriptor>() {{
                        put("collection", new ParameterDescriptor("collection", String.class, Collection.S1.toString(), true));
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
                        put("polarisation",  new ParameterDescriptor("polarisation", String.class));
                    }});
                    put("Sentinel-2", new HashMap<String, ParameterDescriptor>() {{
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
                        put("cloudCover",  new ParameterDescriptor("cloudCover", Double.class));
                    }});
                }});
        final String targetFolder = ConfigurationManager.getInstance().getValue("product.location");
        productFetchers = Collections.unmodifiableMap(
                new HashMap<String, ProductFetchStrategy>() {{
                    put("Sentinel-1", new PepsDownloadStrategy(targetFolder));
                    put("Sentinel-2", new PepsDownloadStrategy(targetFolder));
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

    @Override
    public Map<String, ProductFetchStrategy> getRegisteredProductFetchStrategies() { return productFetchers; }

}
