package ro.cs.tao.datasource;

import ro.cs.tao.eodata.util.ProductHelper;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A factory class for creating instances of {@link ProductHelper} based on the given product name.
 * This class interacts with a {@link ServiceRegistry} to retrieve and sort registered services
 * of type {@link ProductHelper}, and selects an appropriate helper implementation based on
 * the provided product name.
 */
public class ProductHelperFactory {

    /**
     * Fetches an appropriate instance of {@link ProductHelper} based on the given product name.
     * The method retrieves all available {@link ProductHelper} instances from the
     * {@link ServiceRegistry}, orders them based on their priority, and selects the
     * first helper that is intended for the specified product name.
     *
     * @param productName The name of the product for which a helper is sought.
     * @return A duplicate of the matching {@link ProductHelper} instance with its name set to
     *         the specified product name, or {@code null} if no suitable helper is found.
     */
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
