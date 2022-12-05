package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.List;

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
