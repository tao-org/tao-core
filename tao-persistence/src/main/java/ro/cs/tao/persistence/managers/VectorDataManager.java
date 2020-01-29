package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.eodata.VectorData;
import ro.cs.tao.persistence.VectorDataProvider;
import ro.cs.tao.persistence.repository.VectorDataRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("vectorDataManager")
public class VectorDataManager extends EntityManager<VectorData, String, VectorDataRepository> implements VectorDataProvider {

    @Override
    public List<VectorData> getByLocation(String... locations) {
        return repository.getProductsByLocation(new HashSet<>(Arrays.asList(locations)));
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && (existingEntity == (get(entityId) != null));
    }

    @Override
    protected boolean checkEntity(VectorData vectorDataProduct) {
        return vectorDataProduct != null && vectorDataProduct.getId() != null && !vectorDataProduct.getId().isEmpty() &&
                vectorDataProduct.getName() != null && vectorDataProduct.getGeometry() != null &&
                vectorDataProduct.getLocation() != null;
    }
}
