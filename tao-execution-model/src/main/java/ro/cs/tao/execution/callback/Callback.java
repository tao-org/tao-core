package ro.cs.tao.execution.callback;

import java.util.logging.Logger;

public abstract class Callback implements CallbackClient {
    protected final Logger logger;
    protected final EndpointDescriptor descriptor;
    protected ResponseConverter converter;

    public Callback(EndpointDescriptor endpointDescriptor) {
        this.descriptor = endpointDescriptor;
        this.logger = Logger.getLogger(getClass().getName());
    }
}
