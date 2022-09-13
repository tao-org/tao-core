package ro.cs.tao.utils.executors.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class ListenableInputStream extends InputStream {
    private final InputStream wrappedStream;
    private final ProgressListener listener;
    private double total;
    private double lastValue;
    private Timer timer;

    public ListenableInputStream(InputStream wrappedStream, ProgressListener listener) {
        this(wrappedStream, listener, 5000);
    }

    public ListenableInputStream(InputStream wrappedStream, ProgressListener listener, long reportInterval) {
        if (wrappedStream == null || listener == null) {
            throw new IllegalArgumentException("Null argument");
        }
        this.wrappedStream = wrappedStream;
        this.listener = listener;
        this.lastValue = 0;
        this.timer = new Timer("Progress reporter", true);
        this.timer.scheduleAtFixedRate(new TimedJob(), 0, reportInterval);
        try {
            this.total = this.wrappedStream.available();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public int read() throws IOException {
        final int read = wrappedStream.read();
        if (read != -1) {
            lastValue += read;
        }
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        final int read = wrappedStream.read(b);
        if (read != -1) {
            lastValue += read;
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int read = wrappedStream.read(b, off, len);
        if (read != -1) {
            lastValue += read;
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        lastValue += n;
        return wrappedStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return wrappedStream.available();
    }

    @Override
    public void close() throws IOException {
        wrappedStream.close();
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
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

    private class TimedJob extends TimerTask {
        @Override
        public void run() {
            listener.notifyProgress(lastValue / total);
        }
    }
}