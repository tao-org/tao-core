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
import ro.cs.tao.datasource.common.DataQuery;
import ro.cs.tao.datasource.common.DataSource;
import ro.cs.tao.datasource.common.QueryException;
import ro.cs.tao.datasource.common.QueryParameter;
import ro.cs.tao.datasource.remote.scihub.SciHubDataQuery;
import ro.cs.tao.datasource.remote.scihub.SciHubDataSource;
import ro.cs.tao.datasource.remote.scihub.SentinelDownloader;
import ro.cs.tao.datasource.util.Polygon2D;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.config.DatabaseConfiguration;
import ro.cs.tao.persistence.data.enums.DataSourceType;

import java.net.URISyntaxException;
import java.sql.SQLException;
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
        try {
            Logger logger = LogManager.getLogManager().getLogger("");
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(Level.INFO);
            }
            DataSource<EOData, SciHubDataQuery> dataSource = new SciHubDataSource();
            dataSource.setCredentials("kraftek", "cei7pitici.");

            persistenceManager.saveDataSource(dataSource, DataSourceType.SCIHUB_SENTINEL_1_DATA_SOURCE,
              "SciHub Sentinel-1 Data Source", "No description");

        } catch (URISyntaxException e) {
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
            DataSource<EOData, SciHubDataQuery> dataSource = new SciHubDataSource();
            dataSource.setCredentials("kraftek", "cei7pitici.");

            DataQuery<EOData> query = dataSource.createQuery();
            query.addParameter("platformName", "Sentinel-2");
            QueryParameter begin = query.createParameter("beginPosition", Date.class);
            begin.setMinValue(Date.from(LocalDateTime.of(2017, 5, 30, 0, 0, 0, 0)
              .atZone(ZoneId.systemDefault())
              .toInstant()));
            begin.setMaxValue(Date.from(LocalDateTime.of(2017, 6, 1, 0, 0, 0, 0)
              .atZone(ZoneId.systemDefault())
              .toInstant()));
            query.addParameter(begin);
            query.addParameter("polarisationMode", "VV");
            query.addParameter("sensorOperationalMode", "IW");
            query.addParameter("productType", "SLC");
            query.setPageSize(50);
            query.setMaxResults(83);
            SentinelDownloader downloader = new SentinelDownloader("E:\\NewFormat");
            List<EOData> results = query.execute();
            if(results.size() > 0)
            {
                // save first result, for example
                EOProduct dataProduct = (EOProduct)results.get(0);

                persistenceManager.saveDataProduct(dataProduct, null);
            }

        } catch (URISyntaxException | QueryException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }

    }
}
