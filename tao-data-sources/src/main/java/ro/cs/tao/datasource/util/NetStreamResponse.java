package ro.cs.tao.datasource.util;

import java.io.Closeable;
import java.io.IOException;

public class NetStreamResponse implements Closeable {
    private final long length;
    private final String contentType;
    private final byte[] stream;


    public NetStreamResponse(long length, String type, byte[] stream) {
        this.length = length;
        this.contentType = type;
        this.stream = stream;
    }

    public long getLength() { return length; }

    public String getContentType() { return contentType; }

    public byte[] getStream() { return stream; }

    @Override
    public void close() throws IOException {
//        if (stream != null) {
//            stream.close();
//        }
    }
}
