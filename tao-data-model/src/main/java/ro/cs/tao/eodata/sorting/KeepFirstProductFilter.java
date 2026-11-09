package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.List;

/**
 * A concrete implementation of the DataFilter class for filtering a list of EOProduct
 * instances by retaining only the first product in the list. If the input list is null
 * or empty, the filter method returns null.
 *
 * This filter is specifically designed to ensure that only the first product from the
 * input list is retained, and all other products are excluded. It provides a simple way
 * to constrain the output to a single product from potentially larger datasets.
 */
public class KeepFirstProductFilter extends DataFilter<EOProduct> {
    @Override
    public EOProduct filter(List<EOProduct> sourceList) {
        return sourceList != null && sourceList.size() >= 1
               ? sourceList.get(0)
               : null;
    }

    @Override
    protected String friendlyName() {
        return "Keep first";
    }
}
