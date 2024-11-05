package ro.cs.tao.persistence.test;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ro.cs.tao.component.*;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.ogc.WMSComponent;
import ro.cs.tao.component.ogc.WPSComponent;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.eodata.Attribute;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.VectorData;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.config.DatabaseConfiguration;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.topology.*;
import ro.cs.tao.user.User;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;
import ro.cs.tao.workflow.enums.Status;

import java.awt.*;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by oana on 7/18/2017.
 * Updated by Stefan Efrem on 10/30/2024
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:tao-persistence-context.xml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistenceManagerTest {

    private static final Log logger = LogFactory.getLog(PersistenceManagerTest.class);

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
            setupDB();
            assertNotNull(dbConfig.dataSource());
            assertNotNull(dbConfig.dataSource().getConnection());
            System.out.println("Connection established!");
        }
        catch (SQLException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_02_save_new_execution_node_new_flavor_new_services()
    {
        logger.info("TC_02_save_new_execution_node_new_flavor_new_services");
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setId("Test1_host_name");
            node.setUserName("Test1 user name");
            node.setUserPass("Test1 user pass");
            NodeFlavor flavor = new NodeFlavor("Test flavor", 2, 1024, 1000, 4, 1);
            flavor = persistenceManager.nodeFlavors().save(flavor);

            assertNotNull(flavor);
            assertNotNull(flavor.getId());
            //check persisted flavor
            node.setFlavor(flavor);

            node.setDescription("Node1 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.INSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.NOT_FOUND));
            node.setServicesStatus(servicesStatus);

            node = persistenceManager.nodes().save(node);
            // check persisted node
            assertNotNull(node);
            assertNotNull(node.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_03_save_new_execution_node_existing_flavor_existing_services()
    {
        logger.info("TC_03_save_new_execution_node_existing_flavor_existing_services");
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setId("Test2_host_name");
            node.setUserName("Test2 user name");
            node.setUserPass("Test2 user pass");
            NodeFlavor flavor = new NodeFlavor("Test flavor", 2, 1024, 1000, 4, 1);
            node.setFlavor(flavor);

            node.setDescription("Node2 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "1.9", "Docker description"), ServiceStatus.UNINSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.UNINSTALLED));
            node.setServicesStatus(servicesStatus);

            node = persistenceManager.nodes().save(node);
            // check persisted node
            assertNotNull(node);
            assertNotNull(node.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            assertTrue(e.getMessage().contains("There is already another node with the host name: "));
            System.out.println("TC03: The node already exists!");
            //fail(e.getMessage());
        }
    }

    @Test
    public void TC_04_save_new_execution_node_existing_flavor_mixed_services()
    {
        logger.info("TC_04_save_new_execution_node_existing_flavor_mixed_services");
        try
        {
            // add a new execution node for test
            NodeDescription node  = new NodeDescription();
            node.setId("Test3_host_name");
            node.setUserName("Test3 user name");
            node.setUserPass("Test3 user pass");
            NodeFlavor flavor = new NodeFlavor("Test flavor", 2, 1024, 1000, 4, 1);
            node.setFlavor(flavor);

            node.setDescription("Node3 just for test");

            List<NodeServiceStatus> servicesStatus = new ArrayList<>();
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Docker", "2.0", "Docker description"), ServiceStatus.INSTALLED));
            servicesStatus.add(new NodeServiceStatus(new ServiceDescription("Torque", "1.5", "Torque CRM"), ServiceStatus.INSTALLED));
            node.setServicesStatus(servicesStatus);

            node = persistenceManager.nodes().save(node);
            // check persisted node
            assertNotNull(node);
            assertNotNull(node.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            assertTrue(e.getMessage().contains("There is already another node with the host name: "));
            System.out.println("TC04: The node already exists!");
            //fail(e.getMessage());
        }
    }

    @Test
    public void TC_05_retrieve_all_execution_nodes()
    {
        logger.info("TC_05_retrieve_all_execution_nodes");
        try
        {
            List<NodeDescription> nodes  = persistenceManager.nodes().list();
            assertNotNull(nodes);
            assertFalse(nodes.isEmpty());
            for (NodeDescription node : nodes)
            {
                logger.info("Found node " + node.getId());
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_06_update_execution_node()
    {
        logger.info("TC_06_update_execution_node");
        try
        {
            List<NodeDescription> nodes  = persistenceManager.nodes().list();
            if(!nodes.isEmpty())
            {
                NodeDescription lastNode = nodes.getLast();
                lastNode.getFlavor().setDisk(9);
                lastNode = persistenceManager.nodes().update(lastNode);
                assertEquals(9, lastNode.getFlavor().getDisk());
            }
        }
        catch (TopologyException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_07_check_execution_node_existence_by_host_name()
    {
        logger.info("TC_07_check_execution_node_existence_by_host_name");
        List<NodeDescription> nodes  = persistenceManager.nodes().list();
        if(!nodes.isEmpty())
        {
            NodeDescription lastNode = nodes.getLast();
            String hostName = lastNode.getId();
            assertTrue(persistenceManager.nodes().exists(hostName));
        }
    }

    @Test
    public void TC_08_retrieve_execution_node_by_host_name()
    {
        logger.info("TC_08_retrieve_execution_node_by_host_name");
        List<NodeDescription> nodes  = persistenceManager.nodes().list();
        if(!nodes.isEmpty())
        {
            NodeDescription lastNode = nodes.getLast();
            String hostName = lastNode.getId();
            NodeDescription searchedNode  = persistenceManager.nodes().get(hostName);
            assertTrue(searchedNode != null && searchedNode.getId().equals(hostName));
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
            node.setId("Test4_host_name");
            node.setUserName("Test4 user name");
            node.setUserPass("Test4 user pass");
            NodeFlavor flavor = new NodeFlavor("Test flavor", 2, 1024, 1000, 4, 1);
            node.setFlavor(flavor);
            node.setDescription("Node4 just for test");

            node = persistenceManager.nodes().save(node);
            // check persisted node
            assertNotNull(node);
            assertNotNull(node.getId());

            // deactivate node
            persistenceManager.nodes().delete(node.getId());
            node = persistenceManager.nodes().get(node.getId());

            assertNotNull(node);
            assertFalse(node.getActive());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_10_delete_volatile_execution_node()
    {
        logger.info("TC_10_delete_execution_node");
        try
        {
            NodeDescription node  = new NodeDescription();
            node.setId("Test5_host_name");
            node.setUserName("Test5 user name");
            node.setUserPass("Test5 user pass");
            NodeFlavor flavor = new NodeFlavor("Test flavor", 2, 1024, 1000, 4, 1);
            node.setFlavor(flavor);
            node.setDescription("Node5 just for test");
            node.setVolatile(true);

            node = persistenceManager.nodes().save(node);
            // check persisted node
            assertNotNull(node);
            assertNotNull(node.getId());

            // delete node because it is volatile
            persistenceManager.nodes().delete(node.getId());
            assertNull(persistenceManager.nodes().get(node.getId()));
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_11_delete_server_execution_node()
    {
        logger.info("TC_11_delete_execution_node");
        try
        {
            NodeDescription node  = new NodeDescription();
            node.setId("Test6_host_name");
            node.setUserName("Test6 user name");
            node.setUserPass("Test6 user pass");
            NodeFlavor flavor = new NodeFlavor("Test flavor", 2, 1024, 1000, 4, 1);
            node.setFlavor(flavor);
            node.setDescription("Node6 just for test");
            node.setVolatile(true);
            node.setServerId("Test Server");

            node = persistenceManager.nodes().save(node);
            // check persisted node
            assertNotNull(node);
            assertNotNull(node.getId());

            // delete server node
            persistenceManager.nodes().delete(node.getId());
            assertNull(persistenceManager.nodes().get(node.getId()));
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_12_save_new_container()
    {
        logger.info("TC_12_save_new_container");
        try
        {
            // add a new container for test
            Container container = createNewTestContainer("container01");
            container = persistenceManager.containers().save(container);
            // check persisted container
            assertNotNull(container);
            assertNotNull(container.getId());
            assertNotNull(container.getApplications());
            assertEquals(3, container.getApplications().size());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    private Container createNewTestContainer(String containerId) {
        // add a new container for test
        Container container = new Container();
        container.setId(containerId);
        container.setDescription("container test description");
        container.setType(ContainerType.DOCKER);
        container.setName("container for test");
        container.setTag("container tag");
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
    public void TC_13_retrieve_containers()
    {
        logger.info("TC_13_retrieve_containers");
        List<Container> containers  = persistenceManager.containers().list();
        assertNotNull(containers);
        assertFalse(containers.isEmpty());
        logger.info("Found " + containers.size() + " container(s).");
        for (Container container : containers)
        {
            logger.info("Found container " + container.getId());
        }
    }

    @Test
    public void TC_14_delete_container_with_applications_no_PC()
    {
        logger.info("TC_14_delete_container_with_applications_no_PC");
        try
        {
            // add a new container for test
            Container container = createNewTestContainer("container02");

            container = persistenceManager.containers().save(container);
            // check persisted container
            assertNotNull(container);
            assertNotNull(container.getId());

            // delete container
            persistenceManager.containers().delete(container.getId());

            // check that the container was deleted
            assertFalse(persistenceManager.containers().exists("container02"));
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_15_save_new_processing_component()
    {
        logger.info("TC_15_save_new_processing_component");
        try
        {
            // add a new processing component for test
            ProcessingComponent component = createNewProcessingComponent("component01", "container01", 1);

            component = persistenceManager.processingComponents().save(component);
            // check persisted component
            assertNotNull(component);
            assertNotNull(component.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    private ProcessingComponent createNewProcessingComponent(String componentId, String containerId, int paramIndexStart) {
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
        component.setTransient(false);

        Template template = new BasicTemplate();
        template.setName("basic template name");
        template.setTemplateType(TemplateType.VELOCITY);
        component.setTemplate(template);
        component.setTemplateType(TemplateType.VELOCITY);

        component.setVisibility(ProcessingComponentVisibility.CONTRIBUTOR);
        component.setNodeAffinity(NodeAffinity.Any);
        component.setActive(true);

        component.setContainerId(containerId);

        // list of component variables
        Set<Variable> variables = new HashSet<>();

        final Variable var1 = new Variable();
        var1.setKey("var" + paramIndexStart);
        var1.setValue("value" + paramIndexStart);
        variables.add(var1);

        final Variable var2 = new Variable();
        var2.setKey("var" + (paramIndexStart + 1));
        var2.setValue("value" + (paramIndexStart + 1));
        variables.add(var2);

        component.setVariables(variables);

        // set of component parameters
        Set<ParameterDescriptor> parameters = new LinkedHashSet<>();

        final ParameterDescriptor param1 = new ParameterDescriptor();
        param1.setId("testParam" + paramIndexStart);
        param1.setType(ParameterType.REGULAR);
        param1.setDataType(String.class);
        param1.setLabel("Test Param " + paramIndexStart);
        param1.setName("testParam" + paramIndexStart);
        parameters.add(param1);

        final ParameterDescriptor param2 = new ParameterDescriptor();
        param2.setId("testParam" + (paramIndexStart + 1));
        param2.setType(ParameterType.REGULAR);
        param2.setDataType(String.class);
        param2.setLabel("Test Param " + (paramIndexStart + 1));
        param2.setName("testParam" + (paramIndexStart + 1));
        parameters.add(param2);

        // add also a template parameter, which regular parameter(s) inside
        final TemplateParameterDescriptor tParam = new TemplateParameterDescriptor();
        List<ParameterDescriptor> templateParamParameters = new ArrayList<>();
        tParam.setId("templateParam" + paramIndexStart);
        tParam.setType(ParameterType.TEMPLATE);
        tParam.setDataType(String.class);
        tParam.setLabel("Test TemplateParam"+ paramIndexStart);
        tParam.setName("templateParam"+ paramIndexStart);

        final ParameterDescriptor param3 = new ParameterDescriptor();
        param3.setId("testParam" + (paramIndexStart + 2));
        param3.setType(ParameterType.REGULAR);
        param3.setDataType(String.class);
        param3.setLabel("Test Param " + (paramIndexStart + 2));
        param3.setName("testParam" + (paramIndexStart + 2));
        templateParamParameters.add(param3);

        final ParameterDescriptor param4 = new ParameterDescriptor();
        param4.setId("testParam" + (paramIndexStart + 3));
        param4.setType(ParameterType.REGULAR);
        param4.setDataType(String.class);
        param4.setLabel("Test Param " + (paramIndexStart + 3));
        param4.setName("testParam" + (paramIndexStart + 3));
        templateParamParameters.add(param4);

        tParam.setParameters(templateParamParameters);
        parameters.add(tParam);

        component.setParameterDescriptors(parameters);
        component.setTemplateContents("");
        return component;
    }

    @Test
    public void TC_16_retrieve_processing_components()
    {
        logger.info("TC_16_retrieve_processing_components");
        List<ProcessingComponent> components  = persistenceManager.processingComponents().list();
        assertNotNull(components);
        assertFalse(components.isEmpty());
        logger.info("Found " + components.size() + " processing component(s).");
        for (ProcessingComponent component : components)
        {
            logger.info("Found component " + component.getId());
        }
    }

    @Test
    public void TC_17_update_processing_component()
    {
        logger.info("TC_17_update_processing_component");
        try
        {
            ProcessingComponent component  = persistenceManager.processingComponents().get("component01");
            assertNotNull(component);
            assertNotNull(component.getId());
            component.setDescription("Description updated");
            component = persistenceManager.processingComponents().update(component);
            assertEquals("Description updated", component.getDescription());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_18_check_processing_component_existence_by_id()
    {
        logger.info("TC_18_check_processing_component_existence_by_id");
        List<ProcessingComponent> components  = persistenceManager.processingComponents().list();;
        if(!components.isEmpty())
        {
            ProcessingComponent lastComponent = components.getLast();
            String identifier = lastComponent.getId();
            assertTrue(persistenceManager.processingComponents().exists(identifier));
        }
    }

    @Test
    public void TC_19_retrieve_processing_component_by_id()
    {
        logger.info("TC_19_retrieve_processing_component_by_id");
        List<ProcessingComponent> components  = persistenceManager.processingComponents().list();
        if(!components.isEmpty())
        {
            ProcessingComponent lastComponent = components.getLast();
            String identifier = lastComponent.getId();
            ProcessingComponent searchedComponent  = persistenceManager.processingComponents().get(identifier);
            assertNotNull(searchedComponent);
            assertEquals(identifier, searchedComponent.getId());
        }
    }

    @Test
    public void TC_20_delete_processing_component()
    {
        logger.info("TC_20_delete_processing_component");
        try
        {
            // add a new processing component for test
            ProcessingComponent component = createNewProcessingComponent("component02_to_delete", "container01",  5);

            component = persistenceManager.processingComponents().save(component);
            // check persisted component
            assertNotNull(component);
            assertNotNull(component.getId());

            // delete component
            persistenceManager.processingComponents().delete(component.getId());
            assertFalse(persistenceManager.processingComponents().exists("component02_to_delete"));
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_21_delete_container_with_applications_with_PC()
    {
        logger.info("TC_21_delete_container_with_applications_with_PC");
        try
        {
            // add a new container for test
            Container container = createNewTestContainer("container03");
            container = persistenceManager.containers().save(container);
            // check persisted container
            assertTrue(container != null && container.getId() != null);

            // add a new processing component for test
            ProcessingComponent component = createNewProcessingComponent("PC_container_to_delete", "container03", 9);
            component = persistenceManager.processingComponents().save(component);
            // check persisted component
            assertTrue(component != null && component.getId() != null);

            // delete container
            persistenceManager.containers().delete(container.getId());
            // check that the container was deleted
            assertFalse(persistenceManager.containers().exists("container03"));

        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }



    @Test
    public void TC_22_save_new_data_source_component()
    {
        logger.info("TC_22_save_new_data_source_component");
        try
        {
            // add a new data source component for test
            DataSourceComponent component = createNewDataSourceComponent("datasourcecomponent01");

            component = persistenceManager.dataSourceComponents().save(component);
            // check persisted component
            assertNotNull(component);
            assertNotNull(component.getId());
            // check targets
            assertNotNull(component.getTargets());
            assertFalse(component.getTargets().isEmpty());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    private DataSourceComponent createNewDataSourceComponent(String componentId) {
        final String sensorName = "Optical sensor";
        final String dataSourceName = "AWS";

        DataSourceComponent component = new DataSourceComponent(sensorName, dataSourceName);
        component.setId(componentId);
        component.setLabel("component label");
        component.setVersion("component version");
        component.setDescription("component description");
        component.setAuthors("component authors");
        component.setCopyright("component copyright");

        component.setNodeAffinity(NodeAffinity.Any);
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
    public void TC_23_save_new_data_products()
    {
        logger.info("TC_23_save_new_data_products");
        try {
            EOProduct product = new EOProduct();
            product.setId("LC82010442017273LGN00");
            product.setName("LC08_L1TP_201044_20170930_20171013_01_T1");
            product.setAcquisitionDate(LocalDateTime.of(2017, 9, 30, 0, 0));
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
            product = persistenceManager.rasterData().save(product);
            assertNotNull(product);
            assertNotNull(product.getId());
        } catch (PersistenceException | URISyntaxException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_24_retrieve_raster_data_products()
    {
        logger.info("TC_24_retrieve_raster_data_products");
        List<EOProduct> products  = persistenceManager.rasterData().list();
        assertNotNull(products);
        assertFalse(products.isEmpty());
        logger.info("Found " + products.size() + " raster data product(s).");
        for (EOProduct product : products)
        {
            logger.info("\t\tFound raster product \"" + product.getName() + "\"");
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

            // attributes
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

            newVectorProduct = persistenceManager.vectorData().save(newVectorProduct);
            assertNotNull(newVectorProduct);
            assertNotNull(newVectorProduct.getId());
        }
        catch (PersistenceException | URISyntaxException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_26_retrieve_vector_data_products()
    {
        logger.info("TC_26_retrieve_vector_data_products");
        List<VectorData> vectorProducts  = persistenceManager.vectorData().list();
        assertNotNull(vectorProducts);
        assertFalse(vectorProducts.isEmpty());
        logger.info("Found " + vectorProducts.size() + " vector data product(s).");
        for (VectorData vectorProduct : vectorProducts)
        {
            logger.info("\t\tFound vector product \"" + vectorProduct.getName() + "\"");
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
            message.setTopic("Test Topic");
            message.setUserId("test user");
            message = persistenceManager.notifications().save(message);
            assertNotNull(message);
            assertNotNull(message.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_28_save_new_workflow_add_nodes_individually()
    {
        logger.info("TC_28_save_new_workflow_add_nodes_individually");
        try
        {

            WorkflowDescriptor workflow = new WorkflowDescriptor();
            workflow.setName("test_workflow_1");
            workflow.setStatus(Status.DRAFT);
            workflow.setVisibility(Visibility.PRIVATE);
            User adminUser = persistenceManager.users().getByName("admin");
            workflow.setUserId(adminUser.getId());
            workflow.setCreated(LocalDateTime.now());

            workflow = persistenceManager.workflows().save(workflow);
            assertNotNull(workflow);
            assertNotNull(workflow.getId());
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            List<WorkflowNodeDescriptor> nodes = persistenceManager.workflowNodes().list();
            long nodeIdStartingIndex = nodes.getLast().getId();

            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            node1.setCreated(LocalDateTime.now());
            node1.setId(nodeIdStartingIndex+1);
            node1 = persistenceManager.workflowNodes().save(node1, workflow);
            assertNotNull(node1);
            assertNotNull(node1.getId());
            logger.info("Workflow node " + node1.getName() + " saved, ID = " + node1.getId().toString());

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            node2.setComponentType(ComponentType.PROCESSING);
            node2.setCreated(LocalDateTime.now());
            node2.setId(nodeIdStartingIndex+2);
            node2 = persistenceManager.workflowNodes().save(node2, workflow);
            assertNotNull(node2);
            assertNotNull(node2.getId());
            logger.info("Workflow node " + node2.getName() + " saved, ID = " + node2.getId().toString());

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            node3.setComponentType(ComponentType.PROCESSING);
            node3.setCreated(LocalDateTime.now());
            node3.setId(nodeIdStartingIndex+3);
            node3 = persistenceManager.workflowNodes().save(node3, workflow);
            assertNotNull(node3);
            assertNotNull(node3.getId());
            logger.info("Workflow node " + node3.getName() + " saved, ID = " + node3.getId().toString());

            logger.info("After saving each node separate : Workflow has " + workflow.getNodes().size() + " nodes");

            // check persisted workflow nodes
            assertNotNull(workflow.getNodes());
            assertEquals(3, workflow.getNodes().size());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

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
            User adminUser = persistenceManager.users().getByName("admin");
            workflow.setUserId(adminUser.getId());
            workflow.setCreated(LocalDateTime.now());

            List<WorkflowNodeDescriptor> nodes = persistenceManager.workflowNodes().list();
            long nodeIdStartingIndex = nodes.getLast().getId();

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            node1.setCreated(LocalDateTime.now());
            node1.setId(nodeIdStartingIndex+1);

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            node2.setComponentType(ComponentType.PROCESSING);
            node2.setCreated(LocalDateTime.now());
            node2.setId(nodeIdStartingIndex+2);

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            node3.setComponentType(ComponentType.PROCESSING);
            node3.setCreated(LocalDateTime.now());
            node3.setId(nodeIdStartingIndex+3);

            workflow.addNode(node1);
            workflow.addNode(node2);
            workflow.addNode(node3);

            // save the workflow entity
            workflow = persistenceManager.workflows().save(workflow);
            assertNotNull(workflow);
            assertNotNull(workflow.getId());
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            logger.info("After saving nodes cascade : Workflow has " + workflow.getNodes().size() + " nodes");

            // check persisted workflow
            assertNotNull(workflow.getNodes());
            assertEquals(3, workflow.getNodes().size());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
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
            User adminUser = persistenceManager.users().getByName("admin");
            workflow.setUserId(adminUser.getId());
            workflow.setCreated(LocalDateTime.now());

            List<WorkflowNodeDescriptor> nodes = persistenceManager.workflowNodes().list();
            long nodeIdStartingIndex = nodes.getLast().getId();

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            node1.setCreated(LocalDateTime.now());
            node1.setId(nodeIdStartingIndex+1);

            node1.addCustomValue("customName1", "customValue1");
            node1.addCustomValue("customName2", "customValue2");
            node1.addCustomValue("customName3", "customValue3");

            workflow.addNode(node1);

            // save the parent workflow entity
            workflow = persistenceManager.workflows().save(workflow);
            assertNotNull(workflow);
            assertNotNull(workflow.getId());
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            // check persisted workflow
            assertNotNull(workflow.getNodes());
            assertEquals(1, workflow.getNodes().size());

            // check persisted node custom values
            assertEquals(3, workflow.getNodes().getFirst().getCustomValues().size());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
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
            User adminUser = persistenceManager.users().getByName("admin");
            workflow.setUserId(adminUser.getId());
            workflow.setCreated(LocalDateTime.now());

            List<WorkflowNodeDescriptor> nodes = persistenceManager.workflowNodes().list();
            long nodeIdStartingIndex = nodes.getLast().getId();

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            node1.setCreated(LocalDateTime.now());
            node1.setId(nodeIdStartingIndex+ 1);

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            node2.setComponentType(ComponentType.PROCESSING);
            node2.setCreated(LocalDateTime.now());
            node2.setId(nodeIdStartingIndex + 2);

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            node3.setComponentType(ComponentType.PROCESSING);
            node3.setCreated(LocalDateTime.now());
            node3.setId(nodeIdStartingIndex + 3);

            workflow.addNode(node1);
            workflow.addNode(node2);
            workflow.addNode(node3);

            // save the parent workflow entity
            workflow = persistenceManager.workflows().save(workflow);
            assertNotNull(workflow);
            assertNotNull(workflow.getId());
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            logger.info("After saving nodes cascade : Workflow has " + workflow.getNodes().size() + " nodes");
            // check persisted workflow
            assertNotNull(workflow.getNodes());
            assertEquals(3, workflow.getNodes().size());

            // remove a node
            workflow.removeNode(node2);
            workflow = persistenceManager.workflows().update(workflow);

            logger.info("After removing a node : Workflow has " + workflow.getNodes().size() + " nodes");
            // check persisted workflow
            assertNotNull(workflow.getNodes());
            assertEquals(2, workflow.getNodes().size());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
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
            User adminUser = persistenceManager.users().getByName("admin");
            workflow.setUserId(adminUser.getId());
            workflow.setCreated(LocalDateTime.now());

            List<WorkflowNodeDescriptor> nodes = persistenceManager.workflowNodes().list();
            long nodeIdStartingIndex = nodes.getLast().getId();

            // add nodes within this workflow
            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            node1.setCreated(LocalDateTime.now());
            node1.setId(nodeIdStartingIndex + 1);

            WorkflowNodeDescriptor node2 = new WorkflowNodeDescriptor();
            node2.setName("node2");
            node2.setComponentId("component01");
            node2.setComponentType(ComponentType.PROCESSING);
            node2.setCreated(LocalDateTime.now());
            node2.setId(nodeIdStartingIndex + 2);

            WorkflowNodeDescriptor node3 = new WorkflowNodeDescriptor();
            node3.setName("node3");
            node3.setComponentId("component01");
            node3.setComponentType(ComponentType.PROCESSING);
            node3.setCreated(LocalDateTime.now());
            node3.setId(nodeIdStartingIndex + 3);

            WorkflowNodeDescriptor node4 = new WorkflowNodeDescriptor();
            node4.setName("node4");
            node4.setComponentId("component01");
            node4.setComponentType(ComponentType.PROCESSING);
            node4.setCreated(LocalDateTime.now());
            node4.setId(nodeIdStartingIndex + 4);

            workflow.addNode(node1);
            workflow.addNode(node2);
            workflow.addNode(node3);
            workflow.addNode(node4);

            // save the parent workflow entity
            workflow = persistenceManager.workflows().save(workflow);
            assertNotNull(workflow);
            assertNotNull(workflow.getId());
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            logger.info("After saving nodes cascade : Workflow has " + workflow.getNodes().size() + " nodes");
            // check persisted workflow
            assertNotNull(workflow.getNodes());
            assertEquals(4, workflow.getNodes().size());

            // delete workflow
            persistenceManager.workflows().delete(workflow.getId());
            workflow = persistenceManager.workflows().get(workflow.getId());
            assertNotNull(workflow);
            assertNotNull(workflow.getNodes());
            assertEquals(4, workflow.getNodes().size());
            assertFalse(workflow.isActive());
            logger.info("Successfully deactivate workflow ID " + workflow.getId());

        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
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
            User adminUser = persistenceManager.users().getByName("admin");
            workflow.setUserId(adminUser.getId());
            workflow.setCreated(LocalDateTime.now());

            List<WorkflowNodeDescriptor> nodes = persistenceManager.workflowNodes().list();
            long nodeIdStartingIndex = nodes.getLast().getId();

            // add nodes within this workflow
            WorkflowNodeDescriptor sourceNode = new WorkflowNodeDescriptor();
            sourceNode.setName("sourcenode1");
            sourceNode.setComponentId("component01");
            sourceNode.setComponentType(ComponentType.PROCESSING);
            sourceNode.setId(nodeIdStartingIndex + 1);

            // add processing custom values for the node
            sourceNode.addCustomValue("customName1", "customValue1");
            sourceNode.addCustomValue("customName2", "customValue2");
            sourceNode.addCustomValue("customName3", "customValue3");

            WorkflowNodeDescriptor targetNode = new WorkflowNodeDescriptor();
            targetNode.setName("targetnode1");
            targetNode.setComponentId("component01");
            targetNode.setComponentType(ComponentType.PROCESSING);
            targetNode.setId(nodeIdStartingIndex + 2);

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

            ComponentLink componentLink1 = new ComponentLink(sourceNode.getId(), linkInput, linkOutput);
            Set<ComponentLink> links = new HashSet<>();
            links.add(componentLink1);
            targetNode.setIncomingLinks(links);

            // add the nodes within the workflow
            workflow.addNode(sourceNode);
            workflow.addNode(targetNode);

            // save the parent workflow entity
            workflow = persistenceManager.workflows().save(workflow);
            assertNotNull(workflow);
            assertNotNull(workflow.getId());
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            // check persisted workflow
            assertNotNull(workflow.getNodes());
            assertEquals(2, workflow.getNodes().size());

            // check persisted node custom values
            final List<ParameterValue> customValues = workflow.getNodes().getFirst().getCustomValues();
            assertEquals(3, customValues.size());
            logger.info("1st node custom values: ");
            for (ParameterValue pValue: customValues) {
                logger.info("\t\t\t" + pValue.getParameterName() + "=" + pValue.getParameterValue());
            }
            logger.info("1st node custom values (with forEach): ");
            customValues.forEach(v -> logger.info("\t\t\t\t" + v.getParameterName() + "=" + v.getParameterValue()));

            // check persisted incoming links
            assertTrue(workflow.getNodes().get(0).getIncomingLinks() == null || workflow.getNodes().get(0).getIncomingLinks().isEmpty());
            assertTrue(workflow.getNodes().get(1).getIncomingLinks() != null && workflow.getNodes().get(1).getIncomingLinks().size() == 1);
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
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
            User adminUser = persistenceManager.users().getByName("admin");
            workflow.setUserId(adminUser.getId());
            workflow.setCreated(LocalDateTime.now());

            List<WorkflowNodeDescriptor> nodes = persistenceManager.workflowNodes().list();
            long nodeIdStartingIndex = nodes.getLast().getId();

            WorkflowNodeDescriptor node1 = new WorkflowNodeDescriptor();
            node1.setName("node1");
            node1.setComponentId("component01");
            node1.setComponentType(ComponentType.PROCESSING);
            node1.setCreated(LocalDateTime.now());
            node1.setId(nodeIdStartingIndex + 1);

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

            // add target descriptor
            TargetDescriptor targetDescriptor = new TargetDescriptor("targetDescriptor02");
            targetDescriptor.setParentId("component01");
            targetDescriptor.setDataDescriptor(dataDescriptor);
            List<String> targetConstraints = new ArrayList<>();

            targetConstraints.add("target_constraint01");
            targetConstraints.add("target_constraint02");
            targetConstraints.add("target_constraint03");
            targetDescriptor.setConstraints(targetConstraints);

            SourceDescriptor sourceDescriptor = new SourceDescriptor("sourceDescriptor02");
            sourceDescriptor.setParentId("component01");
            sourceDescriptor.setDataDescriptor(dataDescriptor);
            List<String> sourceConstraints = new ArrayList<>();

            sourceConstraints.add("source_constraint01");
            sourceConstraints.add("source_constraint02");
            sourceConstraints.add("source_constraint03");
            sourceDescriptor.setConstraints(sourceConstraints);

            // add the node within the workflow
            workflow.addNode(node1);

            // add a group node
            WorkflowNodeGroupDescriptor nodeGroup = new WorkflowNodeGroupDescriptor();
            nodeGroup.setName("groupNode01");
            nodeGroup.setComponentId("component01");
            nodeGroup.setComponentType(ComponentType.GROUP);
            nodeGroup.setCreated(LocalDateTime.now());
            nodeGroup.setId(0L);

            nodeGroup.addNode(node1);

            // add the group node
            workflow.addNode(nodeGroup);

            ComponentLink componentLink1 = new ComponentLink(node1.getId(), targetDescriptor, sourceDescriptor);
            Set<ComponentLink> links = new HashSet<>();
            links.add(componentLink1);
            node1.setIncomingLinks(links);

            // save the parent workflow entity
            workflow = persistenceManager.workflows().save(workflow);
            assertNotNull(workflow);
            assertNotNull(workflow.getId());
            logger.info("Workflow " + workflow.getName() + " saved, ID = " + workflow.getId().toString());

            // check persisted workflow
            assertNotNull(workflow.getNodes());
            assertEquals(2, workflow.getNodes().size());

            // check persisted node custom values
            assertEquals(3, workflow.getNodes().getFirst().getCustomValues().size());
            assertTrue(workflow.getNodes().getFirst().getIncomingLinks() != null && workflow.getNodes().getFirst().getIncomingLinks().size() == 1);
            assertTrue(workflow.getNodes().get(1).getIncomingLinks() == null || workflow.getNodes().get(1).getIncomingLinks().isEmpty());
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
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
            User adminUser = persistenceManager.users().getByName("admin");
            query.setUserId(adminUser.getId());
            query.setWorkflowNodeId(1L);

            query = persistenceManager.queries().save(query);
            // check persisted query
            assertNotNull(query);
            assertNotNull(query.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
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
            job.setName("test_job1");
            job.setWorkflowId(1L);
            job.setJobOutputPath("/output");
            job.setJobType(JobType.EXECUTION);
            User adminUser = persistenceManager.users().getByName("admin");
            job.setUserId(adminUser.getId());

            job = persistenceManager.jobs().save(job);
            // check persisted job
            assertNotNull(job);
            assertNotNull(job.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_37_save_new_processing_execution_task()
    {
        logger.info("TC_37_save_new_execution_task");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.jobs().list();
            // retrieve processing components
            List<ProcessingComponent> components = persistenceManager.processingComponents().list();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.nodes().list();

            if((jobs != null && !jobs.isEmpty()) &&
                    (components != null && !components.isEmpty()) &&
                    (nodes != null && !nodes.isEmpty()))
            {
                // retrieve first job
                ExecutionJob job = jobs.getFirst();
                // retrieve first component
                ProcessingComponent component = components.getFirst();
                // retrieve first node
                NodeDescription node = nodes.getFirst();

                // add a new task for test
                ProcessingExecutionTask task = new ProcessingExecutionTask();
                task.setResourceId("ProcessingExecutionTask-resourceId01");
                task.setExecutionStatus(ExecutionStatus.RUNNING);
                task.setExecutionNodeHostName(node.getId());
                task.setComponent(component);
                task.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

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

                task = (ProcessingExecutionTask) persistenceManager.tasks().save(task, job);
                // check persisted task
                assertNotNull(task);
                assertNotNull(task.getId());
                assertTrue(task.getId() != 0);
                // check if job correctly updated
                assertTrue(job.getTasks().contains(task));
                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_38_save_new_data_source_query_execution_task()
    {
        logger.info("TC_38_save_new_data_source_query_execution_task");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.jobs().list();
            // retrieve data sources components
            List<DataSourceComponent> components = persistenceManager.dataSourceComponents().list();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.nodes().list();

            if((jobs != null && !jobs.isEmpty()) &&
                    (components != null && !components.isEmpty()) &&
                    (nodes != null && !nodes.isEmpty()))
            {
                // retrieve first job
                ExecutionJob job = jobs.getFirst();
                // retrieve first component
                DataSourceComponent component = components.getFirst();
                // retrieve first node
                NodeDescription node = nodes.getFirst();

                // add a new task for test
                DataSourceExecutionTask task = new DataSourceExecutionTask();
                task.setResourceId("DataSourceExecutionTask-resourceId01");
                task.setExecutionStatus(ExecutionStatus.RUNNING);
                task.setExecutionNodeHostName(node.getId());
                task.setComponent(component);
                task.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

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

                task = (DataSourceExecutionTask) persistenceManager.tasks().save(task, job);
                // check persisted task
                assertNotNull(task);
                assertNotNull(task.getId());
                assertTrue(task.getId() != 0);
                // check if job correctly updated
                assertTrue(job.getTasks().contains(task));
                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_39_save_new_group_execution_task()
    {
        logger.info("TC_39_save_new_group_execution_task");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.jobs().list();
            // retrieve data sources components
            List<DataSourceComponent> dataSourceComponents = persistenceManager.dataSourceComponents().list();
            // retrieve also processing components
            List<ProcessingComponent> processingComponents = persistenceManager.processingComponents().list();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.nodes().list();

            if((jobs != null && !jobs.isEmpty()) &&
                    (dataSourceComponents != null && !dataSourceComponents.isEmpty()) &&
                            (processingComponents != null && !processingComponents.isEmpty()) &&
                                    (nodes != null && !nodes.isEmpty()))
            {
                // retrieve first job
                ExecutionJob job = jobs.getFirst();
                // retrieve first component
                DataSourceComponent dataSourceComponent = dataSourceComponents.getFirst();
                ProcessingComponent processingComponent = processingComponents.getFirst();
                // retrieve first node
                NodeDescription node = nodes.getFirst();

                // add a new processing task for test
                ProcessingExecutionTask processingTask = new ProcessingExecutionTask();
                processingTask.setResourceId("ProcessingExecutionTask-resourceId02");
                processingTask.setExecutionStatus(ExecutionStatus.RUNNING);
                processingTask.setExecutionNodeHostName(node.getId());
                processingTask.setComponent(processingComponent);
                processingTask.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

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
                dataSourceTask.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

                dataSourceTask.setInputParameterValues(inputsValues);


                // make a ExecutionGroup from the 2 tasks
                ExecutionGroup taskGroup = new ExecutionGroup();
                taskGroup.setResourceId("ExecutionGroup-resourceId01");
                taskGroup.setExecutionStatus(ExecutionStatus.RUNNING);
                taskGroup.setExecutionNodeHostName(node.getId());
                taskGroup.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

                // save first the execution group and after the sub-tasks within
                ExecutionGroup taskGroupSaved = (ExecutionGroup)persistenceManager.tasks().save(taskGroup, job);
                // check persisted task group
                assertNotNull(taskGroupSaved);
                assertNotNull(taskGroupSaved.getId());
                assertTrue(taskGroupSaved.getId() != 0);

                // check if job correctly updated
                assertTrue(job.getTasks().contains(taskGroupSaved));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

                processingTask.setGroupTask(taskGroupSaved);
                processingTask = (ProcessingExecutionTask)persistenceManager.tasks().save(processingTask, job);

                dataSourceTask.setGroupTask(taskGroupSaved);
                dataSourceTask = (DataSourceExecutionTask)persistenceManager.tasks().save(dataSourceTask, job);

                // add tasks to saved group
                taskGroupSaved.addTask(processingTask);
                taskGroupSaved.addTask(dataSourceTask);

                persistenceManager.tasks().update(taskGroupSaved);

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_40_save_new_group_execution_task_with_subtasks_unattached_to_job()
    {
        logger.info("TC_40_save_new_group_execution_task_with_subtasks_unattached_to_job");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.jobs().list();
            // retrieve data sources components
            List<DataSourceComponent> dataSourceComponents = persistenceManager.dataSourceComponents().list();
            // retrieve also processing components
            List<ProcessingComponent> processingComponents = persistenceManager.processingComponents().list();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.nodes().list();

            if((jobs != null && !jobs.isEmpty()) &&
                    (dataSourceComponents != null && !dataSourceComponents.isEmpty()) &&
                    (processingComponents != null && !processingComponents.isEmpty()) &&
                    (nodes != null && !nodes.isEmpty()))
            {
                // retrieve first job
                ExecutionJob job = jobs.getFirst();
                // retrieve first component
                DataSourceComponent dataSourceComponent = dataSourceComponents.getFirst();
                ProcessingComponent processingComponent = processingComponents.getFirst();
                // retrieve first node
                NodeDescription node = nodes.getFirst();

                // add a new processing task for test
                ProcessingExecutionTask processingTask = new ProcessingExecutionTask();
                processingTask.setResourceId("ProcessingExecutionTask-resourceId03");
                processingTask.setExecutionStatus(ExecutionStatus.RUNNING);
                processingTask.setExecutionNodeHostName(node.getId());
                processingTask.setComponent(processingComponent);
                processingTask.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

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
                dataSourceTask.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

                dataSourceTask.setInputParameterValues(inputsValues);


                // make a ExecutionGroup from the 2 tasks
                ExecutionGroup taskGroup = new ExecutionGroup();
                taskGroup.setResourceId("ExecutionGroup-resourceId02");
                taskGroup.setExecutionStatus(ExecutionStatus.RUNNING);
                taskGroup.setExecutionNodeHostName(node.getId());
                taskGroup.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

                // save first the execution group and after the sub-tasks within
                ExecutionGroup taskGroupSaved = (ExecutionGroup)persistenceManager.tasks().save(taskGroup, job);
                // check persisted task group
                assertNotNull(taskGroupSaved);
                assertNotNull(taskGroupSaved.getId());
                assertTrue(taskGroupSaved.getId() != 0);
                // check if job correctly updated
                assertTrue(job.getTasks().contains(taskGroupSaved));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");

                processingTask.setGroupTask(taskGroupSaved);
                processingTask = (ProcessingExecutionTask)persistenceManager.tasks().save(processingTask, taskGroupSaved);

                dataSourceTask.setGroupTask(taskGroupSaved);
                dataSourceTask = (DataSourceExecutionTask)persistenceManager.tasks().save(dataSourceTask, taskGroupSaved);

                // add tasks to saved group
                taskGroupSaved.addTask(processingTask);
                taskGroupSaved.addTask(dataSourceTask);

                taskGroupSaved = (ExecutionGroup)persistenceManager.tasks().update(taskGroupSaved);
                assertNotNull(taskGroupSaved);
                assertNotNull(taskGroupSaved.getId());
                assertEquals(2, taskGroupSaved.getTasks().size());
                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_41_save_new_group_execution_task_with_subtasks_unattached_to_job_all_once()
    {
        logger.info("TC_41_save_new_group_execution_task_with_subtasks_unattached_to_job_all_once");
        try
        {
            // retrieve one existing job, for test
            List<ExecutionJob> jobs = persistenceManager.jobs().list();
            // retrieve data sources components
            List<DataSourceComponent> dataSourceComponents = persistenceManager.dataSourceComponents().list();
            // retrieve also processing components
            List<ProcessingComponent> processingComponents = persistenceManager.processingComponents().list();
            // retrieve execution nodes
            List<NodeDescription> nodes = persistenceManager.nodes().list();

            if((jobs != null && !jobs.isEmpty()) &&
                    (dataSourceComponents != null && !dataSourceComponents.isEmpty()) &&
                    (processingComponents != null && !processingComponents.isEmpty()) &&
                    (nodes != null && !nodes.isEmpty()))
            {
                // retrieve first job
                ExecutionJob job = jobs.getFirst();
                // retrieve first component
                DataSourceComponent dataSourceComponent = dataSourceComponents.getFirst();
                ProcessingComponent processingComponent = processingComponents.getFirst();
                // retrieve first node
                NodeDescription node = nodes.getFirst();

                // add a new processing task for test
                ProcessingExecutionTask processingTask = new ProcessingExecutionTask();
                processingTask.setResourceId("ProcessingExecutionTask-resourceId04");
                processingTask.setExecutionStatus(ExecutionStatus.RUNNING);
                processingTask.setExecutionNodeHostName(node.getId());
                processingTask.setComponent(processingComponent);
                processingTask.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

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
                dataSourceTask.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

                dataSourceTask.setInputParameterValues(inputsValues);


                // make a ExecutionGroup from the 2 tasks
                ExecutionGroup taskGroup = new ExecutionGroup();
                taskGroup.setResourceId("ExecutionGroup-resourceId03");
                taskGroup.setExecutionStatus(ExecutionStatus.RUNNING);
                taskGroup.setExecutionNodeHostName(node.getId());
                taskGroup.setWorkflowNodeId(persistenceManager.workflows().get(job.getWorkflowId()).getNodes().getFirst().getId());

                // add tasks to tasks group
                taskGroup.addTask(processingTask);
                taskGroup.addTask(dataSourceTask);

                taskGroup = (ExecutionGroup)persistenceManager.tasks().saveWithSubTasks(taskGroup, job);

                // check persisted task group
                assertNotNull(taskGroup);
                assertNotNull(taskGroup.getId());
                assertTrue(taskGroup.getId() != 0);
                // check if job correctly updated
                assertTrue(job.getTasks().contains(taskGroup));

                logger.info("Now job ID" + job.getId()  + " has " + job.getTasks().size() + " tasks/groups");
            }
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_42_get_running_execution_tasks()
    {
        logger.info("TC_42_get_running_execution_tasks");
        try
        {
            // retrieve running tasks
            List<ExecutionTask> tasks = persistenceManager.tasks().listRunning();
            assertNotNull(tasks);
            assertFalse(tasks.isEmpty());

            for(ExecutionTask task: tasks)
            {
                logger.info("Running task: " + task.getResourceId());
            }
        }
        catch (Exception e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    @Test
    public void TC_43_save_new_wps_component()
    {
        logger.info("TC_43_save_new_wps_component");
        try
        {
            WPSComponent component = createNewWPSComponent("wps01");

            component = persistenceManager.wpsComponents().save(component);
            // check persisted component
            assertNotNull(component);
            assertNotNull(component.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    private WPSComponent createNewWPSComponent(String componentId) {
        // add a new processing component for test
        WPSComponent component = new WPSComponent();
        component.setId(componentId);
        component.setLabel("component label");
        component.setVersion("component version");
        component.setDescription("component description");
        component.setAuthors("component authors");
        component.setCopyright("component copyright");
        component.setVisibility(ProcessingComponentVisibility.CONTRIBUTOR);
        component.setNodeAffinity(NodeAffinity.Any);
        component.setCapabilityName("remote name");
        component.setOwner("avl1");
        Container container = persistenceManager.containers().get("container01");
        component.setService(container);
        component.setRemoteAddress("http://localhost/wps");
        component.setActive(true);
        List<ParameterDescriptor> parameters = new ArrayList<>();

        final ParameterDescriptor param1 = new ParameterDescriptor();
        param1.setId("testParamWPS1");
        param1.setName("testParamWPS1");
        param1.setType(ParameterType.REGULAR);
        param1.setDataType(String.class);
        param1.setLabel("Test Param WPS 1");
        parameters.add(param1);

        final ParameterDescriptor param2 = new ParameterDescriptor();
        param2.setId("testParamWPS2");
        param2.setName("testParamWPS2");
        param2.setType(ParameterType.REGULAR);
        param2.setDataType(String.class);
        param2.setLabel("Test Param WPS 2");
        parameters.add(param2);

        // add also a template parameter, which regular parameter(s) inside
        final TemplateParameterDescriptor tParam = new TemplateParameterDescriptor();
        List<ParameterDescriptor> templateParamParameters = new ArrayList<>();
        tParam.setId("templateParamWPS");
        tParam.setName("templateParamWPS");
        tParam.setType(ParameterType.TEMPLATE);
        tParam.setDataType(String.class);
        tParam.setLabel("Test TemplateParam WPS");

        final ParameterDescriptor param3 = new ParameterDescriptor();
        param3.setId("testParamWPS3");
        param3.setName("testParamWPS3");
        param3.setType(ParameterType.REGULAR);
        param3.setDataType(String.class);
        param3.setLabel("Test Param WPS 3");
        templateParamParameters.add(param3);

        final ParameterDescriptor param4 = new ParameterDescriptor();
        param4.setId("testParamWPS4");
        param4.setName("testParamWPS4");
        param4.setType(ParameterType.REGULAR);
        param4.setDataType(String.class);
        param4.setLabel("Test Param WPS 4");
        templateParamParameters.add(param4);

        tParam.setParameters(templateParamParameters);
        parameters.add(tParam);

        component.setParameters(parameters);
        return component;
    }

    @Test
    public void TC_44_save_new_wms_component()
    {
        logger.info("TC_44_save_new_wms_component");
        try
        {
            WMSComponent component = createNewWMSComponent("wms01");

            component = persistenceManager.wmsComponents().save(component);
            // check persisted component
            assertNotNull(component);
            assertNotNull(component.getId());
        }
        catch (PersistenceException e)
        {
            logger.error(ExceptionUtils.getStackTrace(e));
            fail(e.getMessage());
        }
    }

    private WMSComponent createNewWMSComponent(String componentId) {
        // add a new processing component for test
        WMSComponent component = new WMSComponent();
        component.setId(componentId);
        component.setLabel("component label");
        component.setVersion("component version");
        component.setDescription("component description");
        component.setAuthors("component authors");
        component.setCopyright("component copyright");
        component.setVisibility(ProcessingComponentVisibility.CONTRIBUTOR);
        component.setNodeAffinity(NodeAffinity.Any);
        component.setCapabilityName("remote name");
        component.setOwner("avl1");
        Container container = persistenceManager.containers().get("container01");
        component.setService(container);
        component.setRemoteAddress("http://localhost/wms");
        component.setActive(true);
        List<ParameterDescriptor> parameters = new ArrayList<>();

        final ParameterDescriptor param1 = new ParameterDescriptor();
        param1.setId("testParamWMS1");
        param1.setName("testParamWMS1");
        param1.setType(ParameterType.REGULAR);
        param1.setDataType(String.class);
        param1.setLabel("Test Param WMS 1");
        parameters.add(param1);

        final ParameterDescriptor param2 = new ParameterDescriptor();
        param2.setId("testParamWMS2");
        param2.setName("testParamWMS2");
        param2.setType(ParameterType.REGULAR);
        param2.setDataType(String.class);
        param2.setLabel("Test Param WMS 2");
        parameters.add(param2);

        // add also a template parameter, which regular parameter(s) inside
        final TemplateParameterDescriptor tParam = new TemplateParameterDescriptor();
        List<ParameterDescriptor> templateParamParameters = new ArrayList<>();
        tParam.setId("templateParamWMS");
        tParam.setName("templateParamWMS");
        tParam.setType(ParameterType.TEMPLATE);
        tParam.setDataType(String.class);
        tParam.setLabel("Test TemplateParam WMS");

        final ParameterDescriptor param3 = new ParameterDescriptor();
        param3.setId("testParamWMS3");
        param3.setName("testParamWMS3");
        param3.setType(ParameterType.REGULAR);
        param3.setDataType(String.class);
        param3.setLabel("Test Param WMS 3");
        templateParamParameters.add(param3);

        final ParameterDescriptor param4 = new ParameterDescriptor();
        param4.setId("testParamWMS4");
        param4.setName("testParamWMS4");
        param4.setType(ParameterType.REGULAR);
        param4.setDataType(String.class);
        param4.setLabel("Test Param WMS 4");
        templateParamParameters.add(param4);

        tParam.setParameters(templateParamParameters);
        parameters.add(tParam);

        component.setParameters(parameters);
        return component;
    }

    private static void setupDB() {
        final ConfigurationProvider provider = ConfigurationManager.getInstance();
        provider.setValue("spring.datasource.url", "jdbc:postgresql://localhost:5432/taodata?stringtype=unspecified");
        provider.setValue("spring.datasource.username", "tao");
        provider.setValue("spring.datasource.password", "tao");
    }
}
