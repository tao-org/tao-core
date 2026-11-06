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

import ro.cs.tao.Sort;
import ro.cs.tao.Tag;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceConfiguration;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.services.model.component.ProductSetInfo;

import java.security.Principal;
import java.util.List;
import java.util.Set;

public interface DataSourceComponentService extends CRUDService<DataSourceComponent, String> {

    /**
     * Retrieves all the data source components of a given user.
     * @param userId    The user identifier
     */
    List<DataSourceComponent> getUserDataSourceComponents(String userId);

    /**
     * Retrieves all the system (i.e., not belonging to a user) data source components.
     */
    List<DataSourceComponent> getSystemDataSourceComponents();

    /**
     * Retrieves one page of system data source components.
     */
    List<DataSourceComponent> getSystemDataSourceComponents(int pageNumber, int pageSize, Sort sort);

    /**
     * Counts system data source components.
     */
    long countSystemDataSourceComponents();

    /**
     * Retrieves the data source components that don't have the given identifiers.
     * @param ids   The set of identifiers to be excluded
     */
    List<DataSourceComponent> getOhterDataSourceComponents(Set<String> ids);

    /**
     * Creates a user data source component that wraps the given list of products.
     * @param products      The list of products
     * @param dataSource    The data source name
     * @param queryId       (optional) The id of the query from which the products originated
     * @param label         The label of the custom data source component
     * @param principal     The principal to create the component for
     */
    DataSourceComponent createForProducts(List<EOProduct> products, String dataSource, Long queryId,
                                          String label, Principal principal) throws PersistenceException;
    /**
     * Creates a user data source component that wraps the given list of products.
     * @param productNames      The list of names of products
     * @param sensor            The type of products (must be of the same satellite)
     * @param dataSource        The name of the data source
     * @param queryId           (optional) The id of the query from which the products originated
     * @param label             The label of the custom data source component
     * @param principal         The principal to create the component for
     */
    DataSourceComponent createForLocations(List<String> productNames, String sensor, String dataSource, Long queryId,
                                           String label, Principal principal) throws PersistenceException;

    /**
     * Creates a data source component based on a repository query with the specified properties.
     *
     * @param label             A human-readable label for the query.
     * @param srcRepositoryId   The identifier of the source repository.
     * @param srcPath           The path within the source repository.
     * @param dstRepositoryId   The identifier of the destination repository.
     * @param dstPath           The path within the destination repository.
     * @param itemsCount        The maximum number of items to be processed in each execution.
     * @param fileNamePattern   The pattern for matching the file names to be processed.
     * @return                  A {@code DataSourceComponent} representing the created repository query.
     * @throws PersistenceException If there is an error during the creation of the repository query.
     */
    DataSourceComponent createRepositoryQuery(String label,
                                              String srcRepositoryId, String srcPath,
                                              String dstRepositoryId, String dstPath,
                                              int itemsCount, String fileNamePattern) throws PersistenceException;

    /**
     * Creates a user data source component that wraps the given list of products (a product set)
     * @param productNames      The list of names of products
     * @param label             The label of the custom data source component
     * @param principal         The principal to create the component for
     */
    DataSourceComponent createForLocations(List<String> productNames, String label, Principal principal) throws PersistenceException;

    DataSourceComponent createForLocations(List<String> productNames, String repositoryId, String label, Principal principal) throws PersistenceException;

    /**
     * Retrieves all the tags associated to data source components.
     */
    List<Tag> getDatasourceTags();

    /**
     * Retrieves the configuration of the given data source.
     * @param dataSourceId  The data source identifier
     * @throws PersistenceException
     */
    DataSourceConfiguration getConfiguration(String dataSourceId) throws PersistenceException;

    /**
     * Saves the configuration of a data source.
     * @param configuration     The data source configuration structure
     * @throws PersistenceException
     */
    void saveConfiguration(DataSourceConfiguration configuration) throws PersistenceException;

    /**
     * Updates the configuration of a data source.
     * @param configuration     The data source configuration structure
     * @throws PersistenceException
     */
    void updateConfiguration(DataSourceConfiguration configuration) throws PersistenceException;

    /**
     * Retrieves all the product sets of a given user.
     * @param userId    The user identifier
     * @throws PersistenceException
     */
    List<DataSourceComponent> getProductSets(String userId) throws PersistenceException;

    /**
     * Retrieves all the data source components for a given data source.
     * @param dataSourceName    The data source name
     * @throws PersistenceException
     */
    List<DataSourceComponent> getBySource(String dataSourceName) throws PersistenceException;

    /**
     * Retrieves all the data source components for a given data source and collection (or sensor).
     * @param dataSourceName    The data source name
     * @param sensor    The collection or sensor
     * @throws PersistenceException
     */
    List<DataSourceComponent> getBySourceAndSensor(String dataSourceName, String sensor) throws PersistenceException;

    /**
     * Updates the data source component associated with a product set.
     * @param productSet    The product set information
     * @throws PersistenceException
     */
    DataSourceComponent updateProductSet(ProductSetInfo productSet) throws PersistenceException;

    List<DataSourceComponent> getRepositoryQueries(String userId) throws PersistenceException;

    List<String> getQueryPathsForRepository(String repositoryId) throws PersistenceException;

    void deleteQueryForRepository(String repositoryId, String path) throws PersistenceException;

}
