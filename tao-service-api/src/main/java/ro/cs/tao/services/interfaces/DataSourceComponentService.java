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

import ro.cs.tao.Tag;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceConfiguration;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.services.model.component.ProductSetInfo;

import java.security.Principal;
import java.util.List;

public interface DataSourceComponentService extends CRUDService<DataSourceComponent, String> {

    List<DataSourceComponent> getUserDataSourceComponents(String userName);

    List<DataSourceComponent> getSystemDataSourceComponents();

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
     * Creates a user data source component that wraps the given list of products (a product set)
     * @param productNames      The list of names of products
     * @param label             The label of the custom data source component
     * @param principal         The principal to create the component for
     */
    DataSourceComponent createForLocations(List<String> productNames, String label, Principal principal) throws PersistenceException;

    List<Tag> getDatasourceTags();

    DataSourceConfiguration getConfiguration(String dataSourceId) throws PersistenceException;

    void saveConfiguration(DataSourceConfiguration configuration) throws PersistenceException;

    void updateConfiguration(DataSourceConfiguration configuration) throws PersistenceException;

    List<DataSourceComponent> getProductSets(String userName) throws PersistenceException;

    List<DataSourceComponent> getBySource(String dataSourceName) throws PersistenceException;

    List<DataSourceComponent> getBySourceAndSensor(String dataSourceName, String sensor) throws PersistenceException;

    DataSourceComponent updateProductSet(ProductSetInfo productSet) throws PersistenceException;

}
