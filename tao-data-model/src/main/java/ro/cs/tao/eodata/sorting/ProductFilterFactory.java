package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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
