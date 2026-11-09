package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.List;

/**
 * A concrete implementation of the DataFilter class designed to retain only the last
 * element from a list of EOProduct objects. This filter operates on a given list of
 * EOProduct instances and returns the last product in the list or null if the list
 * is null or empty.
 *
 * The class provides a user-friendly name to indicate its specific filtering behavior.
 */
public class KeepLastProductFilter extends DataFilter<EOProduct> {
    @Override
    public EOProduct filter(List<EOProduct> sourceList) {
        return sourceList != null && sourceList.size() >= 1
               ? sourceList.get(sourceList.size() - 1)
               : null;
    }

    @Override
    protected String friendlyName() {
        return "Keep last";
    }
}
