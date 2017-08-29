package ro.cs.tao.persistence;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ro.cs.tao.datasource.AbstractDataSource;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.scihub.SciHubDataQuery;
import ro.cs.tao.datasource.remote.scihub.SciHubDataSource;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.persistence.config.DatabaseConfiguration;
import ro.cs.tao.persistence.data.DataSourceType;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by oana on 7/18/2017.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:tao-persistence-context.xml")
public class PersistenceManagerTest {

    private static Log logger = LogFactory.getLog(PersistenceManagerTest.class);

    /**
     * Instance of the persistence manager
     */
    @Autowired
    private PersistenceManager persistenceManager;

    /**
     * Instance of the DB configuration
     */
    @Autowired
    private DatabaseConfiguration dbConfig;

    @Test
    public void check_DB_configuration()
    {
        try
        {
            Assert.assertTrue(dbConfig.dataSource() != null);
            Assert.assertTrue(dbConfig.dataSource().getConnection() != null);
        }
        catch (SQLException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void save_new_data_source()
    {
        DataSourceType dataSourceType = null;
        try {
            Logger logger = LogManager.getLogManager().getLogger("");
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(Level.INFO);
            }
            AbstractDataSource<SciHubDataQuery> dataSource = new SciHubDataSource();
            dataSource.setCredentials("kraftek", "cei7pitici.");

            List<DataSourceType> savedDataSourceTypes = persistenceManager.getDataSourceTypes();

            for (DataSourceType savedDataSourceType : savedDataSourceTypes)
            {
                if (savedDataSourceType.getType().contains("SCIHUB_SENTINEL_1_DATA_SOURCE"))
                {
                    dataSourceType = savedDataSourceType;
                    break;
                }
            }

            if (dataSourceType == null)
            {
                // save new data source type
                dataSourceType = persistenceManager.getDataSourceTypeById(persistenceManager.saveDataSourceType("SCIHUB_SENTINEL_1_DATA_SOURCE"));
            }

            persistenceManager.saveDataSource(dataSource, dataSourceType, "SciHub Sentinel-1 Data Source", "No description");

        } catch (URISyntaxException | PersistenceException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void save_new_data_product()
    {
        try {
            Logger logger = LogManager.getLogManager().getLogger("");
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(Level.INFO);
            }
            DataSource<SciHubDataQuery> dataSource = new SciHubDataSource();
            dataSource.setCredentials("kraftek", "cei7pitici.");

            DataQuery query = dataSource.createQuery();
            query.addParameter("platformName", "Sentinel-2");
            QueryParameter begin = query.createParameter("beginPosition", Date.class);
            begin.setMinValue(Date.from(LocalDateTime.of(2016, 2, 1, 0, 0, 0, 0)
              .atZone(ZoneId.systemDefault())
              .toInstant()));
            begin.setMaxValue(Date.from(LocalDateTime.of(2017, 2, 1, 0, 0, 0, 0)
              .atZone(ZoneId.systemDefault())
              .toInstant()));
            query.addParameter(begin);
            Polygon2D aoi = Polygon2D.fromWKT("POLYGON((22.8042573604346 43.8379609098684," +
              "24.83885442747927 43.8379609098684," +
              "24.83885442747927 44.795645304033826," +
              "22.8042573604346 44.795645304033826," +
              "22.8042573604346 43.8379609098684))");

            query.addParameter("footprint", aoi);

            query.addParameter("cloudcoverpercentage", 100.);
            query.setPageSize(50);
            query.setMaxResults(83);
            List<EOProduct> results = query.execute();
            if(results.size() > 0)
            {
                // save first result, for example
                EOProduct dataProduct = results.get(0);

                persistenceManager.saveDataProduct(dataProduct, null);
            }
            else
            {
                logger.info("save_new_data_product() - No result found!");
            }

        } catch (URISyntaxException | QueryException | PersistenceException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void save_new_execution_node()
    {
        try
        {
            // add a new execution node for test
            final Integer executionNodeId = persistenceManager.saveExecutionNode("No name", "No description", "No IP", null, "username", "password", 10, 10, 10);
            // check persisted ID
            Assert.assertTrue(executionNodeId != null && executionNodeId > 0);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }
}
