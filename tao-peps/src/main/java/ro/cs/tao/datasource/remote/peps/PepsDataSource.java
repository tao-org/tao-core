package ro.cs.tao.datasource.remote.peps;

import ro.cs.tao.datasource.remote.URLDataSource;
import ro.cs.tao.datasource.remote.peps.parameters.PepsParameterProvider;
import ro.cs.tao.datasource.util.NetUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
public class PepsDataSource extends URLDataSource<PepsDataQuery> {
    private static String URL;

    static {
        Properties props = new Properties();
        try {
            props.load(PepsDataSource.class.getResourceAsStream("peps.properties"));
            URL = props.getProperty("peps.search.url");
        } catch (IOException ignored) {
        }
    }

    public PepsDataSource() throws URISyntaxException {
        super(URL);
        setParameterProvider(new PepsParameterProvider());
    }

    @Override
    public String defaultName() { return "PEPS"; }

    @Override
    public void setCredentials(String username, String password) {
        super.setCredentials(username, password);
        String authToken = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        NetUtils.setAuthToken(authToken);
    }

    @Override
    protected PepsDataQuery createQueryImpl(String sensorName) {
        return new PepsDataQuery(this, sensorName);
    }
}
