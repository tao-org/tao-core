package ro.cs.tao.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.datasource.AbstractDataSource;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.eodata.Attribute;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.data.DataProduct;
import ro.cs.tao.persistence.data.DataSourceType;
import ro.cs.tao.persistence.data.ExecutionNode;
import ro.cs.tao.persistence.data.User;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.*;
import ro.cs.tao.topology.NodeDescription;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO
 * Created by oana on 7/18/2017.
 */

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Scope("singleton")
public class PersistenceManager {

    /** Constant for the identifier member name of execution node entity */
    private static final String NODE_IDENTIFIER_PROPERTY_NAME = "hostName";

    /** CRUD Repository for EOProduct entities */
    @Autowired
    private EOProductRepository eoProductRepository;

    /** CRUD Repository for NodeDescription entities */
    @Autowired
    private NodeRepository nodeRepository;

//    /** CRUD Repository for DataSource entities */
//    @Autowired
//    private DataSourceRepository dataSourceRepository;
//
//    /** CRUD Repository for DataSourceType entities */
//    @Autowired
//    private DataSourceTypeRepository dataSourceTypeRepository;
//
//    /** CRUD Repository for DataProduct entities */
//    @Autowired
//    private DataProductRepository dataProductRepository;
//
//    /** CRUD Repository for ExecutionNode entities */
//    @Autowired
//    private ExecutionNodeRepository executionNodeRepository;
//
//    /** CRUD Repository for ProcessingComponent entities */
//    @Autowired
//    private ProcessingComponentRepository processingComponentRepository;

//    @Transactional
//    public Integer saveDataSourceType(String type) throws PersistenceException
//    {
//        // check method parameters
//        if(type == null || type.length() == 0)
//        {
//            throw new PersistenceException("Invalid parameters were provided for adding new data source type (empty type)!");
//        }
//
//        // check if there is already an identical type persisted
//        DataSourceType existingType = dataSourceTypeRepository.findByType(type);
//        if (existingType != null)
//        {
//            throw new PersistenceException("Invalid parameters were provided for adding new data source type (type already exists)!");
//        }
//
//        DataSourceType dataSourceTypeEnt = new DataSourceType();
//        dataSourceTypeEnt.setType(type);
//
//        // save the DataSourceType entity
//        dataSourceTypeEnt = dataSourceTypeRepository.save(dataSourceTypeEnt);
//
//        if(dataSourceTypeEnt.getId() == null)
//        {
//            throw new PersistenceException("Error saving data source type: " + dataSourceTypeEnt.getType());
//        }
//
//        return dataSourceTypeEnt.getId();
//    }
//
//    @Transactional(readOnly = true)
//    public DataSourceType getDataSourceTypeById(final Integer dataSourceTypeId) throws PersistenceException
//    {
//        // check method parameters
//        if (dataSourceTypeId == null || dataSourceTypeId <= 0)
//        {
//            throw new PersistenceException("Invalid parameters were provided for retrieving data source type by its identifier (" + String.valueOf(dataSourceTypeId) + ")");
//        }
//
//        // retrieve the DataSourceType entity based on its id
//        final DataSourceType dataSourceType = dataSourceTypeRepository.findById(dataSourceTypeId);
//        if (dataSourceType == null)
//        {
//            throw new PersistenceException("There is no data source type with the specified identfier (" + dataSourceTypeId.toString() + ")");
//        }
//
//        return dataSourceType;
//    }
//
//
//    /**
//     * Retrieve the list of data source types
//     * @return the list of data source types
//     */
//    @Transactional(readOnly = true)
//    public List<DataSourceType> getDataSourceTypes()
//    {
//        // retrieve all entities DataSourceType
//        return (List<DataSourceType>) dataSourceTypeRepository.findAll();
//    }
//
//    @Transactional
//    public <Q extends DataQuery, S extends AbstractDataSource<Q>> Integer saveDataSource(S dataSource, DataSourceType dataSourceType, String name, String description) throws PersistenceException
//    {
//        if(dataSource == null || dataSource.getCredentials() == null || dataSource.getConnectionString() == null ||
//          dataSourceType == null || name == null)
//        {
//            throw new PersistenceException("Invalid parameters were provided for adding new data source!");
//        }
//
//        ro.cs.tao.persistence.data.DataSource dataSourceEnt = new ro.cs.tao.persistence.data.DataSource();
//        dataSourceEnt.setName(name);
//        dataSourceEnt.setDataSourceType(dataSourceType);
//        dataSourceEnt.setUsername(dataSource.getCredentials().getUserName());
//        dataSourceEnt.setPassword(dataSource.getCredentials().getPassword());
//        dataSourceEnt.setConnectionString(dataSource.getConnectionString());
//        if(description != null)
//        {
//            dataSourceEnt.setDescription(description);
//        }
//        dataSourceEnt.setCreatedDate(LocalDateTime.now());
//
//        // save the DataSource entity
//        dataSourceEnt = dataSourceRepository.save(dataSourceEnt);
//
//        if(dataSourceEnt.getId() == null)
//        {
//            throw new PersistenceException("Error saving data source with name: " + dataSourceEnt.getName());
//        }
//
//        return dataSourceEnt.getId();
//
//    }

    /**@Transactional
    public Long saveDataProduct(EOProduct dataProduct, User user) throws PersistenceException
    {
        // check method parameters
        if(dataProduct == null ||
          dataProduct.getName() == null || dataProduct.getGeometry() == null || dataProduct.getProductType() == null ||
          dataProduct.getLocation() == null || dataProduct.getSensorType() == null || dataProduct.getPixelType() == null)
        {
            throw new PersistenceException("Invalid parameters were provided for adding new data product!");
        }

        DataProduct dataProductEnt = new DataProduct();
        // set all info
        dataProductEnt.setIdentifier(dataProduct.getId());
        dataProductEnt.setName(dataProduct.getName());
        dataProductEnt.setDataFormat(Integer.parseInt(dataProduct.getProductType()));
        dataProductEnt.setGeometry(dataProduct.getPolygon());
        if(dataProduct.getCrs() != null)
        {
            dataProductEnt.setCoordinateReferenceSystem(dataProduct.getCrs());
        }
        dataProductEnt.setLocation(dataProduct.getLocation().toString());
        dataProductEnt.setSensorType(Integer.parseInt(dataProduct.getSensorType().toString()));
        if(dataProduct.getAcquisitionDate() != null)
        {
            dataProductEnt.setAcquisitionDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(dataProduct.getAcquisitionDate().getTime()), ZoneId.systemDefault()));
        }
        dataProductEnt.setPixelType(Integer.parseInt(dataProduct.getPixelType().toString()));
        // TODO: update width and height after corrections
        dataProductEnt.setWidth(dataProduct.getWidth() > 0 ? dataProduct.getWidth() : 0);
        dataProductEnt.setHeight(dataProduct.getHeight() > 0 ? dataProduct.getHeight() : 0);

        if(user != null)
        {
            dataProductEnt.setUser(user);
        }

        // attributs
        if(dataProduct.getAttributes() != null && dataProduct.getAttributes().length > 0)
        {
            dataProductEnt.setAttributes(Arrays.stream(dataProduct.getAttributes()).collect(Collectors.toMap(Attribute::getName, Attribute::getValue)));
        }

        dataProductEnt.setCreatedDate(LocalDateTime.now());

        // save the DataProduct entity
        dataProductEnt = dataProductRepository.save(dataProductEnt);

        if(dataProductEnt.getId() == null)
        {
            throw new PersistenceException("Error saving data product with name: " + dataProductEnt.getName());
        }

        return dataProductEnt.getId();
    }**/

//    // TODO Use a data model entity when ready
//    @Transactional
//    public Integer saveExecutionNode(String name, String description, String ipAddress, String sshKey,
//                                     String username, String password,
//                                     Integer totalCPU, Integer totalRAM, Integer totalHDD) throws PersistenceException
//    {
//        // check method parameters
//        if(name == null || ipAddress == null || username == null || password == null ||
//          totalCPU == null || totalHDD == null || totalRAM == null)
//        {
//            throw new PersistenceException("Invalid parameters were provided for adding new execution node!");
//        }
//
//        ExecutionNode executionNodeEnt = new ExecutionNode();
//        // set all info
//        executionNodeEnt.setName(name);
//        if(description != null)
//        {
//            executionNodeEnt.setDescription(description);
//        }
//        executionNodeEnt.setIpAddress(ipAddress);
//        if(sshKey != null)
//        {
//            executionNodeEnt.setSshKey(sshKey);
//        }
//        executionNodeEnt.setUsername(username);
//        executionNodeEnt.setPassword(password);
//        executionNodeEnt.setTotalCPU(totalCPU);
//        executionNodeEnt.setTotalRAM(totalRAM);
//        executionNodeEnt.setTotalHDD(totalHDD);
//        executionNodeEnt.setCreatedDate(LocalDateTime.now());
//        executionNodeEnt.setActive(true);
//
//        // save the ExecutionNode entity
//        executionNodeEnt = executionNodeRepository.save(executionNodeEnt);
//
//        if(executionNodeEnt.getId() == null)
//        {
//            throw new PersistenceException("Error saving execution node with name: " + executionNodeEnt.getName());
//        }
//
//        return executionNodeEnt.getId();
//    }
//
//
//    @Transactional
//    public Integer saveProcessingComponent(ProcessingComponent processingComponent, User owner) throws PersistenceException
//    {
//        // check method parameters
//        if(processingComponent == null ||
//          processingComponent.getId() == null || processingComponent.getLabel() == null ||
//          processingComponent.getVersion() == null || processingComponent.getDescription() == null ||
//          processingComponent.getCopyright() == null || processingComponent.getFileLocation() == null ||
//          processingComponent.getWorkingDirectory() == null || processingComponent.getTemplateType() == null ||
//          processingComponent.getTemplate() == null || processingComponent.getTemplate().getName() == null)
//        {
//            throw new PersistenceException("Invalid parameters were provided for adding new processing component!");
//        }
//
//        // create new entity
//        ro.cs.tao.persistence.data.ProcessingComponent processingComponentEnt = new ro.cs.tao.persistence.data.ProcessingComponent();
//        // set all info
//        processingComponentEnt.setName(processingComponent.getId());
//
//        processingComponentEnt.setLabel(processingComponent.getLabel());
//        processingComponentEnt.setVersion(processingComponent.getVersion());
//        processingComponentEnt.setDescription(processingComponent.getDescription());
//        processingComponentEnt.setAuthors(processingComponent.getAuthors());
//        processingComponentEnt.setCopyright(processingComponent.getCopyright());
//
//        processingComponentEnt.setMainToolFileLocation(processingComponent.getFileLocation());
//        processingComponentEnt.setWorkingDirectory(processingComponent.getWorkingDirectory());
//        processingComponentEnt.setTemplateType(Integer.parseInt(processingComponent.getTemplateType().toString()));
//        processingComponentEnt.setTemplateName(processingComponent.getTemplate().getName());
//
//        // TODO variables
//        // TODO parameters
//
//        // ?? TODO processing component
//
//
//        processingComponentEnt.setCreatedDate(LocalDateTime.now());
//        processingComponentEnt.setActive(true);
//
//        // save the new entity
//        processingComponentEnt = processingComponentRepository.save(processingComponentEnt);
//
//        if(processingComponentEnt.getId() == null)
//        {
//            throw new PersistenceException("Error saving processing component with name: " + processingComponentEnt.getName());
//        }
//
//        return processingComponentEnt.getId();
//    }

    private boolean checkEOProduct(EOProduct eoProduct)
    {
        if(eoProduct == null)
        {
            return false;
        }
        if(eoProduct.getName() == null)
        {
            return false;
        }
        if(eoProduct.getGeometry() == null)
        {
            return false;
        }
        if(eoProduct.getProductType() == null)
        {
            return false;
        }
        if(eoProduct.getLocation() == null)
        {
            return false;
        }
        if(eoProduct.getSensorType() == null)
        {
            return false;
        }
        if(eoProduct.getPixelType() == null)
        {
            return false;
        }

        return true;
    }

    @Transactional
    public EOProduct saveEOProduct(EOProduct eoProduct) throws PersistenceException {
        // check method parameters
        if (!checkEOProduct(eoProduct)) {
            throw new PersistenceException("Invalid parameters were provided for adding new EO product!");
        }

        // save the EOProduct entity
        EOProduct savedEOProduct = eoProductRepository.save(eoProduct);

        if (savedEOProduct.getId() == null) {
            throw new PersistenceException("Error saving EO product with name: " + eoProduct.getName());
        }

        return savedEOProduct;
    }

    private boolean checkExecutionNode(NodeDescription node)
    {
        if(node == null)
        {
            return false;
        }
        if(node.getHostName() == null)
        {
            return false;
        }
        if(node.getIpAddr() == null)
        {
            return false;
        }
        if(node.getUserName() == null)
        {
            return false;
        }
        if(node.getUserPass() == null)
        {
            return false;
        }
        if(node.getProcessorCount() <= 0)
        {
            return false;
        }
        if(node.getDiskSpaceSizeGB() <= 0)
        {
            return false;
        }
        if(node.getMemorySizeGB() <= 0)
        {
            return false;
        }

        return true;
    }

    @Transactional
    public NodeDescription saveExecutionNode(NodeDescription node) throws PersistenceException
    {
        // check method parameters
        if(!checkExecutionNode(node))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new execution node!");
        }

        // check if there is already another node with the same host name
        final NodeDescription nodeWithSameHostName = nodeRepository.findByHostName(node.getHostName());
        if (nodeWithSameHostName != null)
        {
            throw new PersistenceException("There is already another node with the host name: " + node.getHostName());
        }

        // save the new NodeDescription entity
        NodeDescription savedNode = nodeRepository.save(node);

        if(savedNode.getHostName() == null)
        {
            throw new PersistenceException("Error saving execution node with host name: " + node.getHostName());
        }

        return savedNode;
    }

    @Transactional
    public NodeDescription updateExecutionNode(NodeDescription node) throws PersistenceException
    {
        // check method parameters
        if(!checkExecutionNode(node))
        {
            throw new PersistenceException("Invalid parameters were provided for updating the execution node " + (node != null && node.getHostName() != null ? "(host name " + node.getHostName() + ")" : "") + "!");
        }

        NodeDescription updatedNode = nodeRepository.save(node);

//        // TODO check creation date and modified date
//        if(updatedNode.getIpAddr() == null)
//        {
//            throw new PersistenceException("Error updating execution node with host name: " + node.getHostName());
//        }

        return updatedNode;
    }


    @Transactional(readOnly = true)
    public List<NodeDescription> getNodes()
    {
        final List<NodeDescription> nodes = new ArrayList<>();
        // retrieve nodes and filter them
        nodes.addAll(((List<NodeDescription>) nodeRepository.findAll(new Sort(Sort.Direction.ASC, NODE_IDENTIFIER_PROPERTY_NAME))).stream()
          .collect(Collectors.toList()));
        return nodes;
    }

    @Transactional(readOnly = true)
    public boolean checkIfExistsNodeByHostName(final String hostName)
    {
        boolean result = false;

        if (hostName != null && !hostName.isEmpty())
        {
            // try to retrieve NodeDescription after its host name
            final NodeDescription nodeEnt = nodeRepository.findByHostName(hostName);
            if (nodeEnt != null)
            {
                result = true;
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public NodeDescription getNodeByHostName(final String hostName) throws PersistenceException
    {
        // check method parameters
        if (hostName == null || hostName.isEmpty())
        {
            throw new PersistenceException("Invalid parameters were provided for searching execution node by host name ("+ String.valueOf(hostName) +") !");
        }

        // retrieve NodeDescription after its host name
        final NodeDescription nodeEnt = nodeRepository.findByHostName(hostName);
        if (nodeEnt == null)
        {
            throw new PersistenceException("There is no execution node with the specified host name: " + hostName);
        }

        return nodeEnt;
    }

}
