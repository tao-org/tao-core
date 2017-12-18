package ro.cs.tao.persistence;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.execution.ExecutionJob;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.VectorData;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.MessagePersister;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.ContainerRepository;
import ro.cs.tao.persistence.repository.EOProductRepository;
import ro.cs.tao.persistence.repository.ExecutionJobRepository;
import ro.cs.tao.persistence.repository.ExecutionTaskRepository;
import ro.cs.tao.persistence.repository.MessageRepository;
import ro.cs.tao.persistence.repository.NodeRepository;
import ro.cs.tao.persistence.repository.ParameterDescriptorRepository;
import ro.cs.tao.persistence.repository.ProcessingComponentRepository;
import ro.cs.tao.persistence.repository.ServiceRepository;
import ro.cs.tao.persistence.repository.VectorDataRepository;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeServiceStatus;
import ro.cs.tao.topology.ServiceDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//import ro.cs.tao.persistence.data.User;

/**
 * DAO
 * Created by oana on 7/18/2017.
 */

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Scope("singleton")
public class PersistenceManager implements MessagePersister {

    /** Constant for the identifier member name of execution node entity */
    private static final String NODE_IDENTIFIER_PROPERTY_NAME = "hostName";

    /** Constant for the identifier member id of container entity */
    private static final String CONTAINER_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of processing component entity */
    private static final String COMPONENT_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of EOProduct entity */
    private static final String DATA_PRODUCT_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of execution job entity */
    private static final String JOB_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of execution task entity */
    private static final String TASK_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for user messages page size */
    private static final int MESSAGES_PAGE_SIZE = 10;

    /** Constant for the identifier member name of EOProduct entity */
    private static final String MESSAGE_TIMESTAMP_PROPERTY_NAME = "timestamp";

    /** CRUD Repository for EOProduct entities */
    @Autowired
    private EOProductRepository eoProductRepository;

    /** CRUD Repository for VectorData entities */
    @Autowired
    private VectorDataRepository vectorDataRepository;

    /** CRUD Repository for NodeDescription entities */
    @Autowired
    private NodeRepository nodeRepository;

    /** CRUD Repository for ServiceDescription entities */
    @Autowired
    private ServiceRepository serviceRepository;

    /** CRUD Repository for Container entities */
    @Autowired
    private ContainerRepository containerRepository;

    /** CRUD Repository for ProcessingComponent entities */
    @Autowired
    private ProcessingComponentRepository processingComponentRepository;

    /** CRUD Repository for ParameterDescriptor entities */
    @Autowired
    private ParameterDescriptorRepository parameterDescriptorRepository;

    /** CRUD Repository for ExecutionJob entities */
    @Autowired
    private ExecutionJobRepository executionJobRepository;

    /** CRUD Repository for ExecutionTask entities */
    @Autowired
    private ExecutionTaskRepository executionTaskRepository;

    /** CRUD Repository for Mesaage entities */
    @Autowired
    private MessageRepository messageRepository;

//    /** CRUD Repository for DataSource entities */
//    @Autowired
//    private DataSourceRepository dataSourceRepository;
//
//    /** CRUD Repository for DataSourceType entities */
//    @Autowired
//    private DataSourceTypeRepository dataSourceTypeRepository;

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

    private boolean checkEOProduct(EOProduct eoProduct)
    {
        if(eoProduct == null)
        {
            return false;
        }
        if(eoProduct.getId() == null || eoProduct.getId().isEmpty())
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

    private boolean checkVectorData(VectorData vectorDataProduct)
    {
        if(vectorDataProduct == null)
        {
            return false;
        }
        if(vectorDataProduct.getId() == null || vectorDataProduct.getId().isEmpty())
        {
            return false;
        }
        if(vectorDataProduct.getName() == null)
        {
            return false;
        }
        if(vectorDataProduct.getGeometry() == null)
        {
            return false;
        }
        if(vectorDataProduct.getLocation() == null)
        {
            return false;
        }

        return true;
    }

    @Transactional
    public EOProduct saveEOProduct(EOProduct eoProduct) throws PersistenceException {
        // check method parameters
        if (!checkEOProduct(eoProduct)) {
            throw new PersistenceException("Invalid parameters were provided for adding new EO data product!");
        }

        // save the EOProduct entity
        EOProduct savedEOProduct = eoProductRepository.save(eoProduct);

        if (savedEOProduct.getId() == null) {
            throw new PersistenceException("Error saving EO data product with name: " + eoProduct.getName());
        }

        return savedEOProduct;
    }

    /**
     * Retrieve all EOProduct
     * @return
     */
    @Transactional(readOnly = true)
    public List<EOProduct> getEOProducts()
    {
        final List<EOProduct> products = new ArrayList<>();
        // retrieve products
        products.addAll(((List<EOProduct>) eoProductRepository.findAll(new Sort(Sort.Direction.ASC, DATA_PRODUCT_IDENTIFIER_PROPERTY_NAME))));
        return products;
    }

    @Transactional
    public VectorData saveVectorDataProduct(VectorData vectorDataProduct) throws PersistenceException {
        // check method parameters
        if (!checkVectorData(vectorDataProduct)) {
            throw new PersistenceException("Invalid parameters were provided for adding new vector data product!");
        }

        // save the VectorData entity
        VectorData savedVectorData = vectorDataRepository.save(vectorDataProduct);

        if (savedVectorData.getId() == null) {
            throw new PersistenceException("Error saving vector data product with name: " + vectorDataProduct.getName());
        }

        return savedVectorData;
    }

    /**
     * Retrieve all VectorData
     * @return
     */
    @Transactional(readOnly = true)
    public List<VectorData> getVectorDataProducts()
    {
        final List<VectorData> products = new ArrayList<>();
        // retrieve products
        products.addAll(((List<VectorData>) vectorDataRepository.findAll(new Sort(Sort.Direction.ASC, DATA_PRODUCT_IDENTIFIER_PROPERTY_NAME))));
        return products;
    }

    private boolean checkServiceDescription(ServiceDescription service)
    {
        if(service == null)
        {
            return false;
        }
        if(service.getName() == null || service.getName().isEmpty())
        {
            return false;
        }
        if(service.getVersion() == null || service.getVersion().isEmpty())
        {
            return false;
        }

        return true;
    }

    @Transactional
    public ServiceDescription saveServiceDescription(ServiceDescription service) throws PersistenceException
    {
        // check method parameters
        if(!checkServiceDescription(service))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new service!");
        }

        // check if there is already another service with the same name and version
        final ServiceDescription serviceWithSameName = serviceRepository.findByNameAndVersion(service.getName(), service.getVersion());
        if (serviceWithSameName != null)
        {
            throw new PersistenceException("There is already another service with the name: " + service.getName());
        }

        // save the new ServiceDescription entity and return it
        return serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public boolean checkIfExistsServiceByNameAndVersion(final String serviceName, final String serviceVersion)
    {
        boolean result = false;

        if (serviceName != null && !serviceName.isEmpty())
        {
            // try to retrieve ServiceDescription after its name
            final ServiceDescription serviceEnt = serviceRepository.findByNameAndVersion(serviceName, serviceVersion);
            if (serviceEnt != null)
            {
                result = true;
            }
        }

        return result;
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
        if(node.getUserName() == null)
        {
            return false;
        }
        if(node.getUserPass() == null)
        {
            return false;
        }
        if(node.getProcessorCount() < 0)
        {
            return false;
        }
        if(node.getDiskSpaceSizeGB() < 0)
        {
            return false;
        }
        if(node.getMemorySizeGB() < 0)
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

        // save the services first
        for(NodeServiceStatus serviceStatus: node.getServicesStatus())
        {
            if (!checkIfExistsServiceByNameAndVersion(serviceStatus.getServiceDescription().getName(), serviceStatus.getServiceDescription().getVersion()))
            {
                serviceRepository.save(serviceStatus.getServiceDescription());
            }
            else
            {
                // retrieve the existent entities and associate them on the node
                ServiceDescription existingService = serviceRepository.findByNameAndVersion(serviceStatus.getServiceDescription().getName(), serviceStatus.getServiceDescription().getVersion());
                serviceStatus.setServiceDescription(existingService);
            }
        }

        // save the new NodeDescription entity and return it
        return nodeRepository.save(node);
    }

    @Transactional
    public NodeDescription updateExecutionNode(NodeDescription node) throws PersistenceException
    {
        // check method parameters
        if(!checkExecutionNode(node))
        {
            throw new PersistenceException("Invalid parameters were provided for updating the execution node " + (node != null && node.getHostName() != null ? "(host name " + node.getHostName() + ")" : "") + "!");
        }

        // check if there is such node (to update) with the given host name
        final NodeDescription existingNode = nodeRepository.findByHostName(node.getHostName());
        if (existingNode == null)
        {
            throw new PersistenceException("There is no node with the given host name: " + node.getHostName());
        }

        return nodeRepository.save(node);
    }


    @Transactional(readOnly = true)
    public List<NodeDescription> getNodes()
    {
        final List<NodeDescription> nodes = new ArrayList<>();
        // retrieve nodes and filter them by active flag
        nodes.addAll(((List<NodeDescription>) nodeRepository.findAll(new Sort(Sort.Direction.ASC, NODE_IDENTIFIER_PROPERTY_NAME))).stream()
          .filter(node -> node.getActive())
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

    @Transactional
    public NodeDescription deleteExecutionNode(final String hostName) throws PersistenceException
    {
        // check method parameters
        if (hostName == null || hostName.isEmpty())
        {
            throw new PersistenceException("Invalid parameters were provided for deleting execution node (host name \""+ String.valueOf(hostName) +"\") !");
        }

        // retrieve NodeDescription after its host name
        final NodeDescription nodeEnt = nodeRepository.findByHostName(hostName);
        if (nodeEnt == null)
        {
            throw new PersistenceException("There is no execution node with the specified host name: " + hostName);
        }

        // deactivate the node
        nodeEnt.setActive(false);

        // save it
        return nodeRepository.save(nodeEnt);
    }

    private boolean checkParameterDescriptor(ParameterDescriptor parameterDesc)
    {
        if(parameterDesc == null)
        {
            return false;
        }
        if(parameterDesc.getId() == null || parameterDesc.getId().isEmpty())
        {
            return false;
        }
        if(parameterDesc.getType() == null)
        {
            return false;
        }
        if(parameterDesc.getDataType() == null)
        {
            return false;
        }
        if(parameterDesc.getLabel() == null || parameterDesc.getLabel().isEmpty())
        {
            return false;
        }

        return true;
    }

    /*@Transactional
    public ParameterDescriptor saveParameterDescriptor(ParameterDescriptor parameter, ProcessingComponent component) throws PersistenceException
    {
        // check method parameters
        if(!checkParameterDescriptor(parameter) || !checkProcessingComponent(component))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new processing component parameter!");
        }

        // check if there is already another parameter with the same identifier
        final ParameterDescriptor parameterWithSameId = parameterDescriptorRepository.findById(parameter.getId());
        if (parameterWithSameId != null)
        {
            throw new PersistenceException("There is already another processing parameter with the identifier: " + parameter.getId());
        }

        parameter.setProcessingComponent(component);

        // save the new ParameterDescriptor entity and return it
        return parameterDescriptorRepository.save(parameter);
    }*/

    /*public ParameterDescriptor saveParameterDescriptorWithinExistingTransaction(ParameterDescriptor parameter, ProcessingComponent component) throws PersistenceException
    {
        // check method parameters
        if(!checkParameterDescriptor(parameter) || !checkProcessingComponent(component))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new processing component parameter!");
        }

        // check if there is already another parameter with the same identifier
        final ParameterDescriptor parameterWithSameId = parameterDescriptorRepository.findById(parameter.getId());
        if (parameterWithSameId != null)
        {
            throw new PersistenceException("There is already another processing parameter with the identifier: " + parameter.getId());
        }

        parameter.setProcessingComponent(component);

        // save the new ParameterDescriptor entity and return it
        return parameterDescriptorRepository.save(parameter);
    }*/

    private boolean checkProcessingComponent(ProcessingComponent component)
    {
        if(component == null)
        {
            return false;
        }
        if(component.getId() == null || component.getId().isEmpty())
        {
            return false;
        }
        if(component.getLabel() == null)
        {
            return false;
        }
        if(component.getVersion() == null)
        {
            return false;
        }
        if(component.getDescription() == null)
        {
            return false;
        }
        if(component.getAuthors() == null)
        {
            return false;
        }

        if(component.getCopyright() == null)
        {
            return false;
        }

        if(component.getFileLocation() == null)
        {
            return false;
        }

        if(component.getTemplateType() == null)
        {
            return false;
        }

        /*if(component.getTemplateName() == null)
        {
            return false;
        }*/

        if(component.getVisibility() == null)
        {
            return false;
        }

        for(ParameterDescriptor parameter: component.getParameterDescriptors())
        {
            if(!checkParameterDescriptor(parameter))
            {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public ProcessingComponent saveProcessingComponent(ProcessingComponent component) throws PersistenceException
    {
        // check method parameters
        if(!checkProcessingComponent(component))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new processing component !");
        }

        // check if there is already another component with the same identifier
        final ProcessingComponent componentWithSameId = processingComponentRepository.findById(component.getId());
        if (componentWithSameId != null)
        {
            throw new PersistenceException("There is already another component with the identifier: " + component.getId());
        }

        // save the new ProcessingComponent entity and return it
        return processingComponentRepository.save(component);
    }

    @Transactional
    public ProcessingComponent updateProcessingComponent(ProcessingComponent component) throws PersistenceException
    {
        // check method parameters
        if(!checkProcessingComponent(component))
        {
            throw new PersistenceException("Invalid parameters were provided for updating the processing component " + (component != null && component.getId() != null ? "(identifier " + component.getId() + ")" : "") + "!");
        }

        // check if there is such component (to update) with the given identifier
        final ProcessingComponent existingComponent = processingComponentRepository.findById(component.getId());
        if (existingComponent == null)
        {
            throw new PersistenceException("There is no processing component with the given identifier: " + component.getId());
        }

        return processingComponentRepository.save(component);
    }

    /**
     * Retrieve active processing components with SYSTEM and CONTRIBUTOR visibility
     * @return
     */
    @Transactional(readOnly = true)
    public List<ProcessingComponent> getProcessingComponents()
    {
        final List<ProcessingComponent> components = new ArrayList<>();
        // retrieve components and filter them
        components.addAll(((List<ProcessingComponent>) processingComponentRepository.findAll(new Sort(Sort.Direction.ASC, COMPONENT_IDENTIFIER_PROPERTY_NAME))).stream()
          .filter(c -> (c.getVisibility().equals(ProcessingComponentVisibility.SYSTEM) || c.getVisibility().equals(ProcessingComponentVisibility.CONTRIBUTOR)) && c.getActive())
            .collect(Collectors.toList()));
        return components;
    }

    /**
     * TODO (User entity from data model)
     * Retrieve processing components with USER visibility, for a given user
     * @return
     */
    /*@Transactional(readOnly = true)
    public List<ProcessingComponent> getUserProcessingComponents(User user)
    {
        final List<ProcessingComponent> components = new ArrayList<>();
        // retrieve components and filter them
        components.addAll(((List<ProcessingComponent>) processingComponentRepository.findAll(new Sort(Sort.Direction.ASC, COMPONENT_IDENTIFIER_PROPERTY_NAME))).stream()
          // TODO c.getUser() = user  =>  add user property on component
          .filter(c -> c.getVisibility().equals(ProcessingComponentVisibility.USER))
          .collect(Collectors.toList()));
        return components;
    }*/

    @Transactional(readOnly = true)
    public boolean checkIfExistsComponentById(final String id)
    {
        boolean result = false;

        if (id != null && !id.isEmpty())
        {
            // try to retrieve ProcessingComponent after its identifier
            final ProcessingComponent componentEnt = processingComponentRepository.findById(id);
            if (componentEnt != null)
            {
                result = true;
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ProcessingComponent getProcessingComponentById(final String id) throws PersistenceException
    {
        // check method parameters
        if (id == null || id.isEmpty())
        {
            throw new PersistenceException("Invalid parameters were provided for searching processing component by identifier ("+ String.valueOf(id) +") !");
        }

        // retrieve ProcessingComponent after its identifier
        final ProcessingComponent componentEnt = processingComponentRepository.findById(id);
        if (componentEnt == null)
        {
            throw new PersistenceException("There is no processing component with the specified identifier: " + id);
        }

        return componentEnt;
    }

    @Transactional
    public ProcessingComponent deleteProcessingComponent(final String id) throws PersistenceException
    {
        // check method parameters
        if (id == null || id.isEmpty())
        {
            throw new PersistenceException("Invalid parameters were provided for deleting processing component (id\""+ String.valueOf(id) +"\") !");
        }

        // retrieve ProcessingComponent after its id
        final ProcessingComponent componentEnt = processingComponentRepository.findById(id);
        if (componentEnt == null)
        {
            throw new PersistenceException("There is no processing component with the specified id: " + id);
        }

        // deactivate the processing component
        componentEnt.setActive(false);

        // save it
        return processingComponentRepository.save(componentEnt);
    }

    private boolean checkExecutionJob(ExecutionJob job, boolean existingEntity)
    {
        if(job == null)
        {
            return false;
        }
        if(existingEntity && job.getId() == null)
        {
            return false;
        }
        if(!existingEntity && job.getId() != null)
        {
            return false;
        }

        return true;
    }

    @Transactional
    public ExecutionJob saveExecutionJob(ExecutionJob job) throws PersistenceException
    {
        // check method parameters
        if(!checkExecutionJob(job, false))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new execution job !");
        }

        // save the new ExecutionJob entity and return it
        return executionJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public boolean checkIfExistsExecutionJobById(final Long jobId)
    {
        boolean result = false;

        if (jobId != null && jobId > 0)
        {
            // try to retrieve ExecutionJob after its identifier
            final ExecutionJob jobEnt = executionJobRepository.findById(jobId);
            if (jobEnt != null)
            {
                result = true;
            }
        }

        return result;
    }

    @Transactional
    public ExecutionJob updateExecutionJob(ExecutionJob job) throws PersistenceException
    {
        // check method parameters
        if(!checkExecutionJob(job, true))
        {
            throw new PersistenceException("Invalid parameters were provided for updating the execution job " + (job != null && job.getId() != null ? "(identifier " + job.getId() + ")" : "") + "!");
        }

        // check if there is such job (to update) with the given identifier
        final ExecutionJob existingJob = executionJobRepository.findById(job.getId());
        if (existingJob == null)
        {
            throw new PersistenceException("There is no execution job with the given identifier: " + job.getId());
        }

        // save the updated entity
        return executionJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<ExecutionJob> getAllJobs()
    {
        final List<ExecutionJob> jobs = new ArrayList<>();
        // retrieve jobs and filter them
        jobs.addAll(((List<ExecutionJob>) executionJobRepository.findAll(new Sort(Sort.Direction.ASC, JOB_IDENTIFIER_PROPERTY_NAME))).stream()
          .collect(Collectors.toList()));
        return jobs;
    }

    private boolean checkExecutionTask(ExecutionTask task, ExecutionJob job, boolean existingEntity)
    {
        // check first the job (that should already be persisted)
        if (!checkExecutionJob(job, true) || !checkIfExistsExecutionJobById(job.getId()))
        {
            return false;
        }
        if(!checkExecutionTask(task, existingEntity))
        {
            return false;
        }
        return true;
    }

    private boolean checkExecutionTask(ExecutionTask task, boolean existingEntity)
    {
        if(task == null)
        {
            return false;
        }
        if(existingEntity && task.getId() == null)
        {
            return false;
        }
        if(!existingEntity && task.getId() != null)
        {
            return false;
        }
        if(existingEntity && (task.getResourceId() == null || task.getResourceId().isEmpty()))
        {
            return false;
        }

        return true;
    }

    @Transactional
    public ExecutionTask saveExecutionTask(ExecutionTask task, ExecutionJob job) throws PersistenceException
    {
        // check method parameters
        if(!checkExecutionTask(task, job, false))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new execution task !");
        }

        // check if there is already task with the same resource identifier
        if (task.getResourceId() != null) {
            final ExecutionTask taskWithSameResourceId = executionTaskRepository.findByResourceId(task.getResourceId());
            if (taskWithSameResourceId != null) {
                throw new PersistenceException("There is already another task with the resource identifier: " + task.getResourceId());
            }
        }

        task.setJob(job);

        // save the new ExecutionTask entity
        final ExecutionTask savedExecutionTask =  executionTaskRepository.save(task);

        // add the task to job tasks collection
        List<ExecutionTask> jobTasks = job.getTasks();
        jobTasks.add(task);
        job.setTasks(jobTasks);

        executionJobRepository.save(job);

        return savedExecutionTask;
    }

    @Transactional
    public ExecutionTask updateExecutionTask(ExecutionTask task) throws PersistenceException
    {
        // check method parameters
        if(!checkExecutionTask(task, true))
        {
            throw new PersistenceException("Invalid parameters were provided for updating the execution task " + (task != null && task.getId() != null ? "(identifier " + task.getId() + ")" : "") + "!");
        }

        // check if there is such task (to update) with the given identifier
        final ExecutionTask existingTask = executionTaskRepository.findById(task.getId());
        if (existingTask == null)
        {
            throw new PersistenceException("There is no execution task with the given identifier: " + task.getId());
        }

        // save the updated entity
        return executionTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<ExecutionTask> getRunningTasks()
    {
        final List<ExecutionTask> runningTasks = new ArrayList<>();
        // retrieve tasks and filter them
        runningTasks.addAll(((List<ExecutionTask>) executionTaskRepository.findAll(new Sort(Sort.Direction.ASC, TASK_IDENTIFIER_PROPERTY_NAME))).stream()
          .filter(t -> (t.getExecutionStatus() == ExecutionStatus.RUNNING || t.getExecutionStatus() == ExecutionStatus.QUEUED_ACTIVE))
          .collect(Collectors.toList()));
        return runningTasks;
    }

    @Transactional(readOnly = true)
    public ExecutionTask getTaskById(Long id) throws PersistenceException
    {
        final ExecutionTask existingTask = executionTaskRepository.findById(id);
        if (existingTask == null)
        {
            throw new PersistenceException("There is no execution task with the given identifier: " + id);
        }
        return existingTask;
    }

    @Transactional(readOnly = true)
    public ExecutionTask getTaskByResourceId(String id) throws PersistenceException
    {
        final ExecutionTask existingTask = executionTaskRepository.findByResourceId(id);
        if (existingTask == null)
        {
            throw new PersistenceException("There is no execution task with the given resource identifier: " + id);
        }
        return existingTask;
    }

    private boolean checkMessage(Message message)
    {
        if(message == null)
        {
            return false;
        }
        if(message.getTimestamp() == 0)
        {
            return false;
        }
        if(message.getUserId() == 0)
        {
            return false;
        }

        if(message.getSource() == null)
        {
            return false;
        }

        if(message.getData() == null)
        {
            return false;
        }

        return true;
    }

    @Transactional
    public Message saveMessage(Message message) throws PersistenceException
    {
        // check method parameters
        if(!checkMessage(message)) {
            throw new PersistenceException("Invalid parameters were provided for adding new message !");
        }

        // save the new Message entity and return it
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public Page<Message> getUserMessages(Integer userId, Integer pageNumber)
    {
        PageRequest pageRequest = new PageRequest(pageNumber - 1, MESSAGES_PAGE_SIZE, Sort.Direction.DESC, MESSAGE_TIMESTAMP_PROPERTY_NAME);
        return messageRepository.findByUserId(userId, pageRequest);
    }

    private boolean checkContainer(Container container)
    {
        if(container == null)
        {
            return false;
        }
        if(container.getId() == null || container.getId().isEmpty())
        {
            return false;
        }
        if(container.getName() == null)
        {
            return false;
        }
        if(container.getTag() == null)
        {
            return false;
        }

        for(Application application: container.getApplications())
        {
            if(!checkApplication(application))
            {
                return false;
            }
        }

        return true;
    }

    private boolean checkApplication(Application application)
    {
        if(application == null)
        {
            return false;
        }
        if(application.getName() == null || application.getName().isEmpty())
        {
            return false;
        }

        return true;
    }

    @Transactional
    public Container saveContainer(Container container) throws PersistenceException
    {
        // check method parameters
        if(!checkContainer(container))
        {
            throw new PersistenceException("Invalid parameters were provided for adding new container !");
        }

        // check if there is already another container with the same identifier
        final Container containerWithSameId = containerRepository.findById(container.getId());
        if (containerWithSameId != null)
        {
            throw new PersistenceException("There is already another container with the identifier: " + container.getId());
        }

        // save the new Container entity and return it
        return containerRepository.save(container);
    }

    @Transactional
    public Container updateContainer(Container container) throws PersistenceException
    {
        // check method parameters
        if(!checkContainer(container))
        {
            throw new PersistenceException("Invalid parameters were provided for updating the container " + (container != null && container.getId() != null ? "(identifier " + container.getId() + ")" : "") + "!");
        }

        // check if there is such container (to update) with the given identifier
        final Container existingContainer = containerRepository.findById(container.getId());
        if (existingContainer == null)
        {
            throw new PersistenceException("There is no container with the given identifier: " + container.getId());
        }

        return containerRepository.save(container);
    }

    /**
     * Retrieve existing containers
     * @return
     */
    @Transactional(readOnly = true)
    public List<Container> getContainers()
    {
        final List<Container> containers = new ArrayList<>();
        // retrieve containers and filter them
        containers.addAll(((List<Container>) containerRepository.findAll(new Sort(Sort.Direction.ASC, CONTAINER_IDENTIFIER_PROPERTY_NAME))).stream()
          .collect(Collectors.toList()));
        return containers;
    }

    /**
     * Retrieve container by its identifier
     * @return
     */
    @Transactional(readOnly = true)
    public Container getContainerById(String id) throws PersistenceException
    {
        // check method parameters
        if(id == null || StringUtils.isEmpty(id))
        {
            throw new PersistenceException("Invalid parameter was provided for retrieving a container (empty identifier)");
        }

        // check if there is such container (to retrieve) with the given identifier
        final Container existingContainer = containerRepository.findById(id);
        if (existingContainer == null)
        {
            throw new PersistenceException("There is no container with the given identifier: " + id);
        }

        return existingContainer;
    }

    /**
     * Retrieve container by its identifier
     * @return
     */
    @Transactional(readOnly = true)
    public boolean checkIfExistsContainerById(String id) throws PersistenceException
    {
        boolean result = false;

        // check method parameters
        if(id == null || StringUtils.isEmpty(id))
        {
            throw new PersistenceException("Invalid parameter was provided for verifying a container existence (empty identifier)");
        }

        // check if there is such container (to retrieve) with the given identifier
        final Container existingContainer = containerRepository.findById(id);
        if (existingContainer != null)
        {
            result = true;
        }

        return result;
    }

    /**
     * Delete container
     * @param id - container identifier
     * @return
     * @throws PersistenceException
     */
    @Transactional
    public void deleteContainer(String id) throws PersistenceException
    {
        // check method parameters
        if(id == null || StringUtils.isEmpty(id))
        {
            throw new PersistenceException("Invalid parameter was provided for deleting a container (empty identifier)");
        }

        // check if there is such container (to delete) with the given identifier
        final Container existingContainer = containerRepository.findById(id);
        if (existingContainer == null)
        {
            throw new PersistenceException("There is no container with the given identifier: " + id);
        }

        containerRepository.delete(existingContainer);
    }

}
