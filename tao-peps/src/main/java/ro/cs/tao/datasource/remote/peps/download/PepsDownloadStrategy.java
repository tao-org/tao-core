package ro.cs.tao.datasource.remote.peps.download;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.peps.PepsDataSource;
import ro.cs.tao.datasource.remote.peps.PepsMetadataResponseHandler;
import ro.cs.tao.datasource.remote.result.json.JsonResponseParser;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
public class PepsDownloadStrategy extends DownloadStrategy {
    private static final Properties properties;
    private int retries;

    static {
        properties = new Properties();
        try {
            properties.load(PepsDataSource.class.getResourceAsStream("peps.properties"));
        } catch (IOException ignored) {
        }
    }

    public PepsDownloadStrategy(String targetFolder) {
        super(targetFolder, properties);
        retries = Integer.parseInt(properties.getProperty("peps.wait.retries", "5"));
    }

    @Override
    public Path fetch(EOProduct product) throws IOException {
        Utilities.ensureExists(Paths.get(destination));
        String productName = product.getName();
        currentStep = "Metadata";
        ProductState productState;
        try (CloseableHttpResponse response = NetUtils.openConnection(getMetadataUrl(product), this.credentials)) {
            switch (response.getStatusLine().getStatusCode()) {
                case 200:
                    JsonResponseParser<Boolean> parser = new JsonResponseParser<>(new PepsMetadataResponseHandler());
                    List<Boolean> parse = parser.parse(EntityUtils.toString(response.getEntity()));
                    productState = parse.get(0) ? ProductState.AVAILABLE : ProductState.ON_TAPE;
                    break;
                case 401:
                    throw new QueryException("The supplied credentials are invalid or the product was not found!");
                default:
                    throw new QueryException(String.format("The request was not successful. Reason: %s",
                                                           response.getStatusLine().getReasonPhrase()));
            }
        } catch (IOException ex) {
            throw new QueryException(ex);
        }
        currentStep = "Archive";
        Path rootPath = Paths.get(destination, productName + ".zip");
        Path productFile = null;
        switch (productState) {
            case ON_TAPE:
                while (retries >= 0) {
                    try {
                        int maxRetries = Integer.parseInt(properties.getProperty("peps.wait.retries", "5"));
                        logger.info(String.format("Product [%s] is stored on tape, retrying after 30 seconds (retry %s of %s)",
                                                  productName, maxRetries - retries + 1, maxRetries + 1));
                        Thread.sleep(30000);
                    } catch (InterruptedException ignored) { }
                    retries--;
                    productFile = fetch(product);
                }
                break;
            case AVAILABLE:
                productFile = downloadFile(getProductUrl(product), rootPath, NetUtils.getAuthToken());
                break;
        }
        if (productFile != null) {
            FileUtils.unzip(productFile);
        }
        return productFile;
    }

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        String location = descriptor.getLocation();
        if (location.endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }
        return location.replace("/download", "");
    }

    private enum ProductState {
        AVAILABLE,
        ON_TAPE,
        ERROR;
    }
}
