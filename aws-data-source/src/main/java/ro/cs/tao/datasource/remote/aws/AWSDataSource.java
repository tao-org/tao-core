package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.remote.URLDataSource;
import ro.cs.tao.datasource.remote.aws.parameters.LandsatParameterProvider;
import ro.cs.tao.datasource.remote.aws.parameters.Sentinel2ParameterProvider;
import ro.cs.tao.eodata.EOData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
public class AWSDataSource extends URLDataSource<EOData, AWSDataQuery> {
    private static String URL;

    static {
        Properties props = new Properties();
        try {
            props.load(AWSDataSource.class.getResourceAsStream("aws.properties"));
            URL = props.getProperty("scihub.search.url");
        } catch (IOException ignored) {
        }
    }

    public AWSDataSource() throws URISyntaxException {
        super(URL);
        addParameterProvider("S2", new Sentinel2ParameterProvider());
        addParameterProvider("L8", new LandsatParameterProvider());
    }

    @Override
    protected AWSDataQuery createQueryImpl(String code) {
        return new AWSDataQuery(this, getParameterProvider(code));
    }
}
