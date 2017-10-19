import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.peps.Collection;
import ro.cs.tao.datasource.remote.peps.PepsDataSource;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class PepsDataSourceTest {

    public static void main(String[] args) {
        Peps_Sentinel2_Test();
    }

    public static void Peps_Sentinel2_Test() {
        try {
            ServiceRegistry<DataSource> serviceRegistry =
                    ServiceRegistryManager.getInstance().getServiceRegistry(DataSource.class);
            Logger logger = LogManager.getLogManager().getLogger("");
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(Level.INFO);
            }
            DataSource dataSource = serviceRegistry.getService(PepsDataSource.class);
            dataSource.setCredentials("kraftek@c-s.ro", "cei7pitici.");
            String[] sensors = dataSource.getSupportedSensors();

            DataQuery query = dataSource.createQuery(sensors[1]);
            query.addParameter("collection", Collection.S2ST.toString());
            query.addParameter("platform", "S2A");

            QueryParameter begin = query.createParameter("startDate", Date.class);
            begin.setValue(Date.from(LocalDateTime.of(2017, 2, 1, 0, 0, 0, 0)
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant()));
            query.addParameter(begin);
            QueryParameter end = query.createParameter("completionDate", Date.class);
            begin.setValue(Date.from(LocalDateTime.of(2017, 3, 1, 0, 0, 0, 0)
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant()));
            query.addParameter(end);
            Polygon2D aoi = Polygon2D.fromWKT("POLYGON((22.8042573604346 43.8379609098684," +
                                                      "24.83885442747927 43.8379609098684," +
                                                      "24.83885442747927 44.795645304033826," +
                                                      "22.8042573604346 44.795645304033826," +
                                                      "22.8042573604346 43.8379609098684))");

            query.addParameter("box", aoi);

            query.addParameter("cloudCover", 100.);
            query.setPageSize(20);
            query.setMaxResults(50);
            List<EOProduct> results = query.execute();
            results.forEach(r -> {
                System.out.println("ID=" + r.getId());
                System.out.println("NAME=" + r.getName());
                System.out.println("LOCATION=" + r.getLocation());
                System.out.println("FOOTPRINT=" + r.getGeometry());
                System.out.println("Attributes ->");
//                Arrays.stream(r.getAttributes())
//                        .forEach(a -> System.out.println("\tName='" + a.getName() +
//                                                                 "', value='" + a.getValue() + "'"));
                r.getAttributes().stream()
                  .forEach(a -> System.out.println("\tName='" + a.getName() +
                    "', value='" + a.getValue() + "'"));
            });
        } catch (QueryException e) {
            e.printStackTrace();
        }
    }
}
