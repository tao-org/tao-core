package ro.cs.tao.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.common.AbstractDataSource;
import ro.cs.tao.datasource.common.DataQuery;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.data.DataProduct;
import ro.cs.tao.persistence.data.ExecutionNode;
import ro.cs.tao.persistence.data.User;
import ro.cs.tao.persistence.data.enums.DataSourceType;
import ro.cs.tao.persistence.repository.DataProductRepository;
import ro.cs.tao.persistence.repository.DataSourceRepository;
import ro.cs.tao.persistence.repository.ExecutionNodeRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    /** CRUD Repository for DataProduct entities */
    @Autowired
    private DataProductRepository dataProductRepository;

    /** CRUD Repository for ExecutionNode entities */
    @Autowired
    private ExecutionNodeRepository executionNodeRepository;

    @Transactional
    public <R extends EOData, Q extends DataQuery<R>, S extends AbstractDataSource<R, Q>> Integer saveDataSource(S dataSource, DataSourceType dataSourceType, String name, String description)
    {
        if(dataSource.getCredentials() == null || dataSource.getConnectionString() == null || name == null)
        {
            // TODO throw exception and remove code above
            System.out.println("Invalid arguments for saving a data source!");
            return 0;
        }

        ro.cs.tao.persistence.data.DataSource dataSourceEnt = new ro.cs.tao.persistence.data.DataSource();
        dataSourceEnt.setName(name);
        dataSourceEnt.setDataSourceType(Integer.parseInt(DataSourceType.valueOf(dataSourceType.name()).toString()));
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
            // TODO throw exception
            System.out.println("Error saving data source " + dataSourceEnt.getName());
        }

        return dataSourceEnt.getId();

    }

    @Transactional
    public Long saveDataProduct(EOProduct dataProduct, User user)
    {
        // check method parameters
        if(dataProduct.getName() == null || dataProduct.getGeometry() == null || dataProduct.getType() == null ||
          dataProduct.getLocation() == null || dataProduct.getSensorType() == null || dataProduct.getPixelType() == null)
        {
            // TODO throw exception and remove code above
            System.out.println("Invalid arguments for saving a data product!");
            return 0L;
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

        // TODO data source

        dataProductEnt.setCreatedDate(LocalDateTime.now());

        // save the DataProduct entity
        dataProductEnt = dataProductRepository.save(dataProductEnt);

        if(dataProductEnt.getId() == null)
        {
            // TODO throw exception
            System.out.println("Error saving data product " + dataProductEnt.getName());
        }

        return dataProductEnt.getId();
    }

    @Transactional
    public Integer saveExecutionNode(String name, String description, String ipAddress, String sshKey,
                                     String username, String password,
                                     Integer totalCPU, Integer totalRAM, Integer totalHDD)
    {
        // check method parameters
        if(name == null || ipAddress == null || username == null || password == null ||
          totalCPU == null || totalHDD == null || totalRAM == null)
        {
            // TODO throw exception and remove code above
            System.out.println("Invalid arguments for saving an execution node!");
            return 0;
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
        executionNodeEnt.setActive(true);

        // save the ExecutionNode entity
        executionNodeEnt = executionNodeRepository.save(executionNodeEnt);

        if(executionNodeEnt.getId() == null)
        {
            // TODO throw exception
            System.out.println("Error saving execution node " + executionNodeEnt.getName());
        }

        return executionNodeEnt.getId();
    }
}
