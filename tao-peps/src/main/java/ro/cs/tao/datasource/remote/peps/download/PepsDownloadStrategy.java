package ro.cs.tao.datasource.remote.peps.download;

import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.peps.PepsDataSource;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
public class PepsDownloadStrategy extends DownloadStrategy {
    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(PepsDataSource.class.getResourceAsStream("peps.properties"));
        } catch (IOException ignored) {
        }
    }

    public PepsDownloadStrategy(String targetFolder) {
        super(targetFolder, properties);
    }

    @Override
    public Path fetch(EOProduct product) throws IOException {
        Utilities.ensureExists(Paths.get(destination));
        String productName = product.getName();
        currentStep = "Archive";
        Path rootPath = Paths.get(destination, productName + ".zip");
        return downloadFile(getProductUrl(product), rootPath, NetUtils.getAuthToken());
    }

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        return null;
    }
}
