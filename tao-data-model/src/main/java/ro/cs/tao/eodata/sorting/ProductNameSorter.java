package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * Sorts a list of EOProduct entities by their name.
 *
 * @author Cosmin Cara
 */
public class ProductNameSorter extends DataSorter<EOProduct> {

    @Override
    public List<EOProduct> sort(List<EOProduct> sourceList, boolean ascending) {
        if (sourceList == null) return null;
        List<EOProduct> newList = new ArrayList<>(sourceList);
        newList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()) * (ascending ? 1 : -1));
        return newList;
    }

    @Override
    protected String friendlyName() { return "Sort by Name"; }
}
