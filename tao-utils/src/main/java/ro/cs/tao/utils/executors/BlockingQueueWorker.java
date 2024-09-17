package ro.cs.tao.utils.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class BlockingQueueWorker<E> extends Thread {
    private final BlockingQueue<E> monitoredQueue;
    private final Function<E, Void> work;
    private final NamedThreadPoolExecutor executor;
    private Consumer<BlockingQueue<E>> statePersister;
    private final Logger logger;
    private volatile boolean stopped;
    private volatile boolean paused;
    private final int delay;
    private final AtomicInteger activeCounter;

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable) {
        this(monitoredQueue, runnable, 1);
    }

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, String name) {
        this(monitoredQueue, runnable, 1, name, 5000);
    }

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, int parallelism) {
        this(monitoredQueue, runnable, parallelism, "queue-worker", 5000);
    }

    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, int parallelism, String name, int delay) {
        this.monitoredQueue = monitoredQueue;
        this.work = runnable;
        this.executor = new NamedThreadPoolExecutor(name, parallelism > 0 ? parallelism : 1);
        this.stopped = false;
        this.delay = delay;
        this.activeCounter = new AtomicInteger(0);
        this.logger = Logger.getLogger(getClass().getName());
    }

    public void setStatePersister(Consumer<BlockingQueue<E>> statePersister) {
        this.statePersister = statePersister;
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
                if (this.paused || (this.activeCounter.get() >= this.executor.getMaximumPoolSize())) {
                    Thread.sleep(this.delay);
                } else {
                    if (this.statePersister != null) {
                        this.statePersister.accept(this.monitoredQueue);
                    }
                    E entry = this.monitoredQueue.take();
                    this.activeCounter.incrementAndGet();
                    this.executor.execute(() -> {
                        try {
                            this.work.apply(entry);
                            // Item was handled, re-persist the state without it
                            if (this.statePersister != null) {
                                this.statePersister.accept(this.monitoredQueue);
                            }
                        } catch (Throwable e) {
                            logger.severe(e.getMessage());
                        } finally {
                            this.activeCounter.decrementAndGet();
                        }
                    });
                }
            } catch (InterruptedException e) {
                logger.warning(e.getMessage());
            }
        }
    }

    public int count() {
        return this.executor.getActiveCount();
    }

    public void setParallelism(int value) {
        this.executor.setCorePoolSize(value);
        this.executor.setMaximumPoolSize(value);
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}