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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ProductService extends CRUDService<EOProduct, String> {

    List<EOProduct> inspect(Path sourcePath) throws IOException;
    List<EOProduct> getByNames(String... names);
    int importProducts(List<EOProduct> products);
    int importProducts(String sourcePath, boolean linkOnly) throws IOException, QuotaException;
    List<String> checkExisting(String... names);
    int countAdditionalProductReferences(String componentId, String name);
    void deleteIfNotReferenced(String refererComponentId, String productName);
    void delete(EOProduct product);
}