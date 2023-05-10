package ro.cs.tao.orchestration;

import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.orchestration.commands.JobCommand;
import ro.cs.tao.persistence.PersistenceException;

import java.util.logging.Logger;

public class JobQueueWorker extends Thread {
    private final JobQueue queue;
    private final Logger logger;
    private final int maxJobs;
    private final Object monitor;
    private Orchestrator orchestrator;
    private volatile boolean paused;
    private volatile boolean stopped;

    public JobQueueWorker(JobQueue queue) {
        this.queue = queue;
        this.setName("job-queue-worker");
        this.logger = Logger.getLogger(JobQueueWorker.class.getName());
        this.maxJobs = NodeManager.getInstance().getActiveNodes() * 2;
        this.monitor = new Object();
    }

    public void setOrchestrator(Orchestrator instance) {
        this.orchestrator = instance;
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
                    if (this.orchestrator.getActiveJobsCount() >= this.maxJobs) {
                        synchronized (this.monitor) {
                            this.monitor.wait(10000);
                        }
                    } else {
                        final ExecutionJob job = this.queue.take();
                        this.orchestrator.incrementActiveJobs();
                        try {
                            JobCommand.START.applyTo(job);
                        } catch (ExecutionException e) {
                            try {
                                this.orchestrator.cancelJob(job);
                            } catch (PersistenceException ex) {
                                logger.severe(ex.getMessage());
                            } finally {
                                this.orchestrator.decrementActiveJobs();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.warning(e.getMessage());
            }
        }
    }
}
