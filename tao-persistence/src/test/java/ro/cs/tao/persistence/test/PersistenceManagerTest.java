package ro.cs.tao.persistence.test;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.component.template.engine.TemplateEngine;
import ro.cs.tao.datasource.AbstractDataSource;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.scihub.SciHubDataQuery;
import ro.cs.tao.datasource.remote.scihub.SciHubDataSource;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.config.DatabaseConfiguration;
import ro.cs.tao.persistence.data.DataSourceType;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeDescription;

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
@ImportResource({"classpath:META-INF/persistence.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    public void TC_00_check_DB_configuration()
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


    /**@Test
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

    }**/

    /**@Test
    public void save_new_data_product()
    {
        try {
            Logger logger = LogManager.getLogManager().getLogger("");
            for (Handler handler : logger.getHandlers()) {
                handler.setLevel(Level.INFO);
            }
            DataSource dataSource = getDatasourceRegistry().getService(SciHubDataSource.class.getName());
            dataSource.setCredentials("kraftek", "cei7pitici.");
            String[] sensors = dataSource.getSupportedSensors();

            DataQuery query = dataSource.createQuery(sensors[1]);
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
                // save all results
                for(EOProduct result : results)
                {
                    persistenceManager.saveDataProduct((EOProduct)result, null);

                }

                // save only the first result, for example
                //EOProduct dataProduct = (EOProduct)results.get(0);
                //persistenceManager.saveDataProduct(dataProduct, null);
            }
            else
            {
                logger.info("save_new_data_product() - No result found!");
            }

        } catch (QueryException | PersistenceException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }

    }**/

    @Test
    public void TC_01_save_new_execution_node()
    {
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setHostName("Test1 host name");
            node.setUserName("Test user name");
            node.setUserPass("Test user pass");
            node.setProcessorCount(2);
            node.setMemorySizeGB(10);
            node.setDiskSpaceSizeGB(1000);

            node = persistenceManager.saveExecutionNode(node);
            // check persisted node
            Assert.assertTrue(node != null && node.getHostName() != null);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_02_retrieve_all_execution_nodes()
    {
        try
        {

            List<NodeDescription> nodes  = persistenceManager.getNodes();
            Assert.assertTrue(nodes != null && nodes.size() > 0);

            for (NodeDescription node : nodes)
            {
                logger.info("Found node " + node.getHostName());
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_03_update_execution_node()
    {
        try
        {
            List<NodeDescription> nodes  = persistenceManager.getNodes();

            if(nodes.size() > 0)
            {
                NodeDescription firstNode = nodes.get(0);

                firstNode.setDiskSpaceSizeGB(9);
                firstNode = persistenceManager.updateExecutionNode(firstNode);

                Assert.assertTrue(firstNode.getDiskSpaceSizeGB() == 9);
            }

        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_04_check_execution_node_existence_by_host_name()
    {
        try
        {
            List<NodeDescription> nodes  = persistenceManager.getNodes();

            if(nodes.size() > 0)
            {
                NodeDescription firstNode = nodes.get(0);
                String hostName = firstNode.getHostName();
                Assert.assertTrue(persistenceManager.checkIfExistsNodeByHostName(hostName));
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_05_retrieve_execution_node_by_host_name()
    {
        try
        {
            List<NodeDescription> nodes  = persistenceManager.getNodes();

            if(nodes.size() > 0)
            {
                NodeDescription firstNode = nodes.get(0);
                String hostName = firstNode.getHostName();

                NodeDescription searchedNode  = persistenceManager.getNodeByHostName(hostName);
                Assert.assertTrue(searchedNode != null && searchedNode.getHostName().equals(hostName));
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_06_save_new_processing_component()
    {
        try
        {
            // add a new processing component for test
            ProcessingComponent component = new ProcessingComponent();
            component.setId("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
            component.setLabel("component label");
            component.setVersion("component version");
            component.setDescription("component description");
            component.setAuthors("component authors");
            component.setCopyright("component copyright");
            component.setFileLocation("component file location");
            component.setWorkingDirectory("component working directory");

            Template template = new BasicTemplate();
            template.setName("basic template name");
            template.setTemplateType(TemplateType.VELOCITY);
            component.setTemplate(template);
            component.setTemplateType(TemplateType.VELOCITY);
            // TODO ??
            component.setTemplateName("basic template name");

            component.setVisibility(ProcessingComponentVisibility.CONTRIBUTOR);
            component.setNodeAffinity("Any");
            component.setMultiThread(true);

            component = persistenceManager.saveProcessingComponent(component);
            // check persisted component
            Assert.assertTrue(component != null && component.getId() != null);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

}
