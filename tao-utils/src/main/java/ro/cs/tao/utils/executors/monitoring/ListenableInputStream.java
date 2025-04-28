package ro.cs.tao.utils.executors.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A wrapper around an {@link InputStream} that reports progress to a {@link ProgressListener}.
 * This class enables monitoring of the read progress by notifying the listener at regular intervals.
 * The intervals are specified in milliseconds during the creation of the instance.
 *
 * The progress is calculated as the number of bytes read relative to the total byte count of the underlying stream.
 * If the underlying stream does not support the {@link InputStream#available()} method reliably, progress reporting may be inaccurate.
 *
 * The class also ensures thread-safe progress updates via a timer that repeatedly notifies the listener.
 */
public class ListenableInputStream extends InputStream {
    private final InputStream wrappedStream;
    private final ProgressListener listener;
    private double total;
    private double lastValue;
    private Timer timer;

    /**
     * Constructs a new ListenableInputStream that wraps the provided InputStream and attaches
     * a ProgressListener to monitor the progress of the stream's operations.
     * This constructor uses a default reporting interval of 5000 milliseconds for progress updates.
     *
     * @param wrappedStream the InputStream to be wrapped by this ListenableInputStream.
     * @param listener the ProgressListener that will receive progress updates.
     * @throws IllegalArgumentException if either wrappedStream or listener is null.
     */
    public ListenableInputStream(InputStream wrappedStream, ProgressListener listener) {
        this(wrappedStream, listener, 5000);
    }

    /**
     * Constructs a new ListenableInputStream that wraps the provided InputStream and attaches
     * a ProgressListener to monitor the progress of the stream's I/O operations.
     * This constructor schedules periodic progress updates at a specified reporting interval.
     *
     * @param wrappedStream the InputStream to be wrapped by this ListenableInputStream.
     *                      Must not be null.
     * @param listener      the ProgressListener that will receive progress updates.
     *                      Must not be null.
     * @param reportInterval the interval in milliseconds at which progress updates will be reported.
     * @throws IllegalArgumentException if the wrappedStream or listener is null.
     */
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

    /**
     * A private class that extends {@link TimerTask} to periodically report progress updates
     * for an ongoing operation. This class is utilized internally by {@code ListenableInputStream}
     * to execute its scheduled progress-reporting logic.
     *
     * The {@code run()} method calculates the current progress as a ratio of completed work
     * to the total work and notifies the attached {@code ProgressListener}.
     *
     * Thread Safety:
     * This class is not intended to be used independently and relies on proper synchronization
     * ensured by the outer {@code ListenableInputStream} implementation.
     *
     * Usage:
     * This internal utility is utilized by scheduling it on a {@link Timer} instance within
     * {@code ListenableInputStream}.
     */
    private class TimedJob extends TimerTask {
        @Override
        public void run() {
            listener.notifyProgress(lastValue / total);
        }
    }
}