package ro.cs.tao.execution.callback;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * A concrete implementation of the {@link Callback} abstract class, designed to handle
 * interactions with remote systems via sockets.
 *
 * This class facilitates the sending of data to a specified endpoint by establishing a
 * socket connection, converting parameter data into the appropriate format, and writing it
 * to the socket's output stream.
 */
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
