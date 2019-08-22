package ro.cs.tao.utils.executors;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class RejectedTaskHandler implements RejectedExecutionHandler {
    private final Logger logger;

    public RejectedTaskHandler() {
        this.logger = Logger.getLogger(getClass().getName());;
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
