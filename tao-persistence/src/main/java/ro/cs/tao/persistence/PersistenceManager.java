package ro.cs.tao.persistence;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.persistence.annotations.ReadOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.AbstractDataSource;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.data.DataProduct;
import ro.cs.tao.persistence.data.DataSourceType;
import ro.cs.tao.persistence.data.ExecutionNode;
import ro.cs.tao.persistence.data.User;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.DataProductRepository;
import ro.cs.tao.persistence.repository.DataSourceRepository;
import ro.cs.tao.persistence.repository.DataSourceTypeRepository;
import ro.cs.tao.persistence.repository.ExecutionNodeRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DAO
 * Created by oana on 7/18/2017.
 */

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = { "ro.cs.tao.persistence" })
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Scope("singleton")
public class PersistenceManager {

    /** CRUD Repository for DataSource entities */
    @Autowired
    private DataSourceRepository dataSourceRepository;

    /** CRUD Repository for DataSourceType entities */
    @Autowired
    private DataSourceTypeRepository dataSourceTypeRepository;

    /** CRUD Repository for DataProduct entities */
    @Autowired
    private DataProductRepository dataProductRepository;

    /** CRUD Repository for ExecutionNode entities */
    @Autowired
    private ExecutionNodeRepository executionNodeRepository;

    @Transactional
    public Integer saveDataSourceType(String type) throws PersistenceException
    {
        // check method parameters
        if(type == null || type.length() == 0)
        {
            throw new PersistenceException("Invalid parameters were provided for adding new data source type (empty type)!");
        }

        // check if there is already an identical type persisted
        DataSourceType existingType = dataSourceTypeRepository.findByType(type);
        if (existingType != null)
        {
            throw new PersistenceException("Invalid parameters were provided for adding new data source type (type already exists)!");
        }

        DataSourceType dataSourceTypeEnt = new DataSourceType();
        dataSourceTypeEnt.setType(type);

        // save the DataSourceType entity
        dataSourceTypeEnt = dataSourceTypeRepository.save(dataSourceTypeEnt);

        if(dataSourceTypeEnt.getId() == null)
        {
            throw new PersistenceException("Error saving data source type: " + dataSourceTypeEnt.getType());
        }

        return dataSourceTypeEnt.getId();
    }

    @Transactional(readOnly = true)
    public DataSourceType getDataSourceTypeById(final Integer dataSourceTypeId) throws PersistenceException
    {
        // check method parameters
        if (dataSourceTypeId == null || dataSourceTypeId <= 0)
        {
            throw new PersistenceException("Invalid parameters were provided for retrieving data source type by its identifier (" + String.valueOf(dataSourceTypeId) + ")");
        }

        // retrieve the DataSourceType entity based on its id
        final DataSourceType dataSourceType = dataSourceTypeRepository.findById(dataSourceTypeId);
        if (dataSourceType == null)
        {
            throw new PersistenceException("There is no data source type with the specified identfier (" + dataSourceTypeId.toString() + ")");
        }

        return dataSourceType;
    }


    /**
     * Retrieve the list of data source types
     * @return the list of data source types
     */
    @Transactional(readOnly = true)
    public List<DataSourceType> getDataSourceTypes()
    {
        // retrieve all entities DataSourceType
        return (List<DataSourceType>) dataSourceTypeRepository.findAll();
    }

    @Transactional
    public <Q extends DataQuery, S extends AbstractDataSource<Q>> Integer saveDataSource(S dataSource, DataSourceType dataSourceType, String name, String description) throws PersistenceException
    {
        if(dataSource.getCredentials() == null || dataSource.getConnectionString() == null || dataSourceType == null || name == null)
        {
            throw new PersistenceException("Invalid parameters were provided for adding new data source!");
        }

        ro.cs.tao.persistence.data.DataSource dataSourceEnt = new ro.cs.tao.persistence.data.DataSource();
        dataSourceEnt.setName(name);
        //dataSourceEnt.setDataSourceType(Integer.parseInt(DataSourceType.valueOf(dataSourceType.name()).toString()));
        dataSourceEnt.setDataSourceType(dataSourceType);
        dataSourceEnt.setUsername(dataSource.getCredentials().getUserName());
        dataSourceEnt.setPassword(dataSource.getCredentials().getPassword());
        dataSourceEnt.setConnectionString(dataSource.getConnectionString());
        if(description != null)
        {
            dataSourceEnt.setDescription(description);
        }
        dataSourceEnt.setCreatedDate(LocalDateTime.now());

        // save the DataSource entity
        dataSourceEnt = dataSourceRepository.save(dataSourceEnt);

        if(dataSourceEnt.getId() == null)
        {
            throw new PersistenceException("Error saving data source with name: " + dataSourceEnt.getName());
        }

        return dataSourceEnt.getId();

    }

    @Transactional
    public Long saveDataProduct(EOProduct dataProduct, User user) throws PersistenceException
    {
        // check method parameters
        if(dataProduct.getName() == null || dataProduct.getGeometry() == null || dataProduct.getType() == null ||
          dataProduct.getLocation() == null || dataProduct.getSensorType() == null || dataProduct.getPixelType() == null)
        {
            throw new PersistenceException("Invalid parameters were provided for adding new data product!");
        }

        DataProduct dataProductEnt = new DataProduct();
        // set all info
        dataProductEnt.setIdentifier(dataProduct.getId());
        dataProductEnt.setName(dataProduct.getName());
        dataProductEnt.setDataFormat(Integer.parseInt(dataProduct.getType().toString()));
        dataProductEnt.setGeometry(dataProduct.getGeometry());
        if(dataProduct.getCrs() != null)
        {
            dataProductEnt.setCoordinateReferenceSystem(dataProduct.getCrs().toString());
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
            dataProductEnt.setAttributes(Arrays.stream(dataProduct.getAttributes()).collect(Collectors.toMap(a -> a.getName(), a -> a.getValue())));
        }

        dataProductEnt.setCreatedDate(LocalDateTime.now());

        // save the DataProduct entity
        dataProductEnt = dataProductRepository.save(dataProductEnt);

        if(dataProductEnt.getId() == null)
        {
            throw new PersistenceException("Error saving data product with name: " + dataProductEnt.getName());
        }

        return dataProductEnt.getId();
    }

    @Transactional
    public Integer saveExecutionNode(String name, String description, String ipAddress, String sshKey,
                                     String username, String password,
                                     Integer totalCPU, Integer totalRAM, Integer totalHDD) throws PersistenceException
    {
        // check method parameters
        if(name == null || ipAddress == null || username == null || password == null ||
          totalCPU == null || totalHDD == null || totalRAM == null)
        {
            throw new PersistenceException("Invalid parameters were provided for adding new execution node!");
        }

        ExecutionNode executionNodeEnt = new ExecutionNode();
        // set all info
        executionNodeEnt.setName(name);
        if(description != null)
        {
            executionNodeEnt.setDescription(description);
        }
        executionNodeEnt.setIpAddress(ipAddress);
        if(sshKey != null)
        {
            executionNodeEnt.setSshKey(sshKey);
        }
        executionNodeEnt.setUsername(username);
        executionNodeEnt.setPassword(password);
        executionNodeEnt.setTotalCPU(totalCPU);
        executionNodeEnt.setTotalRAM(totalRAM);
        executionNodeEnt.setTotalHDD(totalHDD);
        executionNodeEnt.setCreatedDate(LocalDateTime.now());
        executionNodeEnt.setActive(true);

        // save the ExecutionNode entity
        executionNodeEnt = executionNodeRepository.save(executionNodeEnt);

        if(executionNodeEnt.getId() == null)
        {
            throw new PersistenceException("Error saving execution node with name: " + executionNodeEnt.getName());
        }

        return executionNodeEnt.getId();
    }

}
