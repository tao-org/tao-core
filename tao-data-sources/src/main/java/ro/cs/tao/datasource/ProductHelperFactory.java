package ro.cs.tao.datasource;

import ro.cs.tao.eodata.util.ProductHelper;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductHelperFactory {

    public static ProductHelper getHelper(String productName) {
        final ServiceRegistry<ProductHelper> registry = ServiceRegistryManager.getInstance().getServiceRegistry(ProductHelper.class);
        final List<ProductHelper> helpers = new ArrayList<>(registry.getServices());
        ProductHelper helper = null;
        if (helpers != null) {
            helpers.sort(Comparator.comparingInt(ProductHelper::order));
            for (ProductHelper h : helpers) {
                if (h.isIntended(productName)) {
                    h.setName(productName);
                    helper = h.duplicate();
                    break;
                }
            }
        }
        return helper;
    }

}
