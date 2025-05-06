package ro.cs.tao.execution.callback;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import ro.cs.tao.utils.NetUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * HttpCallback is a concrete implementation of the abstract {@link Callback} class,
 * specifically designed for handling HTTP-based communication with an external endpoint.
 * This class processes a list of parameters, establishes an HTTP connection,
 * and evaluates the response status and content.
 *
 * Key features:
 * - Supports HTTP request and response handling.
 * - Evaluates HTTP response codes such as 200 and 401.
 * - Logs error messages for unsuccessful requests.
 */
public class HttpCallback extends Callback {

    public HttpCallback(EndpointDescriptor endpointDescriptor) {
        super(endpointDescriptor);
    }

    @Override
    public int call(List<NameValuePair> params) {
        int retCode = -1;
        try (CloseableHttpResponse response = createConnection(params)) {
            retCode = response.getStatusLine().getStatusCode();
            switch (retCode) {
                case 200:
                    break;
                case 401:
                    logger.severe("Unauthorized. Please check the sent credentials.");
                    break;
                default:
                    logger.severe(String.format("The request was not successful. Reason: %s",
                                                response.getStatusLine().getReasonPhrase()));
                    break;
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return retCode;
    }

    private CloseableHttpResponse createConnection(List<NameValuePair> params) throws IOException {
        final URL url = new URL(this.descriptor.getProtocol(),
                this.descriptor.getHostName(),
                this.descriptor.getPort(),
                this.descriptor.getEndpoint());
        return this.converter != null ?
                NetUtils.openConnection(this.descriptor.getMethod(), url.toString(), this.descriptor.getCredentials(),
                                        converter.convert(params)) :
                NetUtils.openConnection(this.descriptor.getMethod(), url.toString(), this.descriptor.getCredentials(), params);
    }
}
