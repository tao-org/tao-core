package ro.cs.tao.execution.callback;

/**
 * Factory class responsible for creating instances of {@link CallbackClient}
 * based on the specified protocol in the provided {@link EndpointDescriptor}.
 *
 * This factory abstracts the creation logic for callback clients
 * and ensures that the correct implementation is chosen based on the protocol:
 * - For HTTP and HTTPS protocols, {@link HttpCallback} is used.
 * - For TCP protocol, {@link SocketCallback} is used.
 *
 * If an unsupported protocol is specified, an {@link IllegalArgumentException} is thrown.
 *
 * The specific {@link CallbackClient} implementation would handle the communication
 * to the external endpoint as described by the {@link EndpointDescriptor}.
 */
public class CallbackClientFactory {

    public static CallbackClient createFor(EndpointDescriptor descriptor) {
        final CallbackClient client;
        final String proto = descriptor.getProtocol().toLowerCase();
        switch (proto) {
            case "http":
            case "https":
                client = new HttpCallback(descriptor);
                break;
            case "tcp":
                client = new SocketCallback(descriptor);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported callback protocol %s", proto));
        }
        return client;
    }

}
