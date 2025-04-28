package ro.cs.tao.utils.executors;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

/**
 * This class implements the {@link RejectedExecutionHandler} interface to handle
 * tasks that are rejected from being executed by a {@link ThreadPoolExecutor}.
 * The rejection may occur when the thread pool is saturated and cannot accept
 * additional tasks.
 *
 * The handler provides logging functionality to record details about the
 * rejected task, including the name of the thread pool, the number of active
 * tasks, and the total number of queued tasks at the time of rejection.
 *
 * It is particularly suited for use with {@link NamedThreadPoolExecutor}, which
 * provides identifiable pool names for better traceability and debugging.
 */
public class RejectedTaskHandler implements RejectedExecutionHandler {
    private final Logger logger;

    public RejectedTaskHandler() {
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        NamedThreadPoolExecutor namedThreadPoolExecutor = (NamedThreadPoolExecutor) executor;
        logger.severe(String.format("An execution task was rejected by [%s]. Active tasks: %d; queued tasks: %d.",
                                    namedThreadPoolExecutor.getPoolName(),
                                    namedThreadPoolExecutor.getActiveCount(),
                                    namedThreadPoolExecutor.getTaskCount()));
    }
}
