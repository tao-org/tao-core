package ro.cs.tao.execution.impl;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InternalException;
import org.ggf.drmaa.InvalidJobException;
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
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

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
    protected SessionFactory sessionFactory = SessionFactory.getFactory();
    private PersistenceManager persistenceManager = SpringContextBridge.services().getPersistenceManager();
    private List<ExecutionTask> scheduledTasks = new ArrayList<>();
    @Override
    public void initialize() throws ExecutionException {
        synchronized (isInitialized) {
            if (isInitialized)
                return;
            isInitialized = true;
        }
        session = sessionFactory.getSession();
        try {
            session.init (null);
        } catch (DrmaaException e) {
            isInitialized = false;
            throw new ExecutionException("Error initiating DRMAA session", e);
        }
        // once the session was created, start the timer
        executionsCheckTimer.schedule(new ExecutionsCheckTimer(this), 0, TIMER_PERIOD);
        // mark the executor as initialized
        isInitialized = true;
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
            // TODO: We should create a job name specific to TAO, for job and for task name
            //jt.setJobName();

            jt.setRemoteCommand (cmd);
            jt.setArgs (argsList);
            String id = session.runJob (jt);
            session.deleteJobTemplate (jt);

            task.setResourceId(id);
            task.setExecutionStatus(ExecutionStatus.RUNNING);
            persistenceManager.updateExecutionTask(task);
            logger.info("DrmaaExecutor: Succesfully submitted task with id " + id);
        } catch (DrmaaException e) {
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
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session suspend for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void resume(ExecutionTask task) throws ExecutionException {
        try {
            session.control (task.getResourceId(), Session.RESUME);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session resume for task with id " + task.getResourceId(), e);
        }
    }

    protected void monitorExecutions() {
        // check for the finished
        try {
            List<ExecutionTask> tasks = persistenceManager.getRunningTasks();
            // For each job, get its status from DRMAA
            for (ExecutionTask task: tasks) {
                try {
                    if(task.getResourceId() == null) {
                        System.out.println("Null???");
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
                        case Session.RUNNING:
                            // nothing to do
                            break;
                        case Session.DONE:
                            // TODO: Get the results for the job
                            task.setExecutionStatus(ExecutionStatus.DONE);
                            persistenceManager.updateExecutionTask(task);
                            logger.info("DrmaaExecutor: task with id " + task.getResourceId() + " finished OK");
                            break;
                        case Session.FAILED:
                            task.setExecutionStatus(ExecutionStatus.FAILED);
                            persistenceManager.updateExecutionTask(task);
                            logger.info("DrmaaExecutor: task with id " + task.getResourceId() + " finished NOK");
                            break;
                    }
                } catch (InvalidJobException e) {
                    logger.severe("DrmaaExecutor: Cannot get the status for the task with " + task.getResourceId());
                    task.setExecutionStatus(ExecutionStatus.DONE);
                    persistenceManager.updateExecutionTask(task);
                }
                catch (DrmaaException | InternalException e1) {
                    // TODO: Here we have no idea about what happened with that job. Did it finished normally or failed?
                    // TODO: Unfortunatelly, we do not know about the results produced by that job
                    throw new ExecutionException("Unable to save execution state in the database", e1);
                }
            }
        } catch (PersistenceException e) {
            throw new ExecutionException("Unable to save execution state in the database", e);
        }
    }

    @Override
    public String defaultName() { return "DRMAAExecutor"; }

    private class ExecutionsCheckTimer extends TimerTask {
        private DrmaaTaoExecutor executor;
        public ExecutionsCheckTimer(DrmaaTaoExecutor executor) {
            this.executor = executor;
        }
        @Override
        public void run() {
            executor.monitorExecutions();
        }
    }
}
