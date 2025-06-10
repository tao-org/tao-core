/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.services.interfaces;

import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.workspaces.Repository;

import java.io.IOException;
import java.util.List;

/**
 * This interface defines the service for managing EOProduct entities. It extends the CRUDService,
 * which includes the basic operations (create, read, update, delete) for entity management.
 */
public interface ProductService extends CRUDService<EOProduct, String> {

    /**
     * Inspects the products in the specified repository and source path.
     *
     * @param repository    the repository from which the EOProduct entities are retrieved
     * @param sourcePath    the source path used for identifying or filtering products
     * @return a list of EOProduct entities found in the specified repository and source path
     * @throws IOException if an I/O error occurs during the inspection process
     */
    List<EOProduct> inspect(Repository repository, String sourcePath) throws IOException;

    /**
     * Retrieves a list of EOProduct entities based on the provided product names.
     *
     * @param names a variable-length list of product names to search for
     * @return a list of EOProduct entities matching the specified names
     */
    List<EOProduct> getByNames(String... names);

    /**
     * Imports a list of EOProduct entities into the system.
     * Each product in the list is processed and added to the repository.
     *
     * @param products the list of EOProduct entities to be imported
     * @return the number of successfully imported products
     */
    int importProducts(List<EOProduct> products);

    /**
     * Imports products from the specified source path and processes or links them.
     * This method handles two different modes of operation - linking only or processing
     * and importing the products into the system.
     *
     * @param sourcePath the path to the source from which products will be imported
     * @param linkOnly if true, the method will only link the products without importing them
     * @return the number of products successfully imported or linked
     * @throws IOException if an I/O error occurs during the import process
     * @throws QuotaException if the import exceeds the allowed quota
     */
    int importProducts(String sourcePath, boolean linkOnly) throws IOException, QuotaException;

    /**
     * Checks the existence of entities or products based on the provided names.
     *
     * @param names a variable-length list of names to check for existence
     * @return a list of names that exist, as determined by the implementation
     */
    List<String> checkExisting(String... names);

    /**
     * Counts the additional references of a product based on a component ID and product name.
     *
     * @param componentId the unique identifier of the component associated with the product
     * @param name the name of the product whose references are to be counted
     * @return the number of additional references for the specified product
     */
    int countAdditionalProductReferences(String componentId, String name);

    /**
     * Deletes a product if it is not referenced by any other components in the system.
     *
     * @param refererComponentId the unique identifier of the referencing component
     *                           to check against for additional references
     * @param productName        the name of the product to be potentially deleted
     */
    void deleteIfNotReferenced(String refererComponentId, String productName);

    /**
     * Deletes the specified EOProduct from the system.
     *
     * @param product the EOProduct entity to be deleted
     */
    void delete(EOProduct product);
}
