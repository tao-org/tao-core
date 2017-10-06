package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ParameterDescriptor;

/**
 * CRUD repository for ParameterDescriptor entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "parameterDescriptorRepository")
@Transactional
public interface ParameterRepository extends PagingAndSortingRepository<ParameterDescriptor, String> {

    /**
     * Find ParameterDescriptor entity by its identifier
     * @param id - the given parameter descriptor identifier
     * @return the corresponding ParameterDescriptor entity
     */
    ParameterDescriptor findById(String id);

    /**
     * Find ParameterDescriptor entity by its label
     * @param label - the given label
     * @return the corresponding ParameterDescriptor entity
     */
    ParameterDescriptor findByLabel(String label);
}
