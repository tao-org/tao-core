package ro.cs.tao.execution.drmaa;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InternalException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cosmin on 9/12/2017.
 */
public class DrmaaTaoExecutor extends Executor {
    private Session session;

    @Override
    public void initialize() throws ExecutionException {
        session = org.ggf.drmaa.SessionFactory.getFactory().getSession();
        try {
            session.init(null);
            super.initialize();
        } catch (DrmaaException e) {
            isInitialized = false;
            throw new ExecutionException("Error initiating DRMAA session", e);
        }
    }

    @Override
    public void close() throws ExecutionException {
        super.close();
        try {
            session.exit();
        } catch (DrmaaException e) {
            logger.severe(e.getMessage());
        }
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
            JobTemplate jt = session.createJobTemplate();
            if (jt == null) {
                throw new ExecutionException("Error creating job template from the session!");
            }

            jt.setRemoteCommand(cmd);
            jt.setArgs(argsList);
            String id = session.runJob(jt);
            if(id == null) {
                throw new ExecutionException("Unable to run job (id null) for task " + task.getId());
            }

            session.deleteJobTemplate(jt);

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
            session.control(task.getResourceId(), Session.TERMINATE);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session terminate for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void suspend(ExecutionTask task) throws ExecutionException {
        try {
            session.control(task.getResourceId(), Session.SUSPEND);
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
            session.control(task.getResourceId(), Session.RESUME);
            changeTaskStatus(task, ExecutionStatus.RUNNING);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session resume for task with id " + task.getResourceId(), e);
        } catch (PersistenceException e) {
            throw new ExecutionException("Error saving resumed status for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void monitorExecutions() {
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
}
