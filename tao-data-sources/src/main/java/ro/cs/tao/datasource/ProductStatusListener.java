package ro.cs.tao.datasource;

import ro.cs.tao.eodata.EOProduct;

/**
 * @author Cosmin Cara
 */
public interface ProductStatusListener {
    /**
     * Signals that the download of a product has started
     */
    void downloadStarted(EOProduct product);
    /**
     * Signals that the download of a product has completed successfully
     */
    void downloadCompleted(EOProduct product);
    /**
     * Signals that the download of a product was not successful
     */
    void downloadFailed(EOProduct product);
}
