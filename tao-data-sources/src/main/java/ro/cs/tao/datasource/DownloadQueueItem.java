package ro.cs.tao.datasource;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.Crypto;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class DownloadQueueItem extends StringIdentifiable {
    private String dataSourceName;
    private List<EOProduct> products;
    private Set<String> tiles;
    private String destinationPath;
    private String localRootPath;
    private Properties properties;

    public static String computeId(DownloadQueueItem item) {
        List<String> strings = item.getProducts().stream().map(EOProduct::getId).collect(Collectors.toList());
        strings.add(item.getDataSourceName());
        strings.add(item.getDestinationPath());
        return Crypto.hash(strings);
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public List<EOProduct> getProducts() {
        return products;
    }

    public void setProducts(List<EOProduct> products) {
        this.products = products;
    }

    public Set<String> getTiles() {
        return tiles;
    }

    public void setTiles(Set<String> tiles) {
        this.tiles = tiles;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public String getLocalRootPath() {
        return localRootPath;
    }

    public void setLocalRootPath(String localRootPath) {
        this.localRootPath = localRootPath;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
