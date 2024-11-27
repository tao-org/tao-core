package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.Tuple;

import java.util.*;

public class PreviousProductAssociation extends Association<EOProduct> {

    public PreviousProductAssociation() {
        super();
    }

    @Override
    public String[] description() {
        return new String[] { friendlyName() };
    }

    @Override
    public List<Tuple<EOProduct, EOProduct>> associate(List<EOProduct> source) {
        if (source == null) return null;
        List<Tuple<EOProduct, EOProduct>> results = new ArrayList<>();
        final Map<String, List<EOProduct>> productMap = new HashMap<>();
        for (EOProduct product : source) {
            final String geometry = product.getGeometry();
            if (!productMap.containsKey(geometry)) {
                productMap.put(geometry, new ArrayList<>());
            }
            productMap.get(geometry).add(product);
        }
        for (Map.Entry<String, List<EOProduct>> entry : productMap.entrySet()) {
            final List<EOProduct> list = entry.getValue();
            list.sort(new Comparator<EOProduct>() {
                @Override
                public int compare(EOProduct o1, EOProduct o2) {
                    return o1.getAcquisitionDate().compareTo(o2.getAcquisitionDate());
                }
            });
            final int size = list.size();
            for (int i = 1; i < size; i++) {
                final EOProduct first = list.get(i - 1);
                final EOProduct second = list.get(i);
                if (first.getAcquisitionDate().toLocalDate().isBefore(second.getAcquisitionDate().toLocalDate())) {
                    results.add(new Tuple<>(first, second));
                }
            }
        }
        return results;
    }

    @Override
    protected String friendlyName() {
        return "Pair by Same Footprint and Previous Date";
    }
}
