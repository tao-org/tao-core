package ro.cs.tao.datasource.remote.result.filters;

/**
 * @author Cosmin Cara
 */
public class NullFilter implements AttributeFilter {

    @Override
    public boolean accept(String attributeName, String value) {
        return value != null;
    }
}
