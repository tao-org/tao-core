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

import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.security.Principal;
import java.util.List;

public interface DataSourceComponentService extends CRUDService<DataSourceComponent> {

    List<DataSourceComponent> getUserDataSourceComponents(String userName);

    /**
     * Creates a user data source component that wraps the given list of products.
     * @param products      The list of products
     * @param principal     The principal to create the component for
     */
    DataSourceComponent createFor(List<EOProduct> products, Principal principal) throws PersistenceException;
}
