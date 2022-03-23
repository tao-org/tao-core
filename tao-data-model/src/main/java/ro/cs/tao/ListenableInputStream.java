package ro.cs.tao;

import java.io.IOException;
import java.io.InputStream;

public class ListenableInputStream extends InputStream {
    private final InputStream wrappedStream;
    private final ProgressListener listener;
    private double total;

    public ListenableInputStream(InputStream wrappedStream, ProgressListener listener) {
        if (wrappedStream == null || listener == null) {
            throw new IllegalArgumentException("Null argument");
        }
        this.wrappedStream = wrappedStream;
        this.listener = listener;
        try {
            this.total = this.wrappedStream.available();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public int read() throws IOException {
        final int read = wrappedStream.read();
        if (this.total > 0) {
            this.listener.notifyProgress(read / total);
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        final int read = wrappedStream.read(b);
        if (this.total > 0) {
            this.listener.notifyProgress(read / total);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int read = wrappedStream.read(b, off, len);
        if (this.total > 0) {
            this.listener.notifyProgress(read / total);
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        return wrappedStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return wrappedStream.available();
    }

    @Override
    public void close() throws IOException {
        wrappedStream.close();
    }

    @Override
    public void mark(int readlimit) {
        wrappedStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        wrappedStream.reset();
    }

    @Override
    public boolean markSupported() {
        return wrappedStream.markSupported();
    }
}
