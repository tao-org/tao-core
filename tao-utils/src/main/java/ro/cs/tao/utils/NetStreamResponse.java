package ro.cs.tao.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a response that encapsulates details of a network stream.
 * This class provides information about the content of the stream, such
 * as its name, length, content type, and the raw byte stream data.
 *
 * This class implements the {@link Closeable} interface, allowing resources
 * associated with the stream to be properly released when they are no longer needed.
 */
public class NetStreamResponse implements Closeable {
    private final String name;
    private final long length;
    private final String contentType;
    private final byte[] stream;


    public NetStreamResponse(String name, long length, String type, byte[] stream) {
        this.name = name;
        this.length = length;
        this.contentType = type;
        this.stream = stream;
    }

    public long getLength() { return length; }

    public String getContentType() { return contentType; }

    public byte[] getStream() { return stream; }

    public String getName() { return name; }

    @Override
    public void close() throws IOException {
    }
}
