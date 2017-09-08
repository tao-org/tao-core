package ro.cs.tao.datasource.remote.result.filters;

import java.util.Set;

/**
 * @author Cosmin Cara
 */
public class NameFilter implements AttributeFilter {
    private final Set<String> namesToExclude;

    public NameFilter(Set<String> namesToExclude) {
        this.namesToExclude = namesToExclude;
    }

    @Override
    public boolean accept(String attributeName, String value) {
        return this.namesToExclude == null || !this.namesToExclude.contains(attributeName);
    }
}
