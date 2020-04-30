package ro.cs.tao.utils.executors;

import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.logging.Logger;

public class BlockingQueueWorker<E> extends Thread {
    private final BlockingQueue<E> monitoredQueue;
    private final Function<E, Void> work;
    private final NamedThreadPoolExecutor executor;
    private volatile boolean stopped;

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable) {
        this.monitoredQueue = monitoredQueue;
        this.work = runnable;
        this.executor = new NamedThreadPoolExecutor("queue-worker", 1);
        this.stopped = false;
    }

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, int parallelism) {
        this.monitoredQueue = monitoredQueue;
        this.work = runnable;
        this.executor = parallelism == 1 ?
                new NamedThreadPoolExecutor("queue-worker", 1) :
                new NamedThreadPoolExecutor("queue-worker", parallelism);
        this.stopped = false;
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
                this.executor.submit(() -> this.work.apply(entry));
            } catch (InterruptedException e) {
                Logger.getLogger(getClass().getName()).warning(e.getMessage());
            }
        }
    }

    public int count() {
        return this.executor.getActiveCount();
    }
}