package ro.cs.tao.execution.drmaa;

import org.esa.sen2agri.bridge.spring.SpringContextBridge;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InternalException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cosmin on 9/12/2017.
 */
public class DrmaaTaoExecutor extends Executor {
    protected Logger logger = Logger.getLogger(DrmaaTaoExecutor.class.getName());

    private static final int TIMER_PERIOD = 1000;
    private final Timer executionsCheckTimer = new Timer();
    /* Flag for trying to close the monitoring thread in an elegant manner */
    private Boolean isInitialized = false;
    protected Session session;
    private PersistenceManager persistenceManager = SpringContextBridge.services().getPersistenceManager();
    @Override
    public void initialize() throws ExecutionException {
        synchronized (isInitialized) {
            if (isInitialized)
                return;
            // mark the executor as initialized
            isInitialized = true;
        }
        session = SessionFactory.getFactory().getSession();
        try {
            session.init (null);
        } catch (DrmaaException e) {
            isInitialized = false;
            throw new ExecutionException("Error initiating DRMAA session", e);
        }
        // once the session was created, start the timer
        executionsCheckTimer.schedule(new ExecutionsCheckTimer(this), 0, TIMER_PERIOD);
    }

    @Override
    public void close()  throws ExecutionException {
        // stop the monitoring thread
        synchronized (isInitialized) {
            if (!isInitialized)
                return;
            isInitialized = false;
        }
        executionsCheckTimer.cancel();
        try {
            session.exit ();
        } catch (DrmaaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean supports(TaoComponent component) {
        return (component instanceof ProcessingComponent);
    }

    @Override
    public void execute(ExecutionTask task) throws ExecutionException  {
        // Get from the component the execution command
        String executionCmd = task.buildExecutionCommand();
        List<String> argsList = new ArrayList<>();
        // split the execution command but preserving the entities between double quotes
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(executionCmd);
        while (m.find()) {
            argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
        }
        String cmd = argsList.remove(0);

        try {
            JobTemplate jt = session.createJobTemplate ();
            if (jt == null) {
                throw new ExecutionException("Error creating job template from the session!");
            }

            jt.setRemoteCommand (cmd);
            jt.setArgs (argsList);
            String id = session.runJob (jt);
            if(id == null) {
                throw new ExecutionException("Unable to run job (id null) for task " + task.getId());
            }

            session.deleteJobTemplate (jt);

            task.setResourceId(id);
            task.setStartTime(LocalDateTime.now());
            task.setExecutionStatus(ExecutionStatus.QUEUED_ACTIVE);
            persistenceManager.updateExecutionTask(task);
            logger.info("DrmaaExecutor: Succesfully submitted task with id " + id);
        } catch (DrmaaException | InternalException e) {
            logger.severe("DrmaaExecutor: Error submitting task with id " + task.getId() + " for command " + cmd +
                        ". The exception was " + e.getMessage());
            throw new ExecutionException("Error executing DRMAA session operation", e);
        } catch (PersistenceException e) {
            throw new ExecutionException("Unable to save execution state in the database", e);
        }
    }

    @Override
    public void stop(ExecutionTask task)  throws ExecutionException {
        try {
            session.control (task.getResourceId(), Session.TERMINATE);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session terminate for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void suspend(ExecutionTask task) throws ExecutionException {
        try {
            session.control (task.getResourceId(), Session.SUSPEND);
            changeTaskStatus(task, ExecutionStatus.SUSPENDED);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session suspend for task with id " + task.getResourceId(), e);
        } catch (PersistenceException e) {
            throw new ExecutionException("Error saving suspended status for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void resume(ExecutionTask task) throws ExecutionException {
        try {
            session.control (task.getResourceId(), Session.RESUME);
            changeTaskStatus(task, ExecutionStatus.RUNNING);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session resume for task with id " + task.getResourceId(), e);
        } catch (PersistenceException e) {
            throw new ExecutionException("Error saving resumed status for task with id " + task.getResourceId(), e);
        }
}

    protected void monitorExecutions() {
        if(!isInitialized) {
            return;
        }
        // check for the finished
        try {
            List<ExecutionTask> tasks = persistenceManager.getRunningTasks();
            // For each job, get its status from DRMAA
            for (ExecutionTask task: tasks) {
                try {
                    if(task.getResourceId() == null) {
                        // ignore tasks having resourceId null
                        continue;
                    }
                    int jobStatus = session.getJobProgramStatus(task.getResourceId());

                    switch (jobStatus) {
                        case Session.SYSTEM_ON_HOLD:
                        case Session.USER_ON_HOLD:
                        case Session.USER_SYSTEM_ON_HOLD:
                        case Session.SYSTEM_SUSPENDED:
                        case Session.USER_SUSPENDED:
                        case Session.USER_SYSTEM_SUSPENDED:
                        case Session.UNDETERMINED:
                        case Session.QUEUED_ACTIVE:
                            // nothing to do
                            break;
                        case Session.RUNNING:
                            changeTaskStatus(task, ExecutionStatus.RUNNING);
                            break;
                        case Session.DONE:
                            // Just mark the job as finished with success status
                            markTaskFinished(task, ExecutionStatus.DONE);
                            logger.info("DrmaaExecutor: task with id " + task.getResourceId() + " finished OK");
                            break;
                        case Session.FAILED:
                            // Just mark the job as finished with failed status
                            markTaskFinished(task, ExecutionStatus.FAILED);
                            logger.info("DrmaaExecutor: task with id " + task.getResourceId() + " finished NOK");
                            break;
                    }
                } catch (DrmaaException | InternalException e) {
                    logger.severe("DrmaaExecuto exception " + e.getClass().getName() + ": Cannot get the status for the task with id " + task.getResourceId());
                    markTaskFinished(task, ExecutionStatus.DONE);
                }
            }
        } catch (PersistenceException e) {
            throw new ExecutionException("Unable to save execution state in the database", e);
        }
    }

    @Override
    public String defaultName() { return "DRMAAExecutor"; }

    private void markTaskFinished(ExecutionTask task, ExecutionStatus status) throws PersistenceException {
        task.setEndTime(LocalDateTime.now());
        changeTaskStatus(task, status);
    }

    private void changeTaskStatus(ExecutionTask task, ExecutionStatus status) throws PersistenceException {
        if(status != task.getExecutionStatus()) {
            task.setExecutionStatus(status);
            persistenceManager.updateExecutionTask(task);
        }
    }

    private class ExecutionsCheckTimer extends TimerTask {
        private DrmaaTaoExecutor executor;
        public ExecutionsCheckTimer(DrmaaTaoExecutor executor) {
            this.executor = executor;
        }
        @Override
        public void run() {
            try {
                executor.monitorExecutions();
            } catch (ExecutionException e) {
                logger.severe("DrmaaExecutor: Error during monitoring executions " + e.getMessage());
            }

        }
    }
}
