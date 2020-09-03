package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOData;

import java.util.List;

/**
 * Base class for sorter implementations.
 * A sorter orders a list of inputs based on its internal implementation.
 *
 * @param <T>   The type of the inputs (derivatives of EOData)
 */
public abstract class DataSorter<T extends EOData> {

    private final String friendlyName;

    public DataSorter() { this.friendlyName = friendlyName(); }

    public String getFriendlyName() { return friendlyName; }

    public abstract List<T> sort(List<T> sourceList, boolean ascending);

    protected abstract String friendlyName();
}
