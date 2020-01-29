package ro.cs.tao.persistence;

import ro.cs.tao.eodata.EOProduct;

import java.util.Date;
import java.util.List;

public interface EOProductProvider extends EntityProvider<EOProduct, String> {

    List<EOProduct> getByLocation(String...locations);
    List<EOProduct> getPublicProducts();
    List<EOProduct> getOtherPublishedProducts(String currentUser);
    List<String> getExistingProductNames(String... names);
    List<EOProduct> getProductsByNames(String... names);
    int countOtherProductReferences(String componentId, String name);
    void deleteIfNotReferenced(String refererComponentId, String productName);
    List<EOProduct> getWorkflowOutputs(long workflowId);
    long getUserProductsSize(String user);
    long getUserInputProductsSize(String user, String location);
    Date getNewestProductDateForUser(String user, String footprint);
    List<EOProduct> getJobOutputs(long jobId);
}
