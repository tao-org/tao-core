package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.remote.URLDataSource;
import ro.cs.tao.datasource.remote.aws.parameters.AWSParameterProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

/**
 * @author Cosmin Cara
 */
public class AWSDataSource extends URLDataSource<AWSDataQuery> {
    private static String S2_URL;
    private static String L8_URL;

    static {
        Properties props = new Properties();
        try {
            props.load(AWSDataSource.class.getResourceAsStream("aws.properties"));
            S2_URL = props.getProperty("s2.aws.search.url");
            L8_URL = props.getProperty("l8.aws.search.url");
            Executors.newSingleThreadExecutor().submit(
                    () -> {
                        try {
                            LogManager.getLogManager().getLogger("").info("Loading Sentinel-2 tiles extents");
                            Sentinel2TileExtent.getInstance()
                                    .read(AWSDataSource.class.getResourceAsStream("S2tilemap.dat"));
                        } catch (Exception e) {
                            e.printStackTrace();;
                        }
                        return null;
                    });
            Executors.newSingleThreadExecutor().submit(
                    () -> {
                        try {
                            LogManager.getLogManager().getLogger("").info("Loading Landsat8 tiles extents");
                            Landsat8TileExtent.getInstance()
                                    .read(AWSDataSource.class.getResourceAsStream("L8tilemap.dat"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
        } catch (IOException ignored) {
        }
    }

    public AWSDataSource() throws URISyntaxException {
        super(S2_URL);
        setParameterProvider(new AWSParameterProvider());
    }

    @Override
    public String defaultName() { return "Amazon Web Services"; }

    @Override
    protected AWSDataQuery createQueryImpl(String sensorName) {
        try {
            switch (sensorName) {
                case "Sentinel-2":
                    this.connectionString = S2_URL;
                    this.remoteUrl = new URI(this.connectionString);
                    break;
                case "Landsat-8":
                    this.connectionString = L8_URL;
                    this.remoteUrl = new URI(this.connectionString);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("'%s' is not supported", sensorName));
            }
            return new AWSDataQuery(this, sensorName);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Malformed url: " + ex.getMessage());
        }
    }
}
