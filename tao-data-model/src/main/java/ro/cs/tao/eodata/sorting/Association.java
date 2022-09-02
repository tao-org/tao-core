package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOData;
import ro.cs.tao.utils.Tuple;

import java.util.List;

/**
 * Base class for association implementations.
 * An association groups elements of a list of inputs based on its internal implementation.
 *
 * @param <T>   The type of the inputs (derivatives of EOData)
 */
public abstract class Association<T extends EOData> {

    private final String friendlyName;

    public Association() { this.friendlyName = friendlyName(); }

    public String getFriendlyName() { return friendlyName; }

    public abstract String[] description();

    public abstract List<Tuple<T, T>> associate(List<T> source);

    protected abstract String friendlyName();
}
