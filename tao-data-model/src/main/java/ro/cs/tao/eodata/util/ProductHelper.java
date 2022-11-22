package ro.cs.tao.eodata.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 *  A product helper is a utility class for retrieving common useful properties from
 *  a product, such as metadata file name, sensing date, etc.
 */
public interface ProductHelper {
    String URL_SEPARATOR = "/";

    String getName();

    void setName(String name);

    Class<? extends ProductHelper> getHelperClass();

    String getId();

    void setId(String id);

    String getVersion();

    void setVersion(String version);

    String getSensingDate();

    void setSensingDate(String date);

    String getProcessingDate();

    void setProcessingDate(String processingDate);

    /**
     * Returns the metadata file name.
     */
    String getMetadataFileName();

    String getProductRelativePath();

    /**
     * Returns the tile name pattern.
     */
    Pattern getTilePattern();

    /**
     * Returns the relative orbit of the product.
     */
    String getOrbit();

    ProductHelper duplicate();

    String[] getTokens(Pattern pattern);

    String[] getTokens(Pattern pattern, String input, Map<Integer, String> replacements);
}
