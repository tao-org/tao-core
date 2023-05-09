package ro.cs.tao.datasource;

import ro.cs.tao.eodata.util.ProductHelper;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.Set;

public class ProductHelperFactory {

    public static ProductHelper getHelper(String productName) {
        final ServiceRegistry<ProductHelper> registry = ServiceRegistryManager.getInstance().getServiceRegistry(ProductHelper.class);
        final Set<ProductHelper> helpers = registry.getServices();
        ProductHelper helper = null;
        if (helpers != null) {
            for (ProductHelper h : helpers) {
                try {
                    h.setName(productName);
                    helper = h.duplicate();
                    break;
                } catch (Exception ignored) {
                }
            }
        }
        return helper;
    }

}
