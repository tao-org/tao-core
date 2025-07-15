package ro.cs.tao.configuration;

import java.util.List;

/**
 * Represents a set of values comprising bounds and allowed values.
 * This class is commonly used to define constraints or permitted options
 * for configuration items or other data entities.
 *
 * Fields:
 * - bounds: Encapsulates minimum and maximum constraints for a range of values.
 * - allowed_values: Represents a list of permissible discrete values.
 *
 * Nested Classes:
 * - Bounds: Defines a range with minimum and maximum values.
 * - AllowedValue: Represents a discrete value with an associated display label.
 *
 * This class provides getter and setter methods to access and modify the ranges
 * and list of allowed values.
 */
public class Values {
    private Bounds bounds;
    private List<AllowedValue> allowed_values;

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public List<AllowedValue> getAllowed_values() {
        return allowed_values;
    }

    public void setAllowed_values(List<AllowedValue> allowed_values) {
        this.allowed_values = allowed_values;
    }

    public static class Bounds {
        private int min;
        private int max;

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }

    public static class AllowedValue {
        private String value;
        private String display;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }
    }
}
