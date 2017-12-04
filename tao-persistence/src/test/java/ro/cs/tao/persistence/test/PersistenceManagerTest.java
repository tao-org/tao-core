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
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ParameterType;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.execution.ExecutionJob;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.eodata.Attribute;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.VectorData;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.config.DatabaseConfiguration;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeServiceStatus;
import ro.cs.tao.topology.ServiceDescription;
import ro.cs.tao.topology.ServiceStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void TC_01_01_save_new_execution_node_new_services()
    {
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setHostName("Test1_host_name");
            node.setUserName("Test1 user name");
            node.setUserPass("Test1 user pass");
            node.setProcessorCount(2);
            node.setMemorySizeGB(10);
            node.setDiskSpaceSizeGB(1000);

            node.setDescription("Node1 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.INSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.NOT_FOUND));
            node.setServicesStatus(servicesStatus);

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
    public void TC_01_02_save_new_execution_node_existing_services()
    {
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setHostName("Test2_host_name");
            node.setUserName("Test2 user name");
            node.setUserPass("Test2 user pass");
            node.setProcessorCount(2);
            node.setMemorySizeGB(10);
            node.setDiskSpaceSizeGB(1000);

            node.setDescription("Node2 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.UNINSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.UNINSTALLED));
            node.setServicesStatus(servicesStatus);

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
    public void TC_01_03_save_new_execution_node_mixt_services()
    {
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setHostName("Test3_host_name");
            node.setUserName("Test3 user name");
            node.setUserPass("Test3 user pass");
            node.setProcessorCount(2);
            node.setMemorySizeGB(10);
            node.setDiskSpaceSizeGB(1000);

            node.setDescription("Node3 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "2.0", "Docker description"), ServiceStatus.INSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.INSTALLED));
            node.setServicesStatus(servicesStatus);

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
            // add a new execution node in order to deactivate it
            NodeDescription node  = new NodeDescription();
            node.setHostName("hostname2");
            node.setUserName("Test user name");
            node.setUserPass("Test user pass");
            node.setProcessorCount(2);
            node.setMemorySizeGB(10);
            node.setDiskSpaceSizeGB(1000);

            node.setDescription("Node 2 just for test");

            node = persistenceManager.saveExecutionNode(node);
            // check persisted node
            Assert.assertTrue(node != null && node.getHostName() != null);

            // deactivate node
            node = persistenceManager.deleteExecutionNode(node.getHostName());

            Assert.assertTrue(node != null && node.getActive() == false);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_07_01_save_new_container()
    {
        try
        {
            // add a new container for test
            Container container = new Container();
            container.setId("container01");
            container.setName("container for test");
            container.setTag("container tag");
            container.setApplicationPath(".\\mypath");

            // list of container applications
            List<Application> applications = new ArrayList<>();

            final Application app1 = new Application();
            app1.setPath(".\\mypath1");
            app1.setName("App1");
            applications.add(app1);

            final Application app2 = new Application();
            app2.setPath(".\\mypath2");
            app2.setName("App2");
            applications.add(app2);

            container.setApplications(applications);

            container = persistenceManager.saveContainer(container);
            // check persisted container
            Assert.assertTrue(container != null && container.getId() != null);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_07_02_retrieve_containers()
    {
        try
        {
            List<Container> containers  = persistenceManager.getContainers();
            Assert.assertTrue(containers != null && containers.size() > 0);

            logger.info("Found " + containers.size() + " container(s).");

            for (Container container : containers)
            {
                logger.info("Found container " + container.getId());
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_07_03_delete_container()
    {
        try
        {
            // add a new container for test
            Container container = new Container();
            container.setId("container02");
            container.setName("container for test");
            container.setTag("container tag");
            container.setApplicationPath(".\\mypath");

            // list of container applications
            List<Application> applications = new ArrayList<>();

            final Application app1 = new Application();
            app1.setPath(".\\mypath1");
            app1.setName("App1");
            applications.add(app1);

            final Application app2 = new Application();
            app2.setPath(".\\mypath2");
            app2.setName("App2");
            applications.add(app2);

            container.setApplications(applications);

            container = persistenceManager.saveContainer(container);
            // check persisted container
            Assert.assertTrue(container != null && container.getId() != null);

            // delete container
            persistenceManager.deleteContainer(container.getId());

            // check that the container was deleted
            Assert.assertTrue(!persistenceManager.checkIfExistsContainerById("container02"));
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

            component.setVisibility(ProcessingComponentVisibility.CONTRIBUTOR);
            component.setNodeAffinity("Any");
            component.setMultiThread(true);
            component.setActive(true);

            component.setContainerId("container01");

            // list of component variables
            Set<Variable> variables = new HashSet<>();

            final Variable var1 = new Variable();
            var1.setKey("var1");
            var1.setValue("value1");
            variables.add(var1);

            final Variable var2 = new Variable();
            var2.setKey("var2");
            var2.setValue("value2");
            variables.add(var2);

            component.setVariables(variables);

            // list of component parameters
            List<ParameterDescriptor> parameters = new ArrayList<>();

            final ParameterDescriptor param1 = new ParameterDescriptor();
            param1.setId("testParam1");
            param1.setType(ParameterType.REGULAR);
            param1.setDataType(String.class);
            param1.setLabel("Test Param 1");
            parameters.add(param1);

            final ParameterDescriptor param2 = new ParameterDescriptor();
            param2.setId("testParam2");
            param2.setType(ParameterType.REGULAR);
            param2.setDataType(Integer.class);
            param2.setLabel("Test Param 2");
            parameters.add(param2);

            component.setParameterDescriptors(parameters);

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

            logger.info("Found " + components.size() + " processing component(s).");

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
    public void TC_13_delete_processing_component()
    {
        try
        {
            // add a new processing component for test
            ProcessingComponent component = new ProcessingComponent();
            component.setId("delete_0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
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

            component.setVisibility(ProcessingComponentVisibility.CONTRIBUTOR);
            component.setNodeAffinity("Any");
            component.setMultiThread(true);
            component.setActive(true);

            component.setContainerId("container01");

            Set<Variable> variables = new HashSet<>();

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

            // deactivate component
            component = persistenceManager.deleteProcessingComponent(component.getId());

            Assert.assertTrue(component != null && !component.getActive());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_14_save_new_execution_job()
    {
        try
        {
            // add a new job for test
            ExecutionJob job = new ExecutionJob();
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
    public void TC_15_save_new_execution_task()
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
    public void TC_16_get_running_execution_tasks()
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

    @Test
    public void TC_17_save_new_data_products()
    {
        try {
            EOProduct product = new EOProduct();
            product.setId("LC82010442017273LGN00");
            product.setName("LC08_L1TP_201044_20170930_20171013_01_T1");
            product.setAcquisitionDate(Date.from(LocalDateTime.of(2017, 9, 30, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));
            product.setSensorType(SensorType.OPTICAL);
            product.setProductType("Landsat-8");
            product.setApproximateSize(1700000000);
            product.setPixelType(PixelType.UINT16);
            product.setWidth(7601);
            product.setHeight(7761);
            product.setFormatType(DataFormat.RASTER);
            product.setGeometry("POLYGON ((24.16023 -9.60737, 24.15266 -7.36319, 22.05055 -7.38847, 22.05739 -9.59798, 24.16023 -9.60737))");
            product.setLocation("https://landsat-pds.s3.amazonaws.com/c1/L8/201/044/LC08_L1TP_201044_20170930_20171013_01_T1");

            persistenceManager.saveEOProduct(product);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_18_retrieve_raster_data_products()
    {
        try
        {
            List<EOProduct> products  = persistenceManager.getEOProducts();
            Assert.assertTrue(products != null && products.size() > 0);

            logger.info("Found " + products.size() + " raster data product(s).");

            for (EOProduct product : products)
            {
                logger.info("\t\tFound raster product \"" + product.getName() + "\"");
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_19_save_new_vector_data_product()
    {
        try
        {
            VectorData newVectorProduct = new VectorData();
            newVectorProduct.setId("test-vector-product" + LocalDateTime.now().toString());
            newVectorProduct.setName("test vector data product");
            newVectorProduct.setFormatType(DataFormat.VECTOR);
            newVectorProduct.setGeometry("POLYGON((0 0, 1 1, 2 2, 3 3, 0 0))");
            newVectorProduct.setLocation("nowhere");

            // attributs
            List<Attribute> attributes = new ArrayList<>();
            Attribute attr1 = new Attribute();
            attr1.setName("attr1");
            attr1.setValue("value1-attr1");
            attributes.add(attr1);
            Attribute attr2 = new Attribute();
            attr2.setName("attr2");
            attr2.setValue("value2-attr2");
            attributes.add(attr2);

            newVectorProduct.setAttributes(attributes);

            newVectorProduct = persistenceManager.saveVectorDataProduct(newVectorProduct);
            Assert.assertTrue(newVectorProduct != null && newVectorProduct.getId() != null);

        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_20_retrieve_vector_data_products()
    {
        try
        {
            List<VectorData> vectorProducts  = persistenceManager.getVectorDataProducts();
            Assert.assertTrue(vectorProducts != null && vectorProducts.size() > 0);

            logger.info("Found " + vectorProducts.size() + " vector data product(s).");

            for (VectorData vectorProduct : vectorProducts)
            {
                logger.info("\t\tFound vector product \"" + vectorProduct.getName() + "\"");
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_21_save_new_notification()
    {
        try
        {
            Message message =  new Message();
            message.setTimestamp(System.currentTimeMillis());
            message.setUserId(1);
            message.setRead(true);
            message.setData("notification data");
            message.setSource("notification source");

            try
            {
                persistenceManager.saveMessage(message);
            } catch (PersistenceException e) {
                logger.error("Error saving notification message", e);
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

}
