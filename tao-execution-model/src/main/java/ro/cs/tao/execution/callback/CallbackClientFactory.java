package ro.cs.tao.execution.callback;

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
