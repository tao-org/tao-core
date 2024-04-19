package ro.cs.tao.orchestration;

import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.orchestration.commands.JobCommand;
import ro.cs.tao.orchestration.queue.JobQueue;
import ro.cs.tao.persistence.PersistenceException;

import java.util.logging.Logger;

public class JobQueueWorker extends Thread {
    private final JobQueue queue;
    private final Logger logger;
    private int maxJobs;
    private final Object monitor;
    private Orchestrator orchestrator;
    private String lastUserId;
    private volatile boolean paused;
    private volatile boolean stopped;

    public JobQueueWorker(ro.cs.tao.orchestration.queue.JobQueue queue) {
        this.queue = queue;
        this.setName("job-queue-worker");
        this.logger = Logger.getLogger(JobQueueWorker.class.getName());
        this.maxJobs = NodeManager.getInstance().getActiveNodes() * 2;
        this.monitor = new Object();
    }

    public void setOrchestrator(Orchestrator instance) {
        this.orchestrator = instance;
        this.maxJobs = instance.getMaximumAllowedJobs();
    }

    @Override
    public synchronized void start() {
        super.start();
        this.stopped = false;
    }

    @Override
    public void interrupt() {
        this.stopped = true;
        logger.warning("Job queue worker stopped");
        super.interrupt();
    }

    public void pauseWork() {
        this.paused = true;
    }

    public void resumeWork() {
        this.paused = false;
    }

    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void run() {
        while (!this.stopped) {
            try {
                if (this.paused) {
                    synchronized (this.monitor) {
                        this.monitor.wait(10000);
                    }
                } else {
                    int count;
                    if ((count = this.orchestrator.getActiveJobsCount()) >= this.maxJobs) {
                        synchronized (this.monitor) {
                            logger.finest(String.format("Job queue has still %d active jobs (max. limit is %d)",
                                                        count, this.maxJobs));
                            this.monitor.wait(10000);
                        }
                    } else {
                        final ExecutionJob job = this.queue.takeExcept(this.lastUserId != null ? this.lastUserId : null);
                        if (job != null) {
                            try {
                                logger.finest(String.format("Job [%s] for user '%s' was dequeued",
                                                            job.getName(), job.getUserId()));
                                JobCommand.START.applyTo(job);
                                //this.orchestrator.incrementActiveJobs();
                            } catch (ExecutionException e) {
                                try {
                                    logger.warning(String.format("Job [%s] for user '%s' could not be started. Reason: %s",
                                                                 job.getName(), job.getUserId(), e.getMessage()));
                                    this.orchestrator.cancelJob(job);
                                } catch (PersistenceException ex) {
                                    logger.severe(ex.getMessage());
                                }
                            }
                        }
                        this.lastUserId = null;
                        /*synchronized (this.monitor) {
                            this.monitor.wait(2000);
                        }*/
                    }
                }
            } catch (InterruptedException e) {
                logger.warning(e.getMessage());
                this.stopped = true;
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
    }

    public void setLastUserId(String userId) {
        this.lastUserId = userId;
    }
}
