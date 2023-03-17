package ro.cs.tao;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.utils.executors.MemoryUnit;
import ro.cs.tao.utils.executors.monitoring.DownloadProgressListener;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class ListenableInputStream extends InputStream {
    private final InputStream wrappedStream;
    private double total;
    private double lastValue;
    private Timer timer;

    public ListenableInputStream(InputStream wrappedStream, ProgressListener listener) {
        this(wrappedStream, 0, listener);
    }

    public ListenableInputStream(InputStream wrappedStream, long contentLength, ProgressListener listener) {
        if (wrappedStream == null || listener == null) {
            throw new IllegalArgumentException("Null argument");
        }
        this.wrappedStream = wrappedStream;
        this.lastValue = 0;
        long reportInterval = Long.parseLong(ConfigurationManager.getInstance().getValue("progress.interval", "5000"));
        this.timer = new Timer("Progress reporter", true);
        Runnable computeProgress;
        if (listener instanceof DownloadProgressListener) {
            computeProgress = new Runnable() {
                private LocalDateTime lastUpdated;
                private double lastRead;
                @Override
                public void run() {
                    double speed = lastUpdated != null
                                    ? (lastValue - lastRead) / MemoryUnit.MB.value().doubleValue() / (double) Duration.between(lastUpdated, LocalDateTime.now()).getSeconds()
                                    : 0;
                    ((DownloadProgressListener) listener).notifyProgress(lastValue / total, speed);
                    lastRead = lastValue;
                    lastUpdated = LocalDateTime.now();
                }
            };
        } else {
            computeProgress = () -> {
                listener.notifyProgress(lastValue / total);
            };
        }
        this.timer.scheduleAtFixedRate(new TimedJob(computeProgress), 0, reportInterval);
        if (contentLength > 0) {
            this.total = contentLength;
        } else {
            try {
                this.total = this.wrappedStream.available();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public int read() throws IOException {
        final int read = wrappedStream.read();
        lastValue += read;
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        final int read = wrappedStream.read(b);
        lastValue += read;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int read = wrappedStream.read(b, off, len);
        lastValue += read;
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
        private final Runnable computeProgress;

        public TimedJob(Runnable computeProgress) {
            this.computeProgress = computeProgress;
        }

        @Override
        public void run() {
            computeProgress.run();
        }
    }
}
