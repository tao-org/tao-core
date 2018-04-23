/*
 * Copyright (C) 2017 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.execution.drmaa;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InternalException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import ro.cs.tao.docker.Container;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.local.DefaultSession;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Cara
 */
public class DrmaaTaoExecutor extends Executor<ProcessingExecutionTask> {
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
    public void execute(ProcessingExecutionTask task) throws ExecutionException  {
        try {
            // Get from the component the execution command
            Container container = persistenceManager.getContainerById(task.getComponent().getContainerId());
            String location = task.getComponent().getFileLocation();
            if (!Paths.get(location).isAbsolute()) {
                task.getComponent().setExpandedFileLocation(Paths.get(container.getApplicationPath(), location).toString());
            }
            String executionCmd = task.buildExecutionCommand();
            List<String> argsList = new ArrayList<>();
            // split the execution command but preserving the entities between double quotes
            Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(executionCmd);
            while (m.find()) {
                argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
            }
            //logger.info(String.format("Task %s : %s", task.getId(), String.join(" ", argsList)));
            String cmd = argsList.remove(0);
            JobTemplate jt = session.createJobTemplate();
            if (jt == null) {
                throw new ExecutionException("Error creating job template from the session!");
            }

            jt.setRemoteCommand(cmd);
            jt.setArgs(argsList);
            String id = session.runJob(jt);
            if(id == null) {
                throw new ExecutionException(String.format("Unable to run job (id null) for task %s", task.getId()));
            }

            session.deleteJobTemplate(jt);

            task.setResourceId(id);
            task.setStartTime(LocalDateTime.now());
            changeTaskStatus(task, session instanceof DefaultSession ? ExecutionStatus.RUNNING : ExecutionStatus.QUEUED_ACTIVE);
            //persistenceManager.updateExecutionTask(task);
            logger.info(String.format("Succesfully submitted task with id %s", id));
        } catch (DrmaaException | InternalException | PersistenceException e) {
            logger.severe(String.format("Error submitting task with id %s: %s", task.getId(), e.getMessage()));
            throw new ExecutionException("Error executing DRMAA session operation", e);
        }/* catch (PersistenceException e) {
            throw new ExecutionException("Unable to save execution state in the database", e);
        }*/
    }

    @Override
    public void stop(ProcessingExecutionTask task)  throws ExecutionException {
        try {
            session.control(task.getResourceId(), Session.TERMINATE);
            markTaskFinished(task, ExecutionStatus.CANCELLED);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session terminate for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void suspend(ProcessingExecutionTask task) throws ExecutionException {
        try {
            session.control(task.getResourceId(), Session.SUSPEND);
            changeTaskStatus(task, ExecutionStatus.SUSPENDED);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session suspend for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void resume(ProcessingExecutionTask task) throws ExecutionException {
        try {
            session.control(task.getResourceId(), Session.RESUME);
            changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session resume for task with id " + task.getResourceId(), e);
        }
    }

    @Override
    public void monitorExecutions() {
        if(!isInitialized) {
            return;
        }
        // check for the finished
        List<ExecutionTask> tasks = persistenceManager.getRunningTasks();
        if (tasks != null) {
            tasks.removeIf(t -> !(t instanceof ProcessingExecutionTask));
            // For each job, get its status from DRMAA
            for (ExecutionTask task : tasks) {
                try {
                    if (task.getResourceId() == null) {
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
                            if (task.getExecutionStatus() != ExecutionStatus.RUNNING) {
                                changeTaskStatus(task, ExecutionStatus.RUNNING);
                            }
                            break;
                        case Session.DONE:
                            // Just mark the job as finished with success status
                            if (task.getExecutionStatus() == ExecutionStatus.RUNNING) {
                                // Only a running task can complete
                                markTaskFinished(task, ExecutionStatus.DONE);
                            }
                            logger.info(String.format("Task %s DONE", task.getResourceId()));
                            break;
                        case Session.FAILED:
                            // Just mark the job as finished with failed status
                            markTaskFinished(task, ExecutionStatus.FAILED);
                            logger.info(String.format("Task %s FAILED", task.getResourceId()));
                            break;
                    }
                } catch (DrmaaException | InternalException e) {
                    logger.severe(String.format("%s: Cannot get the status for the task %s [%s]",
                                                e.getClass().getName(), task.getResourceId(), e.getMessage()));
                    markTaskFinished(task, ExecutionStatus.FAILED);
                }
            }
        }
    }

    @Override
    public String defaultName() { return "DRMAAExecutor"; }
}
