package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOData;

import java.util.List;
import java.util.function.Function;

/**
 * An abstract class that defines a filter operation for a collection of objects of type T,
 * where T extends a base class EOData. This class provides a structured approach for filtering
 * data based on specific criteria defined in subclasses.
 *
 * @param <T> the type of objects this filter processes, which must extend EOData
 */
public abstract class DataFilter<T extends EOData> {
    private final String friendlyName;

    public DataFilter() { this.friendlyName = friendlyName(); }

    public String getFriendlyName() { return friendlyName; }

    public String[] description() { return new String[] { friendlyName() }; }

    public abstract T filter(List<T> sourceList);

    protected <U> Function<T, U> reducer() {
        return t -> null;
    }

    protected abstract String friendlyName();
}
