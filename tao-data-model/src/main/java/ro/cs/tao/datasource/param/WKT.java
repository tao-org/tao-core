package ro.cs.tao.datasource.param;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * Represents a Well-Known Text (WKT) string.
 * This is commonly used for spatial and geographic data interchange.
 * The class encapsulates a WKT string and provides access to its value.
 */
public class WKT {
    private final String value;

    public WKT(String value) {
        try {
            // Validate
            new WKTReader().read(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
