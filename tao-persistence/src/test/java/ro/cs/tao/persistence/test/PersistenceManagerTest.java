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
import ro.cs.tao.component.*;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.eodata.Attribute;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.VectorData;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.config.DatabaseConfiguration;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.topology.*;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;
import ro.cs.tao.workflow.enums.Status;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.*;

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
    public void TC_01_check_DB_configuration()
    {
        logger.info("TC_01_check_DB_configuration");
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
            query.addParameter("platformName", "Sentinel2");
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
    public void TC_02_save_new_execution_node_new_services()
    {
        logger.info("TC_02_save_new_execution_node_new_services");
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setId("Test1_host_name");
            node.setUserName("Test1 user name");
            node.setUserPass("Test1 user pass");
            NodeFlavor flavor = new NodeFlavor() {{
                setCpu(2);
                setMemory(1024);
                setDisk(1000);
            }};
            node.setFlavor(flavor);

            node.setDescription("Node1 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.INSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.NOT_FOUND));
            node.setServicesStatus(servicesStatus);

            node = persistenceManager.saveExecutionNode(node);
            // check persisted node
            Assert.assertTrue(node != null && node.getId() != null);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_03_save_new_execution_node_existing_services()
    {
        logger.info("TC_03_save_new_execution_node_existing_services");
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setId("Test2_host_name");
            node.setUserName("Test2 user name");
            node.setUserPass("Test2 user pass");
            NodeFlavor flavor = new NodeFlavor() {{
                setCpu(2);
                setMemory(1024);
                setDisk(1000);
            }};
            node.setFlavor(flavor);

            node.setDescription("Node2 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.UNINSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.UNINSTALLED));
            node.setServicesStatus(servicesStatus);

            node = persistenceManager.saveExecutionNode(node);
            // check persisted node
            Assert.assertTrue(node != null && node.getId() != null);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_04_save_new_execution_node_mixt_services()
    {
        logger.info("TC_04_save_new_execution_node_mixt_services");
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setId("Test3_host_name");
            node.setUserName("Test3 user name");
            node.setUserPass("Test3 user pass");
            NodeFlavor flavor = new NodeFlavor() {{
                setCpu(2);
                setMemory(1024);
                setDisk(1000);
            }};
            node.setFlavor(flavor);

            node.setDescription("Node3 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "2.0", "Docker description"), ServiceStatus.INSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.INSTALLED));
            node.setServicesStatus(servicesStatus);

            node = persistenceManager.saveExecutionNode(node);
            // check persisted node
            Assert.assertTrue(node != null && node.getId() != null);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_05_retrieve_all_execution_nodes()
    {
        logger.info("TC_05_retrieve_all_execution_nodes");
        try
        {
            List<NodeDescription> nodes  = persistenceManager.getNodes();
            Assert.assertTrue(nodes != null && nodes.size() > 0);
            for (NodeDescription node : nodes)
            {
                logger.info("Found node " + node.getId());
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_06_update_execution_node()
    {
        logger.info("TC_06_update_execution_node");
        try
        {
            List<NodeDescription> nodes  = persistenceManager.getNodes();
            if(nodes.size() > 0)
            {
                NodeDescription firstNode = nodes.get(0);
                firstNode.getFlavor().setDisk(9);
                firstNode = persistenceManager.updateExecutionNode(firstNode);
                Assert.assertEquals(9, firstNode.getFlavor().getDisk());
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_07_check_execution_node_existence_by_host_name()
    {
        logger.info("TC_07_check_execution_node_existence_by_host_name");
        try
        {
            List<NodeDescription> nodes  = persistenceManager.getNodes();
            if(nodes.size() > 0)
            {
                NodeDescription firstNode = nodes.get(0);
                String hostName = firstNode.getId();
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
    public void TC_08_retrieve_execution_node_by_host_name()
    {
        logger.info("TC_08_retrieve_execution_node_by_host_name");
        try
        {
            List<NodeDescription> nodes  = persistenceManager.getNodes();
            if(nodes.size() > 0)
            {
                NodeDescription firstNode = nodes.get(0);
                String hostName = firstNode.getId();
                NodeDescription searchedNode  = persistenceManager.getNodeByHostName(hostName);
                Assert.assertTrue(searchedNode != null && searchedNode.getId().equals(hostName));
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_09_delete_execution_node()
    {
        logger.info("TC_09_delete_execution_node");
        try
        {
            // add a new execution node in order to deactivate it
            NodeDescription node  = new NodeDescription();
            node.setId("hostname2");
            node.setUserName("Test user name");
            node.setUserPass("Test user pass");
            NodeFlavor flavor = new NodeFlavor() {{
                setCpu(2);
                setMemory(1024);
                setDisk(1000);
            }};
            node.setFlavor(flavor);

            node.setDescription("Node 2 just for test");

            node = persistenceManager.saveExecutionNode(node);
            // check persisted node
            Assert.assertTrue(node != null && node.getId() != null);

            // deactivate node
            node = persistenceManager.deleteExecutionNode(node.getId());

            Assert.assertTrue(node != null && !node.getActive());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_10_save_new_container()
    {
        logger.info("TC_10_save_new_container");
        try
        {
            // add a new container for test
            Container container = createNewTestContainer("container01");

            container = persistenceManager.saveContainer(container);
            // check persisted container
            Assert.assertTrue(container != null && container.getId() != null);
            Assert.assertTrue(container.getApplications() != null && container.getApplications().size() == 3);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    private Container createNewTestContainer(String containerId) {
        // add a new container for test
        Container container = new Container();
        container.setId(containerId);
        container.setName("container for test");
        container.setTag("container tag");

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

        final Application app3 = new Application();
        app3.setName("App3");
        applications.add(app3);

        container.setApplications(applications);
        return container;
    }

    @Test
    public void TC_11_retrieve_containers()
    {
        logger.info("TC_11_retrieve_containers");
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
    public void TC_12_delete_container_with_applications_no_PC()
    {
        logger.info("TC_12_delete_container_with_applications_no_PC");
        try
        {
            // add a new container for test
            Container container = createNewTestContainer("container02");

            container = persistenceManager.saveContainer(container);
            // check persisted container
            Assert.assertTrue(container != null && container.getId() != null);

            // delete container
            persistenceManager.deleteContainer(container.getId());

            // check that the container was deleted
            Assert.assertTrue(!persistenceManager.existsContainer("container02"));
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_13_save_new_processing_component()
    {
        logger.info("TC_13_save_new_processing_component");
        try
        {
            // add a new processing component for test
            ProcessingComponent component = createNewProcessingComponent("component01", "container01");

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

    private ProcessingComponent createNewProcessingComponent(String componentId, String containerId) {
        // add a new processing component for test
        ProcessingComponent component = new ProcessingComponent();
        component.setId(componentId);
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
        //component.setMultiThread(true);
        component.setActive(true); // TODO

        component.setContainerId(containerId);
        /*component.setSourceCardinality(0);
        component.setTargetCardinality(0);*/

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

        // add also a template parameter, which regular parameter(s) inside
        final TemplateParameterDescriptor tParam = new TemplateParameterDescriptor();
        List<ParameterDescriptor> templateParamParameters = new ArrayList<>();
        tParam.setId("templateParam");
        tParam.setType(ParameterType.TEMPLATE);
        tParam.setDataType(Integer.class);
        tParam.setLabel("Test TemplateParam");

        final ParameterDescriptor param3 = new ParameterDescriptor();
        param3.setId("testParam3");
        param3.setType(ParameterType.REGULAR);
        param3.setDataType(Integer.class);
        param3.setLabel("Test Param 3");
        templateParamParameters.add(param3);

        final ParameterDescriptor param4 = new ParameterDescriptor();
        param4.setId("testParam4");
        param4.setType(ParameterType.REGULAR);
        param4.setDataType(Integer.class);
        param4.setLabel("Test Param 4");
        templateParamParameters.add(param4);

        tParam.setParameters(templateParamParameters);
        parameters.add(tParam);

        component.setParameterDescriptors(parameters);
        component.setTemplateContents("");
        return component;
    }

    @Test
    public void TC_14_retrieve_processing_components()
    {
        logger.info("TC_14_retrieve_processing_components");
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
    public void TC_15_update_processing_component()
    {
        logger.info("TC_15_update_processing_component");
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
    public void TC_16_check_processing_component_existence_by_id()
    {
        logger.info("TC_16_check_processing_component_existence_by_id");
        try
        {
            List<ProcessingComponent> components  = persistenceManager.getProcessingComponents();;

            if(components.size() > 0)
            {
                ProcessingComponent firstComponent = components.get(0);
                String identifier = firstComponent.getId();
                Assert.assertTrue(persistenceManager.existsProcessingComponent(identifier));
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_17_retrieve_processing_component_by_id()
    {
        logger.info("TC_17_retrieve_processing_component_by_id");
//        try
//        {
            List<ProcessingComponent> components  = persistenceManager.getProcessingComponents();;
            if(components.size() > 0)
            {
                ProcessingComponent firstComponent = components.get(0);
                String identifier = firstComponent.getId();
                ProcessingComponent searchedComponent  = persistenceManager.getProcessingComponentById(identifier);
                Assert.assertTrue(searchedComponent != null && searchedComponent.getId().equals(identifier));
            }
//        }
//        catch (PersistenceException e)
//        {
//            logger.error(ExceptionUtils.getStackTrace(e));
//            Assert.fail(e.getMessage());
//        }
    }

    @Test
    public void TC_18_delete_processing_component()
    {
        logger.info("TC_18_delete_processing_component");
        try
        {
            // add a new processing component for test
            ProcessingComponent component = createNewProcessingComponent("component02_to_delete", "container01");

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
    public void TC_19_delete_container_with_applications_with_PC()
    {
        logger.info("TC_19_delete_container_with_applications_with_PC");
        try
        {
            // add a new container for test
            Container container = createNewTestContainer("container03");
            container = persistenceManager.saveContainer(container);
            // check persisted container
            Assert.assertTrue(container != null && container.getId() != null);

            // add a new processing component for test
            ProcessingComponent component = createNewProcessingComponent("PC_container_to_delete", "container03");
            component = persistenceManager.saveProcessingComponent(component);
            // check persisted component
            Assert.assertTrue(component != null && component.getId() != null);

            // delete container
            persistenceManager.deleteContainer(container.getId());
            // check that the container was deleted
            Assert.assertTrue(!persistenceManager.existsContainer("container03"));

        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    private DataSourceComponent createNewDataSourceComponent(String componentId) {
        // add a new data source component for test
        final String sensorName = "Optical sensor";
        final String dataSourceName = "AWS";

        DataSourceComponent component = new DataSourceComponent(sensorName, dataSourceName);
        component.setId(componentId);
        component.setLabel("component label");
        component.setVersion("component version");
        component.setDescription("component description");
        component.setAuthors("component authors");
        component.setCopyright("component copyright");

        component.setNodeAffinity("Any");
        component.setTargetCardinality(0);
        component.setFetchMode(FetchMode.RESUME);

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setFormatType(DataFormat.RASTER);
        dataDescriptor.setGeometry("POLYGON ((24.16023 -9.60737, 24.15266 -7.36319, 22.05055 -7.38847, 22.05739 -9.59798, 24.16023 -9.60737))");
        dataDescriptor.setLocation("https://landsat-pds.s3.amazonaws.com/c1/L8/201/044/LC08_L1TP_201044_20170930_20171013_01_T1");
        dataDescriptor.setSensorType(SensorType.OPTICAL);
        dataDescriptor.setDimension(new Dimension(100, 200));

        // A data source component should have only one target descriptor, and it's already added on constructor
        /*TargetDescriptor targetDescriptor1 = new TargetDescriptor("targetDescriptor10");
        targetDescriptor1.setParentId("component01");
        targetDescriptor1.setDataDescriptor(dataDescriptor);

        TargetDescriptor targetDescriptor2 = new TargetDescriptor("targetDescriptor20");
        targetDescriptor2.setParentId("component01");
        targetDescriptor2.setDataDescriptor(dataDescriptor);

        component.addTarget(targetDescriptor1);
        component.addTarget(targetDescriptor2);*/

        return component;
    }

    @Test
    public void TC_20_save_new_data_source_component()
    {
        logger.info("TC_20_save_new_data_source_component");
        try
        {
            // add a new data source component for test
            DataSourceComponent component = createNewDataSourceComponent("datasourcecomponent01");

            component = persistenceManager.saveDataSourceComponent(component);
            // check persisted component
            Assert.assertTrue(component != null && component.getId() != null);
            // check targets
            Assert.assertTrue(component.getTargets() != null && component.getTargets().size() > 0);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void TC_23_save_new_data_products()
    {
        logger.info("TC_23_save_new_data_products");
        try {
            EOProduct product = new EOProduct();
            product.setId("LC82010442017273LGN00");
            product.setName("LC08_L1TP_201044_20170930_20171013_01_T1");
            product.setAcquisitionDate(Date.from(LocalDateTime.of(2017, 9, 30, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));
            product.setSensorType(SensorType.OPTICAL);
            product.setProductType("Landsat8");
            product.setApproximateSize(1700000000);
            product.setPixelType(PixelType.UINT16);
            product.setWidth(7601);
            product.setHeight(7761);
            product.setFormatType(DataFormat.RASTER);
            product.setGeometry("POLYGON((24.16023 -9.60737, 24.15266 -7.36319, 22.05055 -7.38847, 22.05739 -9.59798, 24.16023 -9.60737))");
            product.setLocation("https://landsat-pds.s3.amazonaws.com/c1/L8/201/044/LC08_L1TP_201044_20170930_20171013_01_T1");
            product.setVisibility(Visibility.PRIVATE);

            persistenceManager.saveEOProduct(product);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_24_retrieve_raster_data_products()
    {
        logger.info("TC_24_retrieve_raster_data_products");
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
    public void TC_25_save_new_vector_data_product()
    {
        logger.info("TC_25_save_new_vector_data_product");
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
    public void TC_26_retrieve_vector_data_products()
    {
        logger.info("TC_26_retrieve_vector_data_products");
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
    public void TC_27_save_new_notification()
    {
        logger.info("TC_27_save_new_notification");
        try
        {
            Message message =  Message.create(SystemPrincipal.instance().getName(),
                                              "notification source",
                                              "first item");
            message.setRead(true);
            message.addItem("Auxiliary", "second item");
            message.setTopic(SystemPrincipal.instance().getName());

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

    /*@Test
    public void TC_28_save_new_workflow_add_nodes_individually()
    {
        logger.info("TC_28_save_new_workflow_add_nodes_individually");
        try
        {
            // add a new workflow for test
            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_1");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            workflow.setUserName("admin");
            //workflow.setCreated(LocalDateTime.now());

            // save the parent workflow entity
            workflow = persistenceManager.saveWorkflowDescriptor(workflow);
            Assert.assertTrue(workflow != null && workflow.getId() != null);
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            // save nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            //node1.setCreated(LocalDateTime.now());
            node1 = persistenceManager.saveWorkflowNodeDescriptor(node1, workflow);
            Assert.assertTrue(node1 != null && node1.getId() != null);
            logger.info("Workflow node " + node1.getName() + " saved, ID = " + node1.getId().toString());

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            //node2.setCreated(LocalDateTime.now());
            node2 = persistenceManager.saveWorkflowNodeDescriptor(node2, workflow);
            Assert.assertTrue(node2 != null && node2.getId() != null);
            logger.info("Workflow node " + node2.getName() + " saved, ID = " + node2.getId().toString());

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            //node3.setCreated(LocalDateTime.now());
            node3 = persistenceManager.saveWorkflowNodeDescriptor(node3, workflow);
            Assert.assertTrue(node3 != null && node3.getId() != null);
            logger.info("Workflow node " + node3.getName() + " saved, ID = " + node3.getId().toString());

            logger.info("After saving each node separate : Workflow has " + workflow.getNodes().size() + " nodes");

            // check persisted workflow nodes
            Assert.assertTrue(workflow.getNodes() != null && workflow.getNodes().size() == 3);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }*/

    @Test
    public void TC_29_save_new_workflow_together_with_nodes_cascade()
    {
        logger.info("TC_29_save_new_workflow_together_with_nodes_cascade");
        try
        {
            // add a new workflow for test
            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_2");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            workflow.setUserName("admin");
            //workflow.setCreated(LocalDateTime.now());

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            //node1.setCreated(LocalDateTime.now());

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            node2.setComponentType(ComponentType.PROCESSING);
            //node2.setCreated(LocalDateTime.now());

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            node3.setComponentType(ComponentType.PROCESSING);
            //node3.setCreated(LocalDateTime.now());

            workflow.addNode(node1);
            workflow.addNode(node2);
            workflow.addNode(node3);

            // save the parent workflow entity
            workflow = persistenceManager.saveWorkflowDescriptor(workflow);
            Assert.assertTrue(workflow != null && workflow.getId() != null);
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            logger.info("After saving nodes cascade : Workflow has " + workflow.getNodes().size() + " nodes");

            // check persisted workflow
            Assert.assertTrue(workflow != null && workflow.getNodes() != null && workflow.getNodes().size() == 3);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_30_save_new_workflow_with_node_custom_values()
    {
        logger.info("TC_30_save_new_workflow_with_node_custom_values");
        try
        {
            // add a new workflow for test
            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_3");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            workflow.setUserName("admin");
            //workflow.setCreated(LocalDateTime.now());

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            //node1.setCreated(LocalDateTime.now());

            node1.addCustomValue("customName1", "customValue1");
            node1.addCustomValue("customName2", "customValue2");
            node1.addCustomValue("customName3", "customValue3");

            workflow.addNode(node1);

            // save the parent workflow entity
            workflow = persistenceManager.saveWorkflowDescriptor(workflow);
            Assert.assertTrue(workflow != null && workflow.getId() != null);
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            // check persisted workflow
            Assert.assertTrue(workflow != null && workflow.getNodes() != null && workflow.getNodes().size() == 1);

            // check persisted node custom values
            Assert.assertTrue(workflow.getNodes().get(0).getCustomValues().size() == 3);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void TC_31_delete_workflow_node()
    {
        logger.info("TC_31_delete_workflow_node");
        try
        {
            // add a new workflow for test
            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_4");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            workflow.setUserName("admin");
            //workflow.setCreated(LocalDateTime.now());

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            //node1.setCreated(LocalDateTime.now());

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            node2.setComponentType(ComponentType.PROCESSING);
            //node2.setCreated(LocalDateTime.now());

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            node3.setComponentType(ComponentType.PROCESSING);
            //node3.setCreated(LocalDateTime.now());

            workflow.addNode(node1);
            workflow.addNode(node2);
            workflow.addNode(node3);

            // save the parent workflow entity
            workflow = persistenceManager.saveWorkflowDescriptor(workflow);
            Assert.assertTrue(workflow != null && workflow.getId() != null);
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            logger.info("After saving nodes cascade : Workflow has " + workflow.getNodes().size() + " nodes");
            // check persisted workflow
            Assert.assertTrue(workflow != null && workflow.getNodes() != null && workflow.getNodes().size() == 3);

            // remove a node
            workflow.removeNode(node2);
            workflow = persistenceManager.updateWorkflowDescriptor(workflow);

            logger.info("After removing a node : Workflow has " + workflow.getNodes().size() + " nodes");
            // check persisted workflow
            Assert.assertTrue(workflow != null && workflow.getNodes() != null && workflow.getNodes().size() == 2);

        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_32_delete_workflow()
    {
        logger.info("TC_32_delete_workflow");
        try
        {
            // add a new workflow for test
            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_5");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            workflow.setUserName("admin");
            //workflow.setCreated(LocalDateTime.now());

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            //node1.setCreated(LocalDateTime.now());

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            node2.setComponentType(ComponentType.PROCESSING);
            //node2.setCreated(LocalDateTime.now());

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            node3.setComponentType(ComponentType.PROCESSING);
            //node3.setCreated(LocalDateTime.now());

            WorkflowNodeDescriptor node4 = new WorkflowNodeDescriptor();
            node4.setName("node4");
            node4.setComponentId("component01");
            node4.setComponentType(ComponentType.PROCESSING);
            //node4.setCreated(LocalDateTime.now());

            workflow.addNode(node1);
            workflow.addNode(node2);
            workflow.addNode(node3);
            workflow.addNode(node4);

            // save the parent workflow entity
            workflow = persistenceManager.saveWorkflowDescriptor(workflow);
            Assert.assertTrue(workflow != null && workflow.getId() != null);
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            logger.info("After saving nodes cascade : Workflow has " + workflow.getNodes().size() + " nodes");
            // check persisted workflow
            Assert.assertTrue(workflow != null && workflow.getNodes() != null && workflow.getNodes().size() == 4);

            // delete workflow
            workflow = persistenceManager.deleteWorkflowDescriptor(workflow.getId());
            Assert.assertTrue(workflow != null && workflow.getNodes() != null && workflow.getNodes().size() == 4 && !workflow.isActive());
            logger.info("Successfully deactivate workflow ID " + workflow.getId());

        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_33_save_new_workflow_with_node_custom_values_and_incoming_links()
    {
        logger.info("TC_33_save_new_workflow_with_node_custom_values_and_incoming_links");
        try
        {
            // add a new workflow for test
            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_6");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            workflow.setUserName("admin");
            //workflow.setCreated(LocalDateTime.now());

            // add nodes within this workflow
            WorkflowNodeDescriptor sourceNode = new WorkflowNodeDescriptor();
            sourceNode.setName("sourcenode1");
            sourceNode.setComponentId("component01");
            sourceNode.setComponentType(ComponentType.PROCESSING);

            // add processing custom values for the node
            sourceNode.addCustomValue("customName1", "customValue1");
            sourceNode.addCustomValue("customName2", "customValue2");
            sourceNode.addCustomValue("customName3", "customValue3");

            WorkflowNodeDescriptor targetNode = new WorkflowNodeDescriptor();
            targetNode.setName("targetnode1");
            targetNode.setComponentId("component01");
            targetNode.setComponentType(ComponentType.PROCESSING);

            // add incoming links for the node
            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setFormatType(DataFormat.RASTER);
            dataDescriptor.setGeometry("POLYGON ((24.16023 -9.60737, 24.15266 -7.36319, 22.05055 -7.38847, 22.05739 -9.59798, 24.16023 -9.60737))");
            dataDescriptor.setLocation("https://landsat-pds.s3.amazonaws.com/c1/L8/201/044/LC08_L1TP_201044_20170930_20171013_01_T1");
            dataDescriptor.setSensorType(SensorType.OPTICAL);
            dataDescriptor.setDimension(new Dimension(100, 200));

            TargetDescriptor linkInput = new TargetDescriptor("linkInput01");
            linkInput.setParentId("component01");
            linkInput.setDataDescriptor(dataDescriptor);

            SourceDescriptor linkOutput = new SourceDescriptor("linkOutput01");
            linkOutput.setParentId("component01");
            linkOutput.setDataDescriptor(dataDescriptor);

            // add the nodes within the workflow
            workflow.addNode(sourceNode);
            workflow.addNode(targetNode);

            // save the parent workflow entity
            workflow = persistenceManager.saveWorkflowDescriptor(workflow);
            Assert.assertTrue(workflow != null && workflow.getId() != null);
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            // check persisted workflow
            Assert.assertTrue(workflow.getNodes() != null && workflow.getNodes().size() == 2);

            ComponentLink componentLink1 = new ComponentLink(sourceNode.getId(), linkInput, linkOutput);
            Set<ComponentLink> links = new HashSet<>();
            links.add(componentLink1);
            targetNode.setIncomingLinks(links);

            persistenceManager.updateWorkflowNodeDescriptor(targetNode);

            // check persisted node custom values
            final List<ParameterValue> customValues = workflow.getNodes().get(0).getCustomValues();
            Assert.assertTrue(customValues.size() == 3);
            logger.info("1st node custom values: ");
            for (ParameterValue pValue: customValues) {
                logger.info("\t\t\t" + pValue.getParameterName() + "=" + pValue.getParameterValue());
            }
            logger.info("1st node custom values (with forEach): ");
            if (customValues != null) {
                customValues.forEach(v -> logger.info("\t\t\t\t" + v.getParameterName() + "=" + v.getParameterValue()));
            }

            // check persisted incoming links
            Assert.assertTrue(workflow.getNodes().get(0).getIncomingLinks() == null || workflow.getNodes().get(0).getIncomingLinks().size() == 0);
            Assert.assertTrue(workflow.getNodes().get(1).getIncomingLinks() != null && workflow.getNodes().get(1).getIncomingLinks().size() == 1);
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_34_save_new_workflow_with_node_groups()
    {
        logger.info("TC_34_save_new_workflow_with_node_groups");
        try
        {
            // add a new workflow for test
            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_7");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            workflow.setUserName("admin");
            //workflow.setCreated(LocalDateTime.now());

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            //node1.setCreated(LocalDateTime.now());

            // add processing custom values for the node
            node1.addCustomValue("customName1", "customValue1");
            node1.addCustomValue("customName2", "customValue2");
            node1.addCustomValue("customName3", "customValue3");

            // add incoming links for the node
            DataDescriptor dataDescriptor = new DataDescriptor();
            dataDescriptor.setFormatType(DataFormat.RASTER);
            dataDescriptor.setGeometry("POLYGON ((24.16023 -9.60737, 24.15266 -7.36319, 22.05055 -7.38847, 22.05739 -9.59798, 24.16023 -9.60737))");
            dataDescriptor.setLocation("https://landsat-pds.s3.amazonaws.com/c1/L8/201/044/LC08_L1TP_201044_20170930_20171013_01_T1");
            dataDescriptor.setSensorType(SensorType.OPTICAL);
            dataDescriptor.setDimension(new Dimension(100, 200));

            TargetDescriptor targetDescriptor = new TargetDescriptor("targetDescriptor02");
            targetDescriptor.setParentId("component01");
            targetDescriptor.setDataDescriptor(dataDescriptor);
            List<String> targetConstraints = new ArrayList<>();
            // TODO put correct constraints
            targetConstraints.add("target_constraint01");
            targetConstraints.add("target_constraint02");
            targetConstraints.add("target_constraint03");
            //targetDescriptor.setConstraints(targetConstraints);

            SourceDescriptor sourceDescriptor = new SourceDescriptor("sourceDescriptor02");
            sourceDescriptor.setParentId("component01");
            sourceDescriptor.setDataDescriptor(dataDescriptor);
            List<String> sourceConstraints = new ArrayList<>();
            // TODO put correct constraints
            sourceConstraints.add("source_constraint01");
            sourceConstraints.add("source_constraint02");
            sourceConstraints.add("source_constraint03");
            //sourceDescriptor.setConstraints(sourceConstraints);

            // add the node within the workflow
            workflow.addNode(node1);


            // add a group node
            WorkflowNodeGroupDescriptor nodeGroup = new WorkflowNodeGroupDescriptor();
            nodeGroup.setName("groupNode01");
            nodeGroup.setComponentId("component01");
            nodeGroup.setComponentType(ComponentType.GROUP);
            //nodeGroup.setCreated(LocalDateTime.now());

            nodeGroup.addNode(node1);

            // add the group node
            workflow.addNode(nodeGroup);

            // save the parent workflow entity
            workflow = persistenceManager.saveWorkflowDescriptor(workflow);

            Assert.assertTrue(workflow != null && workflow.getId() != null);
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            // check persisted workflow
            Assert.assertTrue(workflow.getNodes() != null && workflow.getNodes().size() == 2);

            /* TODO: Add a second node if testing links
            ComponentLink componentLink1 = new ComponentLink(node1.getId(), targetDescriptor, sourceDescriptor);
            List<ComponentLink> links = new ArrayList<>();
            links.add(componentLink1);
            node1.setIncomingLinks(links);

            persistenceManager.updateWorkflowNodeDescriptor(node1);*/

            // check persisted node custom values
            Assert.assertEquals(3, workflow.getNodes().get(0).getCustomValues().size());
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_35_save_new_query()
    {
        logger.info("TC_35_save_new_query");
        try
        {
            Query query = new Query();
            query.setSensor("Optical sensor");
            query.setDataSource("AWS");
            query.setUserId("admin");
            query.setWorkflowNodeId(1L);

            query = persistenceManager.saveQuery(query);
            // check persisted query
            Assert.assertNotNull(query);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_36_save_new_execution_job()
    {
        logger.info("TC_36_save_new_execution_job");
        try
        {
            // add a new job for test
            ExecutionJob job = new ExecutionJob();
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            job.setWorkflowId(1L);
            job.setUserName("admin");

            job = persistenceManager.saveExecutionJob(job);
            // check persisted job
            Assert.assertTrue(job != null && job.getId() != 0);
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_37_save_new_processing_execution_task()
    {
        logger.info("TC_37_save_new_execution_task");
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
                ProcessingExecutionTask task = new ProcessingExecutionTask();
                task.setResourceId("ProcessingExecutionTask-resourceId01");
                task.setExecutionStatus(ExecutionStatus.RUNNING);
                task.setExecutionNodeHostName(node.getId());
                task.setComponent(component);
                task.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                List<Variable> inputsValues = new ArrayList<>();
                Variable input1 = new Variable();
                input1.setKey("input1");
                input1.setValue("val1");
                inputsValues.add(input1);

                Variable input2 = new Variable();
                input2.setKey("input2");
                input2.setValue("val2");
                inputsValues.add(input2);

                task.setInputParameterValues(inputsValues);

                task = (ProcessingExecutionTask) persistenceManager.saveExecutionTask(task, job);
                // check persisted task
                Assert.assertTrue(task != null && task.getId() != 0);
                // check if job correctly updated
                Assert.assertTrue(job.getTasks().contains(task));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_38_save_new_data_source_query_execution_task()
    {
        logger.info("TC_38_save_new_data_source_query_execution_task");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.getAllJobs();
            // retrieve data sources components
            List<DataSourceComponent> components = persistenceManager.getDataSourceComponents();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.getNodes();

            if(jobs != null && jobs.size() > 0 &&
              components != null && components.size() > 0 &&
              nodes != null && nodes.size() > 0)
            {
                // retrieve first job
                ExecutionJob job = jobs.get(0);
                // retrieve first component
                DataSourceComponent component = components.get(0);

                NodeDescription node = nodes.get(0);

                // add a new task for test
                DataSourceExecutionTask task = new DataSourceExecutionTask();
                task.setResourceId("DataSourceExecutionTask-resourceId01");
                task.setExecutionStatus(ExecutionStatus.RUNNING);
                task.setExecutionNodeHostName(node.getId());
                task.setComponent(component);
                task.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                List<Variable> inputsValues = new ArrayList<>();
                Variable input1 = new Variable();
                input1.setKey("input1");
                input1.setValue("val1");
                inputsValues.add(input1);

                Variable input2 = new Variable();
                input2.setKey("input2");
                input2.setValue("val2");
                inputsValues.add(input2);

                task.setInputParameterValues(inputsValues);

                task = (DataSourceExecutionTask) persistenceManager.saveExecutionTask(task, job);
                // check persisted task
                Assert.assertTrue(task != null && task.getId() != 0);
                // check if job correctly updated
                Assert.assertTrue(job.getTasks().contains(task));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_39_save_new_group_execution_task()
    {
        logger.info("TC_39_save_new_group_execution_task");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.getAllJobs();
            // retrieve data sources components
            List<DataSourceComponent> dataSourceComponents = persistenceManager.getDataSourceComponents();
            // retrieve also processing components
            List<ProcessingComponent> processingComponents = persistenceManager.getProcessingComponents();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.getNodes();

            if(jobs != null && jobs.size() > 0 &&
              dataSourceComponents != null && dataSourceComponents.size() > 0 &&
              processingComponents != null && processingComponents.size() > 0 &&
              nodes != null && nodes.size() > 0)
            {
                // retrieve first job
                ExecutionJob job = jobs.get(0);
                // retrieve first component
                DataSourceComponent dataSourceComponent = dataSourceComponents.get(0);
                ProcessingComponent processingComponent = processingComponents.get(0);

                NodeDescription node = nodes.get(0);

                // add a new processing task for test
                ProcessingExecutionTask processingTask = new ProcessingExecutionTask();
                processingTask.setResourceId("ProcessingExecutionTask-resourceId02");
                processingTask.setExecutionStatus(ExecutionStatus.RUNNING);
                processingTask.setExecutionNodeHostName(node.getId());
                processingTask.setComponent(processingComponent);
                processingTask.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                List<Variable> inputsValues = new ArrayList<>();
                Variable input1 = new Variable();
                input1.setKey("input1");
                input1.setValue("val1");
                inputsValues.add(input1);

                Variable input2 = new Variable();
                input2.setKey("input2");
                input2.setValue("val2");
                inputsValues.add(input2);

                processingTask.setInputParameterValues(inputsValues);


                // add a new data source task for test
                DataSourceExecutionTask dataSourceTask = new DataSourceExecutionTask();
                dataSourceTask.setResourceId("DataSourceExecutionTask-resourceId02");
                dataSourceTask.setExecutionStatus(ExecutionStatus.RUNNING);
                dataSourceTask.setExecutionNodeHostName(node.getId());
                dataSourceTask.setComponent(dataSourceComponent);
                dataSourceTask.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                dataSourceTask.setInputParameterValues(inputsValues);


                // make a ExecutionGroup from the 2 tasks
                ExecutionGroup taskGroup = new ExecutionGroup();
                taskGroup.setResourceId("ExecutionGroup-resourceId01");
                taskGroup.setExecutionStatus(ExecutionStatus.RUNNING);
                taskGroup.setExecutionNodeHostName(node.getId());
                taskGroup.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                // save first the execution group and after the sub-tasks within
                ExecutionGroup taskGroupSaved = (ExecutionGroup)persistenceManager.saveExecutionTask(taskGroup, job);
                // check persisted task group
                Assert.assertTrue(taskGroupSaved != null && taskGroupSaved.getId() != 0);
                // check if job correctly updated
                Assert.assertTrue(job.getTasks().contains(taskGroupSaved));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

                processingTask.setGroupTask(taskGroupSaved);
                processingTask = (ProcessingExecutionTask)persistenceManager.saveExecutionTask(processingTask, job);

                dataSourceTask.setGroupTask(taskGroupSaved);
                dataSourceTask = (DataSourceExecutionTask)persistenceManager.saveExecutionTask(dataSourceTask, job);

                // add tasks to saved group
                taskGroupSaved.addTask(processingTask);
                taskGroupSaved.addTask(dataSourceTask);

                persistenceManager.updateExecutionTask(taskGroupSaved);

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_39_save_new_group_execution_task_with_subtasks_unattached_to_job()
    {
        logger.info("TC_39_save_new_group_execution_task_with_subtasks_unattached_to_job");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.getAllJobs();
            // retrieve data sources components
            List<DataSourceComponent> dataSourceComponents = persistenceManager.getDataSourceComponents();
            // retrieve also processing components
            List<ProcessingComponent> processingComponents = persistenceManager.getProcessingComponents();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.getNodes();

            if(jobs != null && jobs.size() > 0 &&
              dataSourceComponents != null && dataSourceComponents.size() > 0 &&
              processingComponents != null && processingComponents.size() > 0 &&
              nodes != null && nodes.size() > 0)
            {
                // retrieve first job
                ExecutionJob job = jobs.get(0);
                // retrieve first component
                DataSourceComponent dataSourceComponent = dataSourceComponents.get(0);
                ProcessingComponent processingComponent = processingComponents.get(0);

                NodeDescription node = nodes.get(0);

                // add a new processing task for test
                ProcessingExecutionTask processingTask = new ProcessingExecutionTask();
                processingTask.setResourceId("ProcessingExecutionTask-resourceId03");
                processingTask.setExecutionStatus(ExecutionStatus.RUNNING);
                processingTask.setExecutionNodeHostName(node.getId());
                processingTask.setComponent(processingComponent);
                processingTask.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                List<Variable> inputsValues = new ArrayList<>();
                Variable input1 = new Variable();
                input1.setKey("input1");
                input1.setValue("val1");
                inputsValues.add(input1);

                Variable input2 = new Variable();
                input2.setKey("input2");
                input2.setValue("val2");
                inputsValues.add(input2);

                processingTask.setInputParameterValues(inputsValues);


                // add a new data source task for test
                DataSourceExecutionTask dataSourceTask = new DataSourceExecutionTask();
                dataSourceTask.setResourceId("DataSourceExecutionTask-resourceId03");
                dataSourceTask.setExecutionStatus(ExecutionStatus.RUNNING);
                dataSourceTask.setExecutionNodeHostName(node.getId());
                dataSourceTask.setComponent(dataSourceComponent);
                dataSourceTask.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                dataSourceTask.setInputParameterValues(inputsValues);


                // make a ExecutionGroup from the 2 tasks
                ExecutionGroup taskGroup = new ExecutionGroup();
                taskGroup.setResourceId("ExecutionGroup-resourceId02");
                taskGroup.setExecutionStatus(ExecutionStatus.RUNNING);
                taskGroup.setExecutionNodeHostName(node.getId());
                taskGroup.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                // save first the execution group and after the sub-tasks within
                ExecutionGroup taskGroupSaved = (ExecutionGroup)persistenceManager.saveExecutionTask(taskGroup, job);
                // check persisted task group
                Assert.assertTrue(taskGroupSaved != null && taskGroupSaved.getId() != 0);
                // check if job correctly updated
                Assert.assertTrue(job.getTasks().contains(taskGroupSaved));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

                processingTask.setGroupTask(taskGroupSaved);
                processingTask = (ProcessingExecutionTask)persistenceManager.saveExecutionGroupSubTask(processingTask, taskGroupSaved);

                dataSourceTask.setGroupTask(taskGroupSaved);
                dataSourceTask = (DataSourceExecutionTask)persistenceManager.saveExecutionGroupSubTask(dataSourceTask, taskGroupSaved);

                // add tasks to saved group
                taskGroupSaved.addTask(processingTask);
                taskGroupSaved.addTask(dataSourceTask);

                persistenceManager.updateExecutionTask(taskGroupSaved);

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_39_save_new_group_execution_task_with_subtasks_unattached_to_job_all_once()
    {
        logger.info("TC_39_save_new_group_execution_task_with_subtasks_unattached_to_job_all_once");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.getAllJobs();
            // retrieve data sources components
            List<DataSourceComponent> dataSourceComponents = persistenceManager.getDataSourceComponents();
            // retrieve also processing components
            List<ProcessingComponent> processingComponents = persistenceManager.getProcessingComponents();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.getNodes();

            if(jobs != null && jobs.size() > 0 &&
              dataSourceComponents != null && dataSourceComponents.size() > 0 &&
              processingComponents != null && processingComponents.size() > 0 &&
              nodes != null && nodes.size() > 0)
            {
                // retrieve first job
                ExecutionJob job = jobs.get(0);
                // retrieve first component
                DataSourceComponent dataSourceComponent = dataSourceComponents.get(0);
                ProcessingComponent processingComponent = processingComponents.get(0);

                NodeDescription node = nodes.get(0);

                // add a new processing task for test
                ProcessingExecutionTask processingTask = new ProcessingExecutionTask();
                processingTask.setResourceId("ProcessingExecutionTask-resourceId04");
                processingTask.setExecutionStatus(ExecutionStatus.RUNNING);
                processingTask.setExecutionNodeHostName(node.getId());
                processingTask.setComponent(processingComponent);
                processingTask.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                List<Variable> inputsValues = new ArrayList<>();
                Variable input1 = new Variable();
                input1.setKey("input1");
                input1.setValue("val1");
                inputsValues.add(input1);

                Variable input2 = new Variable();
                input2.setKey("input2");
                input2.setValue("val2");
                inputsValues.add(input2);

                processingTask.setInputParameterValues(inputsValues);


                // add a new data source task for test
                DataSourceExecutionTask dataSourceTask = new DataSourceExecutionTask();
                dataSourceTask.setResourceId("DataSourceExecutionTask-resourceId04");
                dataSourceTask.setExecutionStatus(ExecutionStatus.RUNNING);
                dataSourceTask.setExecutionNodeHostName(node.getId());
                dataSourceTask.setComponent(dataSourceComponent);
                dataSourceTask.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                dataSourceTask.setInputParameterValues(inputsValues);


                // make a ExecutionGroup from the 2 tasks
                ExecutionGroup taskGroup = new ExecutionGroup();
                taskGroup.setResourceId("ExecutionGroup-resourceId03");
                taskGroup.setExecutionStatus(ExecutionStatus.RUNNING);
                taskGroup.setExecutionNodeHostName(node.getId());
                taskGroup.setWorkflowNodeId(persistenceManager.getWorkflowDescriptor(job.getWorkflowId()).getNodes().get(0).getId());

                // add tasks to tasks group
                taskGroup.addTask(processingTask);
                taskGroup.addTask(dataSourceTask);

                taskGroup = (ExecutionGroup)persistenceManager.saveExecutionGroupWithSubTasks(taskGroup, job);

                // check persisted task group
                Assert.assertTrue(taskGroup != null && taskGroup.getId() != 0);
                // check if job correctly updated
                Assert.assertTrue(job.getTasks().contains(taskGroup));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void TC_40_get_running_execution_tasks()
    {
        logger.info("TC_40_get_running_execution_tasks");
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


}
