import ro.cs.tao.datasource.DefaultProductPathBuilder;
import ro.cs.tao.datasource.ProductPathBuilder;
import ro.cs.tao.eodata.EOProduct;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Properties;

public class ProductPathTest {

    public static void main(String[] args) {
        Path repositoryPath = Paths.get("/mnt/products");
        String localPathFormat = ".";
        Properties properties = new Properties();
        properties.put(ProductPathBuilder.LOCAL_ARCHIVE_PATH_FORMAT, ".");
        properties.put(ProductPathBuilder.PATH_SUFFIX, "none");
        properties.put(ProductPathBuilder.PRODUCT_FORMAT, "zip");
        EOProduct product = new EOProduct();
        product.setName("S1A_IW_SLC__1SDV_20181105T174115_20181105T174142_024456_02AE49_5134.SAFE");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2018, Calendar.NOVEMBER, 5);
        product.setAcquisitionDate(LocalDateTime.of(2018, 11, 5, 17, 41, 15));
        ProductPathBuilder pathBuilder = new DefaultProductPathBuilder(repositoryPath, localPathFormat, properties, true);
        Path path = pathBuilder.getProductPath(repositoryPath, product);
        if (!path.toString().contains(product.getName()) &&
                !(path.toString().toLowerCase().endsWith(".zip") || path.toString().toLowerCase().endsWith(".tar.gz"))) {
            path = path.resolve(product.getName());
        }
        System.out.println(path.toUri().toString());
        System.exit(0);
    }
}
