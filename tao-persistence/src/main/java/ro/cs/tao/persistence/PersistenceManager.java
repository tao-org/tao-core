package ro.cs.tao.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Format;
import ro.cs.tao.persistence.data.DataProduct;
import ro.cs.tao.persistence.data.User;
import ro.cs.tao.persistence.data.enums.DataFormat;
import ro.cs.tao.persistence.data.enums.PixelType;
import ro.cs.tao.persistence.data.enums.SensorType;
import ro.cs.tao.persistence.repository.DataProductRepository;

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

    /** CRUD Repository for DataProduct entities */
    @Autowired
    private DataProductRepository dataProductRepository;

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
        dataProductEnt.setName(dataProduct.getName());
        dataProductEnt.setDataFormat(Integer.parseInt(DataFormat.valueOf(dataProduct.getType().toString()).toString()));
        dataProductEnt.setGeometry(dataProduct.getGeometry());
        if(dataProduct.getCrs() != null)
        {
            dataProductEnt.setCoordinateReferenceSystem(dataProduct.getCrs().toString());
        }
        dataProductEnt.setLocation(dataProduct.getLocation().toString());
        dataProductEnt.setSensorType(Integer.parseInt(SensorType.valueOf(dataProduct.getSensorType().toString()).toString()));
        if(dataProduct.getAcquisitionDate() != null)
        {
            dataProductEnt.setAcquisitionDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(dataProduct.getAcquisitionDate().getTime()), ZoneId.systemDefault()));
        }
        dataProductEnt.setPixelType(Integer.parseInt(PixelType.valueOf(dataProduct.getPixelType().toString()).toString()));
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
}
