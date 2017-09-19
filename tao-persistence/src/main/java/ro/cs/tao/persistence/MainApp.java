package ro.cs.tao.persistence;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.peps.Collection;
import ro.cs.tao.datasource.remote.peps.PepsDataSource;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.persistence.config.DatabaseConfiguration;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeDescription;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Just for test purpose
 */
public class MainApp {

    private static final Logger logger = LogManager.getLogManager().getLogger("");

	public static void main(String[] args) {

		ApplicationContext context = new ClassPathXmlApplicationContext("tao-persistence-context.xml");

		// load the DB configuration
		DatabaseConfiguration dbConfig = context.getBean(DatabaseConfiguration.class);
		if (dbConfig != null)
		{
			logger.info("DatabaseConfiguration loaded!");
		}

		// load the DB manager
		PersistenceManager dbManager = context.getBean(PersistenceManager.class);
		if(dbManager != null)
		{
			logger.info("PersistenceManager loaded!");
            saveNewProduct(dbManager);
            saveNewNode(dbManager);

        }

        // close application context
		((ClassPathXmlApplicationContext) context).close();
		logger.info("ApplicationContext closed.");
	}

    private static void saveNewNode(PersistenceManager dbManager) {
        NodeDescription node  = new NodeDescription();
        node.setHostName("No host name");
        node.setUserName("No user name");
        node.setUserPass("No user pass");
        node.setProcessorCount(2);
        node.setMemorySizeGB(10);
        node.setDiskSpaceSizeGB(1000);

        try {
            dbManager.saveExecutionNode(node);
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Error saving node", e);
        }
    }

    private static void saveNewProduct(PersistenceManager dbManager) {
        // try to perform an operation on BD
        ServiceRegistry<DataSource> serviceRegistry =
          ServiceRegistryManager.getInstance().getServiceRegistry(DataSource.class);

        DataSource dataSource = serviceRegistry.getService(PepsDataSource.class.getName());
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

        if(!results.isEmpty())
        {
            // save only the first result, for example
            EOProduct eoProduct = (EOProduct)results.get(0);
            try {
                dbManager.saveEOProduct(eoProduct);
            } catch (PersistenceException e) {
                    logger.log(Level.SEVERE, "Error saving EOProduct", e);
            }


//                // save all results
//                for(EOProduct result : results)
//                {
//                    try
//                    {
//                        dbManager.saveEOProduct((EOProduct) result);
//                    } catch (PersistenceException e) {
//                        logger.log(Level.SEVERE, "Error saving EOProduct", e);
//                    }
//                }
        }
        else
        {
            logger.info("No EO product found with the given query!");
        }
    }
}
