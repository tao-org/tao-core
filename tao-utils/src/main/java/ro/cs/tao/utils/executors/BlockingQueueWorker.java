package ro.cs.tao.utils.executors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * A worker thread class that processes items from a {@link BlockingQueue} using a provided task function.
 * This class is designed to handle concurrent operations on the queue and offers features like pausing,
 * parallel task execution, and state persistence.
 *
 * @param <E> the type of elements in the monitored queue
 */
public class BlockingQueueWorker<E> extends Thread {
    private final BlockingQueue<E> monitoredQueue;
    private final Function<E, Void> work;
    private final ThreadPoolExecutor executor;
    private Consumer<BlockingQueue<E>> statePersister;
    private final Logger logger;
    private volatile boolean stopped;
    private volatile boolean paused;
    private final int delay;
    private final AtomicInteger activeCounter;
    private final AtomicInteger parallelism;

    /**
     * Constructs a BlockingQueueWorker with the specified blocking queue and task function.
     * The worker processes tasks from the queue using the given function sequentially.
     *
     * @param monitoredQueue the blocking queue to be monitored and processed. Must not be null.
     * @param runnable the function to apply to each element taken from the queue. Must not be null.
     */
    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable) {
        this(monitoredQueue, runnable, 1);
    }

    /**
     * Constructs a BlockingQueueWorker with the specified blocking queue, task function, and worker name.
     * The worker processes tasks from the queue using the given function sequentially by default.
     * The specified name is used for identifying the worker.
     *
     * @param monitoredQueue the blocking queue to be monitored and processed. Must not be null.
     * @param runnable the function to apply to each element taken from the queue. Must not be null.
     * @param name the name of the worker. Must not be null or empty.
     */
    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, String name) {
        this(monitoredQueue, runnable, 1, name, 5000);
    }

    /**
     * Constructs a BlockingQueueWorker with the specified blocking queue, task function,
     * and level of parallelism. This constructor also specifies default values for the
     * worker name and task processing delay.
     *
     * @param monitoredQueue the blocking queue to be monitored and processed. Must not be null.
     * @param runnable the function to apply to each element taken from the queue. Must not be null.
     * @param parallelism the number of threads allowed to process tasks concurrently. Must be a non-negative integer.
     */
    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, int parallelism) {
        this(monitoredQueue, runnable, parallelism, "queue-worker", 5000);
    }

    /**
     * Constructs a BlockingQueueWorker with the specified blocking queue, task function,
     * level of parallelism, a worker name, and delay between task executions.
     * This constructor allows for configuring the worker's runtime behavior such as
     * number of parallel tasks and processing delay.
     *
     * @param monitoredQueue the blocking queue to be monitored and processed. Must not be null.
     * @param runnable the function to apply to each element taken from the queue. Must not be null.
     * @param parallelism the number of threads allowed to process tasks concurrently. Must be a non-negative integer.
     * @param name the name of the worker. Used for identifying threads in logs and debugging.
     * @param delay the delay in milliseconds between consecutive task executions. Must be a non-negative integer.
     */
    public BlockingQueueWorker(BlockingQueue<E> monitoredQueue, Function<E, Void> runnable, int parallelism, String name, int delay) {
        this.monitoredQueue = monitoredQueue;
        this.work = runnable;
        //this.executor = new NamedThreadPoolExecutor(name, parallelism > 0 ? parallelism : 1);
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.stopped = false;
        this.delay = delay;
        this.activeCounter = new AtomicInteger(0);
        this.parallelism = new AtomicInteger(parallelism);
        this.logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Sets the state persister for the current worker instance. The state persister is a consumer
     * that provides custom logic to handle the internal state of the monitored queue. This method
     * allows external components to define how the state of the queue should be persisted.
     *
     * @param statePersister a consumer function that processes the blocking queue. Must not be null.
     */
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
                if (this.paused || (this.activeCounter.get() >= this.parallelism.get())) {
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

    /**
     * Returns the number of active threads currently executing tasks within the worker's thread pool.
     *
     * @return the count of active threads processing tasks.
     */
    public int count() {
        return this.executor.getActiveCount();
    }

    /**
     * Sets the level of parallelism for task processing in the worker.
     * The parallelism value determines the maximum number of threads
     * allowed to process tasks concurrently.
     *
     * @param value the number of threads to be used for parallel processing.
     *              Must be a non-negative integer.
     */
    public void setParallelism(int value) {
        this.parallelism.set(value);
    }

    /**
     * Checks whether the worker is currently in a paused state.
     *
     * @return {@code true} if the worker is paused, {@code false} otherwise.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the paused state of the worker. When paused, the worker stops processing tasks
     * from the monitored queue until resumed.
     *
     * @param paused {@code true} to pause the worker, {@code false} to resume task processing.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}