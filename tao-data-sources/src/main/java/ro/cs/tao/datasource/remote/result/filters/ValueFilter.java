package ro.cs.tao.datasource.remote.result.filters;

/**
 * @author Cosmin Cara
 */
public class ValueFilter implements AttributeFilter {
    private final String value;

    public ValueFilter(String value) {
        this.value = value;
    }

    @Override
    public boolean accept(String attributeName, String value) {
        return this.value == null || !this.value.equals(value);
    }
}
