package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.List;

/**
 * A filter implementation that retains the middle element(s) of a given list of EOProduct instances,
 * returning one middle element based on the size of the input list.
 *
 * This filter selects:
 * - The exact middle element for lists of odd size.
 * - The one-before-middle element for lists of even size.
 *
 * If the input list is empty, the method returns null.
 *
 * This class extends the abstract {@code DataFilter} and provides a specific implementation
 * for filtering {@code EOProduct} instances based on their position.
 */
public class KeepMiddleProductFilter extends DataFilter<EOProduct> {
    @Override
    public EOProduct filter(List<EOProduct> sourceList) {
        final int size = sourceList.size();
        return size > 0
               ? sourceList.get(size % 2 == 1 ? (size - 1) / 2 : size / 2 - 1)
               : null;
    }

    @Override
    protected String friendlyName() {
        return "Keep middle";
    }
}
