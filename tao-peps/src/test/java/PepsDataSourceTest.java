import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.peps.Collection;
import ro.cs.tao.datasource.remote.peps.PepsDataQuery;
import ro.cs.tao.datasource.remote.peps.PepsDataSource;
import ro.cs.tao.eodata.EOData;
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
            DataSource<EOData, PepsDataQuery> dataSource =
                    serviceRegistry.getService(PepsDataSource.class.getName());

            dataSource.setCredentials("kraftek@c-s.ro", "cei7pitici.");

            DataQuery<EOData> query = dataSource.createQuery();
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
            List<EOData> results = query.execute();
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
}
