package ro.cs.tao.execution.impl;

import org.ggf.drmaa.*;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.IExecutor;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cosmin on 9/12/2017.
 */
public class DrmaaTaoExecutor implements IExecutor {
    private static final int TIMER_PERIOD = 1000;
    private final Timer executionsCheckTimer = new Timer();
    /* Flag for trying to close the monitoring thread in an elegant manner */
    private Boolean isInitialized = false;
    protected Session session;
    protected SessionFactory sessionFactory = SessionFactory.getFactory();
    private PersistenceManager persistenceManager = SpringContextBridge.services().getPersistenceManager();

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
        try {
            ProcessingComponent processingComponent = task.getProcessingComponent();
            // TODO: Get from the component the execution command
            String executionCmd = processingComponent.buildExecutionCommand();

            List<String> argsList = new ArrayList<>();
            // split the execution command but preserving the entities between double quotes
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(executionCmd);
            while (m.find()) {
                argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
            }
            String cmd = argsList.remove(0);

            JobTemplate jt = session.createJobTemplate ();
            // TODO: We should create a job name specific to TAO, for job and for task name
            //jt.setJobName();

            jt.setRemoteCommand (cmd);
            jt.setArgs (argsList);
            String id = session.runJob (jt);
            task.setResourceId(id);
            // TODO: add function to PersistenceManager
            //persistenceManager.saveExecutionTask(task);
            session.deleteJobTemplate (jt);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session operation", e);
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
            List<ExecutionTask> tasks = null;
            // TODO: add function to PersistenceManager
            //infos = persistence.getRunningTasks();
            // For each job, get its status from DRMAA
            for (ExecutionTask task: tasks) {
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
                        task.setExecutionStatus(ExecutionStatus.DONE);
                        // TODO: add function to PersistenceManager
                        //persistenceManager.saveExecutionTask(task);
                        break;
                    case Session.FAILED:
                        task.setExecutionStatus(ExecutionStatus.FAILED);
                        // TODO: add function to PersistenceManager
                        //persistenceManager.saveExecutionTask(task);
                        break;
                }

            }
        } catch (DrmaaException e) {
            e.printStackTrace();
            throw new ExecutionException("Error executing DRMAA session operation", e);
        }
    }

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
