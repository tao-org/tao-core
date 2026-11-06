package ro.cs.tao.persistence;

import ro.cs.tao.eodata.EOProduct;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This interface provides a specialized abstraction layer for managing EOProduct entities.
 * It extends the functionality of the EntityProvider interface and includes additional
 * methods specific to EOProduct entities. The EOProductProvider allows operations such as
 * retrieval, filtering, counting, and deletion of EOProducts based on various parameters.
 */
public interface EOProductProvider extends EntityProvider<EOProduct, String> {

    /**
     * Retrieves a list of EOProduct entities filtered by their associated locations.
     *
     * @param locations an array of location identifiers used to filter the EOProduct entities
     * @return a list of EOProduct entities that are associated with the specified locations
     */
    List<EOProduct> getByLocation(String...locations);
    /**
     * Retrieves a list of publicly available EOProduct entities.
     *
     * @return a list of EOProduct instances that are publicly available
     */
    List<EOProduct> getPublicProducts();
    /**
     * Retrieves a list of EOProduct entities that are published by other users,
     * excluding those associated with the provided user identifier.
     *
     * @param currentUser the identifier of the current user whose published products
     *                    should be excluded from the results
     * @return a list of EOProduct entities published by other users
     */
    List<EOProduct> getOtherPublishedProducts(String currentUser);
    /**
     * Retrieves a list of product names that already exist from a given list of input names.
     *
     * @param names an array of product names to check for existence
     * @return a list of product names that are confirmed to exist
     */
    List<String> getExistingProductNames(String... names);
    /**
     * Retrieves a list of EOProduct entities that correspond to the provided product names.
     *
     * @param names an array of product names used to filter the EOProduct entities
     * @return a list of EOProduct instances that match the specified product names
     */
    List<EOProduct> getProductsByNames(String... names);
    /**
     * Counts the references to products by other components, excluding the one
     * specified by the given component identifier and product name.
     *
     * @param componentId the identifier of the component to be excluded from the count
     * @param name the name of the product whose references are being counted
     * @return the number of references to the specified product by other components
     */
    int countOtherProductReferences(String componentId, String name);
    /**
     * Deletes a product if it is not referenced by other components, excluding the reference
     * from the specified component and product name.
     *
     * @param refererComponentId the identifier of the component whose reference should not be considered
     * @param productName the name of the product to potentially delete
     */
    void deleteIfNotReferenced(String refererComponentId, String productName);
    /**
     * Retrieves a list of EOProduct entities that are associated with the specified workflow.
     *
     * @param workflowId the identifier of the workflow whose associated EOProduct entities
     *                   are to be retrieved
     * @return a list of EOProduct instances associated with the given workflow ID
     */
    List<EOProduct> getWorkflowOutputs(long workflowId);
    /**
     * Retrieves the total number of products associated with a given user.
     *
     * @param user the unique identifier of the user whose product count is to be retrieved
     * @return the total number of products associated with the specified user
     */
    long getUserProductsSize(String user);
    /**
     * Retrieves the number of input products associated with a specific user and location.
     *
     * @param user the unique identifier of the user whose input product count is to be retrieved
     * @param location the location associated with the input products to be retrieved
     * @return the total number of input products associated with the specified user and location
     */
    long getUserInputProductsSize(String user, String location);
    /**
     * Retrieves the timestamp of the most recently created product associated with
     * a specific user and footprint.
     *
     * @param user the unique identifier of the user whose newest product date is to be retrieved
     * @param footprint the footprint or spatial coverage associated with the product
     * @return the timestamp of the newest product for the specified user and footprint,
     *         or null if no matching products are found
     */
    LocalDateTime getNewestProductDateForUser(String user, String footprint);
    /**
     * Retrieves a list of EOProduct entities that are associated with the specified job.
     *
     * @param jobId the unique identifier of the job whose associated EOProduct entities are to be retrieved
     * @return a list of EOProduct instances linked to the given job ID
     */
    List<EOProduct> getJobOutputs(long jobId);

    List<EOProduct> saveAll(List<EOProduct> products);
}
