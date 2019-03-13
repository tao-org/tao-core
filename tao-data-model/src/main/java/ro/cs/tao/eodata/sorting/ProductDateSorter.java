package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductDateSorter extends DataSorter<EOProduct> {

    @Override
    public List<EOProduct> sort(List<EOProduct> sourceList, boolean ascending) {
        if (sourceList == null) return null;
        List<EOProduct> newList = new ArrayList<>(sourceList);
        newList.sort((o1, o2) -> {
                Date date1 = o1.getAcquisitionDate();
                Date date2 = o2.getAcquisitionDate();
                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return -1;
                if (date2 == null) return 1;
                int res = date1.compareTo(date2);
                return res * (ascending ? 1 : -1);
        });
        return newList;
    }

    @Override
    protected String friendlyName() { return "Sort by Acquisition Date"; }
}
