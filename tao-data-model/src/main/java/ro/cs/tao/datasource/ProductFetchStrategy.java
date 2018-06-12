/*
 * Copyright (C) 2017 CS ROMANIA
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
package ro.cs.tao.datasource;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.ProgressListener;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Implementation strategy for how to retrieve the products.
 *
 * @author Cosmin Cara
 */
public interface ProductFetchStrategy {

    /**
     * Allows callers to add externally defined properties for this fetch strategy
     */
    void addProperties(Properties properties);

    void setCredentials(UsernamePasswordCredentials credentials);

    /**
     * Retrieves a product given its name. Since the data source may not be able to compose the product URI,
     * the default implementation returns <code>null</code>.
     * For data sources that are able to assemble the product URI from its name, this should be overridden.
     * @param productName   The name of the product
     * @return              The path to the retrieved product or <code>null</code> if fetch failed.
     */
    default Path fetch(String productName) throws IOException, InterruptedException {
        return fetch(new EOProduct() {{ setName(productName); }});
    }
    /**
     * Retrieves a product given its descriptor.
     * @param product   The product descriptor
     * @return          The path to the retrieved product or <code>null</code> if fetch failed.
     */
    Path fetch(EOProduct product) throws IOException, InterruptedException;

    /**
     * Allows further fetch operations.
     * The operation is not guaranteed to be immediately terminated.
     *
     */
    default void resume() { }

    /**
     * Cancels the operation in progress, if any.
     * The operation is not guaranteed to be immediately terminated.
     *
     */
    default void cancel() { }

    /**
     * Sets the progress listener to this strategy
     * @param progressListener  The listener
     */
    default void setProgressListener(ProgressListener progressListener) { }

    ProductFetchStrategy clone();
}
