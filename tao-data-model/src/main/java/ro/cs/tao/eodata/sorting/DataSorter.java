package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOData;

import java.util.List;

public abstract class DataSorter<T extends EOData> {

    private final String friendlyName;

    public DataSorter() { this.friendlyName = friendlyName(); }

    public String getFriendlyName() { return friendlyName; }

    public abstract List<T> sort(List<T> sourceList, boolean ascending);

    protected abstract String friendlyName();
}
