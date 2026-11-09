package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * The ProductFilterFactory class provides a centralized mechanism for creating
 * instances of {@code DataFilter} specifically designed to process collections
 * of {@code EOProduct} objects. It maintains a mapping between filter names and their
 * corresponding filter implementations, enabling the dynamic creation of filters by name.
 *
 * Key Features:
 * 1. Maintains a registry of all available filters and their friendly names.
 * 2. Supports dynamic*/
public class ProductFilterFactory {
    private static final Map<String, Class<? extends DataFilter<EOProduct>>> filterMap;

    static {
        filterMap = new HashMap<>();
        DataFilter<EOProduct> filter = new KeepFirstProductFilter();
        filterMap.put(filter.friendlyName(), KeepFirstProductFilter.class);
        filter = new KeepLastProductFilter();
        filterMap.put(filter.friendlyName(), KeepLastProductFilter.class);
        filter = new KeepMiddleProductFilter();
        filterMap.put(filter.friendlyName(), KeepMiddleProductFilter.class);
        filter = new ClosestToDateFilter();
        filterMap.put(filter.friendlyName(), ClosestToDateFilter.class);
    }

    /**
     * Retrieves a specific {@code DataFilter} implementation for filtering {@code EOProduct} objects
     * based on the provided filter name.
     *
     * @param name the name of the desired filter. It must match one of the registered filter names.
     *             If the name does not match any registered filter, an {@code IllegalArgumentException}
     *             is thrown.
     * @return an instance of the requested {@code DataFilter<EOProduct>}. If the filter cannot
     *         be instantiated due to reflection-related issues, an {@code IllegalArgumentException}
     *         is thrown.
     */
    public static DataFilter<EOProduct> getFilter(String name) {
        Class<? extends DataFilter<EOProduct>> filter = filterMap.get(name);
        if (filter == null) {
            throw new IllegalArgumentException("No such DataFilter<EOProduct>: " + name);
        }
        try {
            return filter.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Cannot create filter %s (are you missing an argument?)",
                                                             name));
        }
    }

    /**
     * Retrieves a specific {@code DataFilter} implementation for filtering {@code EOProduct} objects
     * based on the provided filter name and argument. The method dynamically instantiates the filter
     * using reflection and passes the argument to the filter's constructor.
     *
     * @param name the name of the desired filter. It must match one of the registered filter names.
     *             If the name does not match any registered filter, an {@code IllegalArgumentException}
     *             is thrown.
     * @param arg  the argument to be passed to the filter's constructor. The argument type must match
     *             the parameter type of the constructor for the specified filter. A {@code null} value
     *             will result in an {@code IllegalArgumentException}.
     * @return an instance of the requested {@code DataFilter<EOProduct>}. If the filter cannot
     *         be instantiated due to reflection-related issues, an {@code IllegalArgumentException}
     *         is thrown.
     */
    public static DataFilter<EOProduct> getFilter(String name, Object arg) {
        if (arg == null) {
            throw new IllegalArgumentException(String.format("Cannot create filter %s with NULL argument", name));
        }
        Class<? extends DataFilter<EOProduct>> filter = filterMap.get(name);
        if (filter == null) {
            throw new IllegalArgumentException("No such DataFilter<EOProduct>: " + name);
        }
        try {
            Constructor<? extends DataFilter<EOProduct>> constructor = filter.getDeclaredConstructor(arg.getClass());
            return constructor.newInstance(arg);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException(String.format("Cannot create filter %s. Check the second argument",
                                                             name));
        }
    }
}
