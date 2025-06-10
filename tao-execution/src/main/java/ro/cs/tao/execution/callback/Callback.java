package ro.cs.tao.execution.callback;

import java.util.logging.Logger;

/**
 * An abstract class representing a callback mechanism for interacting with an external endpoint.
 * Extending this class enables custom implementations for sending data and handling responses
 * in various communication protocols (e.g., sockets, HTTP).
 *
 * The class maintains functionality that includes:
 * - Logging for communication operations.
 * - Storage of endpoint connection details.
 * - Customizable response conversion via the {@link ResponseConverter}.
 */
public abstract class Callback implements CallbackClient {
    protected final Logger logger;
    protected final EndpointDescriptor descriptor;
    protected ResponseConverter converter;

    public Callback(EndpointDescriptor endpointDescriptor) {
        this.descriptor = endpointDescriptor;
        this.logger = Logger.getLogger(getClass().getName());
    }
}
