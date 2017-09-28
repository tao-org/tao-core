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
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.execution.ExecutionJob;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
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
import ro.cs.tao.datasource.remote.peps.Collection;
import ro.cs.tao.datasource.remote.peps.PepsDataSource;
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
import java.util.ArrayList;
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
            node.setHostName("Test_host_name");
            node.setUserName("Test user name");
            node.setUserPass("Test user pass");
            node.setProcessorCount(2);
            node.setMemorySizeGB(10);
            node.setDiskSpaceSizeGB(1000);

            node.setDescription("Node just for test");

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
    public void TC_06_delete_execution_node()
    {
        try
        {
            final List<NodeDescription> nodes  = persistenceManager.getNodes();
            if(nodes.size() > 0)
            {
                final NodeDescription firstNode = nodes.get(0);
                final String hostName = firstNode.getHostName();
                final NodeDescription deletedNode = persistenceManager.deleteExecutionNode(hostName);

                Assert.assertTrue(deletedNode != null && deletedNode.getActive() == false);
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_07_save_new_processing_component()
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

            List<Variable> variables = new ArrayList<>();

            final Variable var1 = new Variable();
            var1.setKey("var1");
            var1.setValue("value1");
            variables.add(var1);

            final Variable var2 = new Variable();
            var2.setKey("var2");
            var2.setValue("value2");
            variables.add(var2);

            component.setVariables(variables);

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

    @Test
    public void TC_08_retrieve_processing_components()
    {
        try
        {
            List<ProcessingComponent> components  = persistenceManager.getProcessingComponents();
            Assert.assertTrue(components != null && components.size() > 0);

            logger.info("Found " + components.size() + " processing components.");

            for (ProcessingComponent component : components)
            {
                logger.info("Found component " + component.getId());
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_09_update_processing_component()
    {
        try
        {
            List<ProcessingComponent> components  = persistenceManager.getProcessingComponents();
            if(components.size() > 0)
            {
                ProcessingComponent firstComponent = components.get(0);
                firstComponent.setDescription("Description updated");
                firstComponent = persistenceManager.updateProcessingComponent(firstComponent);
                Assert.assertTrue(firstComponent.getDescription().equals("Description updated"));
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_10_check_processing_component_existence_by_id()
    {
        try
        {
            List<ProcessingComponent> components  = persistenceManager.getProcessingComponents();;

            if(components.size() > 0)
            {
                ProcessingComponent firstComponent = components.get(0);
                String identifier = firstComponent.getId();
                Assert.assertTrue(persistenceManager.checkIfExistsComponentById(identifier));
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_11_retrieve_processing_component_by_id()
    {
        try
        {
            List<ProcessingComponent> components  = persistenceManager.getProcessingComponents();;
            if(components.size() > 0)
            {
                ProcessingComponent firstComponent = components.get(0);
                String identifier = firstComponent.getId();
                ProcessingComponent searchedComponent  = persistenceManager.getProcessingComponentById(identifier);
                Assert.assertTrue(searchedComponent != null && searchedComponent.getId().equals(identifier));
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_12_save_new_execution_job()
    {
        try
        {
            // add a new job for test
            ExecutionJob job = new ExecutionJob();
            job.setResourceId("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);

            job = persistenceManager.saveExecutionJob(job);
            // check persisted job
            Assert.assertTrue(job != null && job.getId() != null);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_13_save_new_execution_task()
    {
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.getAllJobs();
            // retrieve processing components
            List<ProcessingComponent> components = persistenceManager.getProcessingComponents();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.getNodes();

            if(jobs != null && jobs.size() > 0 &&
              components != null && components.size() > 0 &&
              nodes != null && nodes.size() > 0)
            {
                // retrieve first job
                ExecutionJob job = jobs.get(0);
                // retrieve first component
                ProcessingComponent component = components.get(0);

                NodeDescription node = nodes.get(0);

                // add a new task for test
                ExecutionTask task = new ExecutionTask();
                task.setResourceId("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
                task.setExecutionStatus(ExecutionStatus.RUNNING);
                task.setExecutionNodeHostName(node.getHostName());
                task.setProcessingComponent(component);

                task = persistenceManager.saveExecutionTask(task, job);
                // check persisted task
                Assert.assertTrue(task != null && task.getId() != null);
                // check if job correctly updated
                Assert.assertTrue(job.getTasks().contains(task));
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_14_get_running_execution_tasks()
    {
        try
        {
            // retrieve running tasks
            List<ExecutionTask> tasks = persistenceManager.getRunningTasks();

            Assert.assertTrue(tasks != null && tasks.size() > 0);

            for(ExecutionTask task: tasks)
            {
                logger.info("Running task: " + task.getResourceId());
            }

        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    /**@Test
    public void TC_12_save_new_data_product()
    {
//        try
//        {
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
                    persistenceManager.saveEOProduct(eoProduct);
                }catch (Exception e)
                {
                    e.printStackTrace();
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
//        }
//        catch (PersistenceException e)
//        {
//            logger.error(ExceptionUtils.getStackTrace(e));
//            Assert.fail(e.getMessage());
//        }
    }**/

}
