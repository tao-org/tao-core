package ro.cs.tao.datasource.remote.aws.download;

import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.aws.AWSDataSource;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
public class Sentinel2Strategy extends DownloadStrategy {
    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(AWSDataSource.class.getResourceAsStream("aws.properties"));
        } catch (IOException ignored) {
        }
    }

    public Sentinel2Strategy(String targetFolder) {
        super(targetFolder, properties);
    }

    @Override
    public Path fetch(EOProduct product) throws IOException {
        return null;
    }

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        return null;
    }
}
