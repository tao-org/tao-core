package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ro.cs.tao.persistence.data.DataProduct;

/**
 * CRUD repository for DataProduct entities
 * 
 * @author oana
 *
 */
@Repository
@Qualifier(value = "dataProductRepository")
@Transactional
public interface DataProductRepository extends PagingAndSortingRepository<DataProduct, Long>{

	/**
	 * Find DataProduct entity by its identifier
	 * @param id - the given data product identifier
	 * @return the corresponding DataProduct entity
	 */
	DataProduct findById(Long id);
	
	/**
	 * Find DataProduct entity by its name
	 * @param name - the given data product name
	 * @return the corresponding DataProduct entity
	 */
	DataProduct findByName(String name);
}
