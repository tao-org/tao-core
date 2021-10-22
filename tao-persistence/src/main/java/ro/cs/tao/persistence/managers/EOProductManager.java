package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.EOProductProvider;
import ro.cs.tao.persistence.repository.EOProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("eoProductManager")
public class EOProductManager extends EntityManager<EOProduct, String, EOProductRepository> implements EOProductProvider {

    @Override
    public List<EOProduct> getByLocation(String... locations) {
        return repository.getProductsByLocation(new HashSet<>(Arrays.asList(locations)));
    }

    @Override
    public List<EOProduct> getPublicProducts() {
        return repository.getPublicProducts();
    }

    @Override
    public List<EOProduct> getOtherPublishedProducts(String currentUser) {
        return repository.getOtherPublishedProducts(currentUser);
    }

    @Override
    public List<String> getExistingProductNames(String... names) {
        if (names == null || names.length == 0) {
            return new ArrayList<>();
        }
        return repository.getExistingProductNames(Arrays.stream(names).collect(Collectors.toSet()));
    }

    @Override
    public List<EOProduct> getProductsByNames(String... names) {
        if (names == null || names.length == 0) {
            return new ArrayList<>();
        }
        return repository.getProductsByName(Arrays.stream(names).collect(Collectors.toSet()));
    }

    @Override
    public int countOtherProductReferences(String componentId, String name) {
        return repository.getOtherProductReferences(componentId, name);
    }

    @Override
    public void deleteIfNotReferenced(String refererComponentId, String productName) {
        repository.deleteIfNotReferenced(refererComponentId, productName);
    }

    @Override
    public List<EOProduct> getWorkflowOutputs(long workflowId) {
        return repository.getWorkflowOutputs(workflowId);
    }

    @Override
    public long getUserProductsSize(String user) {
        return repository.getUserRasterProductsSize(user);
    }

    @Override
    public long getUserInputProductsSize(String user, String location) {
        return repository.getUserInputRasterProductsSize(user, location);
    }

    @Override
    public LocalDateTime getNewestProductDateForUser(String user, String footprint) {
        final EOProduct prod = repository.getNewestProductForUser(user, footprint);
        if (prod == null) {
            return null;
        }
        return prod.getAcquisitionDate();
    }

    @Override
    public List<EOProduct> getJobOutputs(long jobId) {
        return repository.getJobOutputs(jobId);
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && (existingEntity == (get(entityId) != null));
    }

    @Override
    protected boolean checkEntity(EOProduct eoProduct) {
        return eoProduct != null && eoProduct.getId() != null && !eoProduct.getId().isEmpty() &&
                eoProduct.getName() != null && eoProduct.getGeometry() != null && eoProduct.getProductType() != null &&
                eoProduct.getLocation() != null && eoProduct.getSensorType() != null && eoProduct.getPixelType() != null &&
                eoProduct.getVisibility() != null;
    }

    @Override
    protected boolean checkEntity(EOProduct entity, boolean existingEntity) {
        return checkEntity(entity);
    }
}
