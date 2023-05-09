package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.List;

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
