package ro.cs.tao.execution.callback;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class SocketCallback extends Callback {

    public SocketCallback(EndpointDescriptor endpointDescriptor) {
        super(endpointDescriptor);
    }

    @Override
    public int call(List<NameValuePair> params) {
        if (this.converter == null) {
            throw new IllegalArgumentException("This callback type requires a response converter");
        }
        int retVal = 0;
        try (Socket socket = createSocket()) {
            final byte[] bytes = converter.convert(params).getBytes();
            socket.getOutputStream().write(bytes);
            retVal = bytes.length;
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return retVal;
    }

    private Socket createSocket() throws IOException {
        return new Socket(this.descriptor.getHostName(), this.descriptor.getPort());
    }
}
