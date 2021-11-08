package ro.cs.tao.utils.executors;

import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.logging.Logger;

public class BlockingQueueWorker<E> extends Thread {
    private final BlockingQueue<E> monitoredQueue;
    private final Function<E, Void> work;
    private final NamedThreadPoolExecutor executor;
    private final Logger logger;
    private volatile boolean stopped;

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable) {
        this(monitoredQueue, runnable, 1);
    }

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, int parallelism) {
        this.monitoredQueue = monitoredQueue;
        this.work = runnable;
        this.executor = new NamedThreadPoolExecutor("queue-worker", parallelism);
        this.stopped = false;
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public synchronized void start() {
        super.start();
        this.stopped = false;
    }

    @Override
    public void interrupt() {
        this.stopped = true;
        super.interrupt();
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                E entry = this.monitoredQueue.take();
                logger.finest("Dequeued " + entry.toString());
                this.executor.submit(() -> {
                    try {
                        this.work.apply(entry);
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
                });
            } catch (InterruptedException e) {
                logger.warning(e.getMessage());
            }
        }
    }

    public int count() {
        return this.executor.getActiveCount();
    }
}