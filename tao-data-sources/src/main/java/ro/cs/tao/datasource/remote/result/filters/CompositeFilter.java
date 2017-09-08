package ro.cs.tao.datasource.remote.result.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class CompositeFilter implements AttributeFilter {
    private final List<AttributeFilter> filters;

    public CompositeFilter() {
        this.filters = new ArrayList<>();
    }

    public void addFilter(AttributeFilter filter) {
        this.filters.add(filter);
    }

    @Override
    public boolean accept(String attributeName, String value) {
        return this.filters.stream().allMatch(f -> f.accept(attributeName, value));
    }
}
