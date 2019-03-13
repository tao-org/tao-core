package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ProductBySameDateAssociation extends Association<EOProduct> {

    @Override
    public String[] description() { return new String[] { friendlyName() }; }

    @Override
    public List<Tuple<EOProduct, EOProduct>> associate(List<EOProduct> source) {
        if (source == null) return null;
        List<Tuple<EOProduct, EOProduct>> results = new ArrayList<>();
        final int sourceSize = source.size();
        EOProduct first, second;
        boolean found = false;
        for (int i = 0; i < sourceSize - 1; i++) {
            first = source.get(i);
            for (int j = i + 1; j < sourceSize; j++) {
                found = false;
                second = source.get(j);
                if (first.getAcquisitionDate().equals(second.getAcquisitionDate())) {
                    found = true;
                    results.add(new Tuple<>(first, second));
                }
            }
            if (!found) {
                results.add(new Tuple<>(first, null));
            }
        }
        return results;
    }

    @Override
    protected String friendlyName() { return "Group by Acquisition Date"; }
}
