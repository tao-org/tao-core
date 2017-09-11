package ro.cs.tao.datasource.remote.result.filters;

/**
 * @author Cosmin Cara
 */
public interface AttributeFilter {
    default boolean accept(String attributeName, String value) { return true; }
}
