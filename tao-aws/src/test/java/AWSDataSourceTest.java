import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.aws.AWSDataSource;
import ro.cs.tao.datasource.remote.aws.LandsatProduct;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class AWSDataSourceTest {

    public static void main(String[] args) {
        //Sentinel2_Test();
        Landsat8_Test();
    }

    public static void Sentinel2_Test() {
        try {
            Logger logger = LogManager.getLogManager().getLogger("");
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(Level.INFO);
            }
            ServiceRegistry<DataSource> serviceRegistry = getServiceRegistry();
            DataSource dataSource = serviceRegistry.getService(AWSDataSource.class.getName());
            String[] sensors = dataSource.getSupportedSensors();

            DataQuery query = dataSource.createQuery(sensors[0]);
            //query.addParameter("platformName", "S2");
            QueryParameter begin = query.createParameter("beginPosition", Date.class);
            begin.setValue(Date.from(LocalDateTime.now().minusDays(15)
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant()));
            query.addParameter(begin);
            Polygon2D aoi = new Polygon2D();
            aoi.append(-9.9866909768, 23.4186029838);
            aoi.append(-8.9037319257, 23.4186029838);
            aoi.append(-8.9037319257, 24.413397299);
            aoi.append(-9.9866909768, 24.413397299);
            aoi.append(-9.9866909768, 23.4186029838);
            query.addParameter("footprint", aoi.toWKT());

            query.addParameter("cloudcoverpercentage", 100.);
            query.setPageSize(50);
            query.setMaxResults(83);
            List<EOProduct> results = query.execute();
            results.forEach(r -> {
                System.out.println("ID=" + r.getId());
                System.out.println("NAME=" + r.getName());
                System.out.println("LOCATION=" + r.getLocation().toString());
                System.out.println("FOOTPRINT=" + r.getGeometry().toText());
                System.out.println("Attributes ->");
                Arrays.stream(r.getAttributes())
                        .forEach(a -> System.out.println("\tName='" + a.getName() +
                                                                 "', value='" + a.getValue() + "'"));
            });
        } catch (QueryException e) {
            e.printStackTrace();
        }
    }

    public static void Landsat8_Test() {
        try {
            Logger logger = LogManager.getLogManager().getLogger("");
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(Level.INFO);
            }
            ServiceRegistry<DataSource> serviceRegistry = getServiceRegistry();
            DataSource dataSource = serviceRegistry.getService(AWSDataSource.class.getName());
            String[] sensors = dataSource.getSupportedSensors();
            DataQuery query = dataSource.createQuery(sensors[1]);
            //query.addParameter("platformName", "Landsat-8");
            QueryParameter begin = query.createParameter("sensingStart", Date.class);
            begin.setValue(Date.from(LocalDateTime.now().minusDays(30)
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant()));
            query.addParameter(begin);
            Polygon2D aoi = new Polygon2D();
            aoi.append(-9.9866909768, 23.4186029838);
            aoi.append(-8.9037319257, 23.4186029838);
            aoi.append(-8.9037319257, 24.413397299);
            aoi.append(-9.9866909768, 24.413397299);
            aoi.append(-9.9866909768, 23.4186029838);
            query.addParameter("footprint", aoi.toWKT());
            //query.addParameter("collection", "COLLECTION_1");
            query.addParameter("cloudcoverpercentage", 100.);
            query.addParameter("productType", LandsatProduct.T1.toString());
            query.setPageSize(50);
            query.setMaxResults(83);
            List<EOProduct> results = query.execute();
            results.forEach(r -> {
                System.out.println("ID=" + r.getId());
                System.out.println("NAME=" + r.getName());
                System.out.println("LOCATION=" + r.getLocation().toString());
                System.out.println("FOOTPRINT=" + r.getGeometry().toText());
                System.out.println("Attributes ->");
                Arrays.stream(r.getAttributes())
                        .forEach(a -> System.out.println("\tName='" + a.getName() +
                                                                 "', value='" + a.getValue() + "'"));
            });
        } catch (QueryException e) {
            e.printStackTrace();
        }
    }

    private static ServiceRegistry<DataSource> getServiceRegistry() {
        return ServiceRegistryManager.getInstance().getServiceRegistry(DataSource.class);
    }
}
