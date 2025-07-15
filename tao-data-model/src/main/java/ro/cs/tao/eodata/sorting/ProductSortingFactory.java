package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a factory for creating and retrieving predefined {@link DataSorter} instances
 * for sorting a collection of {@link EOProduct} objects based on various attributes.
 *
 * The factory maintains a mapping of sorter names to their respective {@link DataSorter}
 * implementations, allowing for flexible and reusable sorting strategies. Supported sorters
 * include {@link ProductDateSorter} for sorting by acquisition date and
 * {@link ProductNameSorter} for sorting by name.
 *
 * Key Functionalities:
 * - Offers a centralized mechanism to access predefined sorters by name.
 * - Throws an exception if a non-existent sorter name is requested.
 * - Utilizes static initialization to load and associate specific sorters with their names.
 */
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
