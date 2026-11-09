package ro.cs.tao.execution.callback;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * Defines a contract for a client that performs custom callback operations
 * by sending data to an external endpoint and optionally processing responses.
 */
public interface CallbackClient {
    /**
     * Sets a custom {@link ResponseConverter} to be used for converting request parameters
     * before sending them to the external endpoint.
     *
     * @param converter The {@link ResponseConverter} implementation to be used for parameter conversion.
     */
    default void setConverter(ResponseConverter converter) { }

    /**
     * Sends a request with the provided parameters to an external endpoint and processes the response.
     * Depending on the implementation, the behavior of this method may vary, such as making an HTTP call
     * or using a socket connection.
     *
     * @param params A list of {@link NameValuePair} representing the key-value pairs to be sent to the endpoint.
     * @return The result or status code of the operation, which varies based on the implementation:
     *         - For HTTP-based implementations, it returns the HTTP status code.
     *         - For socket-based implementations, it returns the byte size of the data sent.
     */
    int call(List<NameValuePair> params);
}
