package ro.cs.tao.component;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.geotools.http.HTTPResponse;
import org.geotools.http.SimpleHttpClient;
import ro.cs.tao.utils.PublicHttp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/** Read-only, public-network transport for inspecting untrusted OGC services. */
public final class PublicServiceHttpClient extends SimpleHttpClient {
    private final WebServiceAuthentication authentication;
    private final URL origin;
    public PublicServiceHttpClient(WebServiceAuthentication authentication, URL origin) {
        this.authentication = authentication;
        this.origin = origin;
    }

    @Override
    public HTTPResponse get(URL url, Map<String, String> headers) throws IOException {
        if (!origin.getProtocol().equalsIgnoreCase(url.getProtocol())
                || !origin.getHost().equalsIgnoreCase(url.getHost())
                || effectivePort(origin) != effectivePort(url)) {
            throw new IOException("Inspection cannot follow service URLs to another origin");
        }
        UsernamePasswordCredentials credentials = null;
        if (authentication != null && authentication.getType() != null) {
            switch (authentication.getType()) {
                case NONE -> { }
                case BASIC -> credentials = new UsernamePasswordCredentials(authentication.getUser(), authentication.getPassword());
                default -> throw new IOException("Inspection supports only anonymous or HTTP Basic authentication");
            }
        }
        byte[] data = PublicHttp.get(url.toExternalForm(), credentials, 2 * 1024 * 1024);
        return new HTTPResponse() {
            private final InputStream input = new ByteArrayInputStream(data);
            public void dispose() { try { input.close(); } catch (IOException ignored) { } }
            public String getContentType() { return "application/xml"; }
            public String getResponseHeader(String name) { return "Content-Type".equalsIgnoreCase(name) ? getContentType() : null; }
            public InputStream getResponseStream() { return input; }
            public String getResponseCharset() { return "UTF-8"; }
        };
    }

    private static int effectivePort(URL url) {
        return url.getPort() < 0 ? url.getDefaultPort() : url.getPort();
    }

    @Override
    public HTTPResponse post(URL url, InputStream content, String type) throws IOException {
        throw new IOException("Remote inspection is read-only");
    }
}
