package ro.cs.tao.datasource;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.ProgressListener;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation strategy for how to retrieve the products.
 *
 * @author Cosmin Cara
 */
public interface ProductFetchStrategy {

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
}
