package ro.cs.tao.eodata.sorting;

import ro.cs.tao.Tuple;
import ro.cs.tao.eodata.EOData;

import java.util.List;

public abstract class Association<T extends EOData> {

    private final String friendlyName;

    public Association() { this.friendlyName = friendlyName(); }

    public String getFriendlyName() { return friendlyName; }

    public abstract String[] description();

    public abstract List<Tuple<T, T>> associate(List<T> source);

    protected abstract String friendlyName();
}
