package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.HashMap;
import java.util.Map;

public class ProductSortingFactory {

    private static final Map<String, DataSorter<EOProduct>> sortMap;

    static {
        sortMap = new HashMap<>();
        DataSorter<EOProduct> sorter = new ProductDateSorter();
        sortMap.put(sorter.friendlyName(), sorter);
        sorter = new ProductNameSorter();
        sortMap.put(sorter.friendlyName(), sorter);
    }

    public static DataSorter<EOProduct> getSorter(String name) {
        DataSorter<EOProduct> sorter = sortMap.get(name);
        if (sorter == null) {
            throw new IllegalArgumentException("No such DataSorter<EOProduct>: " + name);
        }
        return sorter;
    }
}
