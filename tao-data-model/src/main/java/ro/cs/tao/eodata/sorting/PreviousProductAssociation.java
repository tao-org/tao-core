package ro.cs.tao.eodata.sorting;

import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.Tuple;

import java.util.*;

/**
 * The PreviousProductAssociation class is a specific implementation of the Association class,
 * designed to associate EOProduct objects based on their geographic footprint and acquisition dates.
 * It produces pairs of EOProducts where the second product in the pair follows the first
 * product chronologically and both products share the same geographic footprint.
 *
 * Key Behavior:
 * - The associate method groups the input EOProduct list by their shared geometry (footprint).
 * - Within each group of products with the same geometry, it sorts the products by acquisition date.
 * - Creates pairs of products where the first product's acquisition date is earlier than
 *   the second product's acquisition date.
 *
 * Description:
 * - This association is described using the friendly name: "Pair by Same Footprint and Previous Date".
 * - The description method returns a String array containing this name.
 */
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
